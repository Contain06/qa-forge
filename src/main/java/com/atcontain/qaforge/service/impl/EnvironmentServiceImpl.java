package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.Environment;
import com.atcontain.qaforge.mapper.EnvironmentMapper;
import com.atcontain.qaforge.service.EnvironmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentServiceImpl extends ServiceImpl<EnvironmentMapper, Environment> implements EnvironmentService{

}
