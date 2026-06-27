package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.ApiCase;
import com.atcontain.qaforge.mapper.ApiCaseMapper;
import com.atcontain.qaforge.service.ApiCaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ApiCaseServiceImpl extends ServiceImpl<ApiCaseMapper, ApiCase> implements ApiCaseService {

}
