package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.dto.RegisterDTO;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.entity.SysUser;
import com.atcontain.qaforge.mapper.SysUserMapper;
import com.atcontain.qaforge.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private SysUserMapper sysUserMapper;
    private PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(SysUserMapper sysUserMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Result<String> register(RegisterDTO registerDTO) {
        SysUser existingUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, registerDTO.getUsername())
        );
        if (existingUser != null) {
            return Result.error(400, "用户名已存在");
        }

        if (!registerDTO.getPassword().equals(registerDTO.getRePassword())) {
            return Result.error(400, "两次输入的密码不一致");
        }
        SysUser existingPhone = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, registerDTO.getPhone())
        );
        if (existingPhone != null) {
            return Result.error(400, "手机号已被注册");
        }
        SysUser existingEmail = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, registerDTO.getEmail())
        );
        if (existingEmail != null) {
            return Result.error(400, "邮箱已被注册");
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));  // ★ 加密存储
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        sysUserMapper.insert(user);
        return Result.success("注册成功");
    }
}
