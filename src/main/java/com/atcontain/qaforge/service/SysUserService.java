package com.atcontain.qaforge.service;

import com.atcontain.qaforge.dto.RegisterDTO;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.validation.Valid;

public interface SysUserService extends IService<SysUser> {
    Result<String> register(@Valid RegisterDTO registerDTO);
}
