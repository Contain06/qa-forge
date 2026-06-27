package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.dto.LoginDTO;
import com.atcontain.qaforge.dto.RegisterDTO;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.entity.SysUser;
import com.atcontain.qaforge.security.util.JwtTokenUtil;
import com.atcontain.qaforge.service.SysUserService;
import com.atcontain.qaforge.util.TokenBlacklistUtils;
import com.atcontain.qaforge.vo.SysUserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class SysUserController {
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistUtils tokenBlacklistService;

    public SysUserController(SysUserService sysUserService, PasswordEncoder passwordEncoder,
                             TokenBlacklistUtils tokenBlacklistService) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public Result<SysUserVO> userLogin(@RequestBody @Valid LoginDTO loginDTO) {
        // 先按用户名 + 启用状态查询用户
        SysUser user = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDTO.getUsername())
                        .eq(SysUser::getStatus, 1)
        );
        // 用户不存在，或密码不匹配
        System.out.println(loginDTO.getUsername() + " " + loginDTO.getPassword());
        if (user == null || !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }

//        ThreadLocal threadLocal = new ThreadLocal();
//        threadLocal.set(user);
        SysUserVO sysUserVO = new SysUserVO();
        BeanUtils.copyProperties(user, sysUserVO);
        String token = JwtTokenUtil.getToken(sysUserVO.getUsername(), sysUserVO.getId());
        sysUserVO.setToken(token);
        System.out.println("登录成功，准备返回，token=" + token);
        return Result.success(sysUserVO);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody @Valid RegisterDTO registerDTO) {
        return sysUserService.register(registerDTO);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.substring(7);
        String jti = JwtTokenUtil.getJti(token);
        long remaining = JwtTokenUtil.getRemainingExpiration(token);
        tokenBlacklistService.addJti(jti, remaining);
        return Result.success();
    }

}
