package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.RunDetail;
import com.atcontain.qaforge.mapper.RunDetailMapper;
import com.atcontain.qaforge.service.RunDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RunDetailServiceImpl extends ServiceImpl<RunDetailMapper, RunDetail> implements RunDetailService {
}
