package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.ApiInfo;
import com.atcontain.qaforge.mapper.ApiInfoMapper;
import com.atcontain.qaforge.service.ApiInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ApiInfoServiceImpl extends ServiceImpl<ApiInfoMapper, ApiInfo> implements ApiInfoService {
    
}
