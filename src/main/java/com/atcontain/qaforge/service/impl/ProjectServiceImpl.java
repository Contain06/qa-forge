package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.mapper.ProjectMapper;
import com.atcontain.qaforge.service.ProjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

}
