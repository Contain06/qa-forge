package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.EnvVariable;
import com.atcontain.qaforge.mapper.EnvVariableMapper;
import com.atcontain.qaforge.service.EnvVariableService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EnvVariableServiceImpl extends ServiceImpl<EnvVariableMapper, EnvVariable> implements EnvVariableService {
}
