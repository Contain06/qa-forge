package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.TestPlan;
import com.atcontain.qaforge.mapper.TestPlanMapper;
import com.atcontain.qaforge.service.TestPlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TestPlanServiceImpl extends ServiceImpl<TestPlanMapper, TestPlan> implements TestPlanService {
}
