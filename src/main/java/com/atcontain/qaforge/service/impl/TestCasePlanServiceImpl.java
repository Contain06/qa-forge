package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.TestPlanCase;
import com.atcontain.qaforge.mapper.TestCasePlanMapper;
import com.atcontain.qaforge.service.TestCasePlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TestCasePlanServiceImpl extends ServiceImpl<TestCasePlanMapper, TestPlanCase> implements TestCasePlanService {
}
