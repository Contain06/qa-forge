package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.ApiAssertion;
import com.atcontain.qaforge.mapper.ApiAssertionMapper;
import com.atcontain.qaforge.service.ApiAssertionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ApiAssertionServiceImpl extends ServiceImpl<ApiAssertionMapper, ApiAssertion> implements ApiAssertionService {

}
