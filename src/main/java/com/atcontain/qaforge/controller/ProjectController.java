package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.dto.PageResult;
import com.atcontain.qaforge.dto.ProjectDTO;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.entity.SysUser;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.service.SysUserService;
import com.atcontain.qaforge.vo.ProjectVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;
    private final SysUserService sysUserService;

    public ProjectController(ProjectService projectService, SysUserService sysUserService) {
        this.projectService = projectService;
        this.sysUserService = sysUserService;
    }

    @GetMapping("/list")
    public Result<PageResult<ProjectVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Page<Project> page = new Page<>(pageNum, pageSize);
        Page<Project> result = projectService.page(
                page,
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
                        .orderByDesc(Project::getCreateTime)
        );

        List<ProjectVO> voList = result.getRecords().stream()
                .map(this::buildProjectVO)
                .toList();

        PageResult<ProjectVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal((int)result.getTotal());
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());

        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<ProjectVO> projectDetailInfo(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, id)
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );
        if (project == null) {
            return Result.error(404, "Project not found");
        }
        return Result.success(buildProjectVO(project));
    }

    @PostMapping("/add")
    public Result<Void> addProject(@RequestBody ProjectDTO projectDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        project.setOwnerId(userId);
        project.setStatus(1);
        project.setCreateBy(userId);
        project.setUpdateBy(userId);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());
        projectService.save(project);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateProject(@RequestBody ProjectDTO projectDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project oldProject = projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, projectDTO.getId())
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );

        if (oldProject == null) {
            return Result.error(404, "Project not found");
        }

        Project project = new Project();
        BeanUtils.copyProperties(projectDTO, project);
        project.setUpdateBy(userId);

        projectService.updateById(project);

        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteProject(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = projectService.getOne(new LambdaQueryWrapper<>(Project.class).eq(Project::getId, id));
        if (project == null) {
            return Result.error(404, "Project not found");
        }
        project.setUpdateBy(userId);
        project.setUpdateTime(LocalDateTime.now());
        projectService.updateById(project);

        projectService.removeById(id);
        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateProjectStatus(@RequestBody ProjectDTO projectDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = projectService.getOne(new LambdaQueryWrapper<>(Project.class).eq(Project::getId, projectDTO.getId()));
        if (project == null) {
            return Result.error(404, "Project not found");
        }
        project.setStatus(projectDTO.getStatus());
        project.setUpdateBy(userId);
        project.setUpdateTime(LocalDateTime.now());
        projectService.updateById(project);
        return Result.success();
    }

    private ProjectVO buildProjectVO(Project project) {
        ProjectVO projectVO = new ProjectVO();
        BeanUtils.copyProperties(project, projectVO);

        SysUser owner = sysUserService.getById(project.getOwnerId());
        if (owner != null) {
            projectVO.setOwnerName(owner.getNickname() != null ? owner.getNickname() : owner.getUsername());
        }

        return projectVO;
    }
}
