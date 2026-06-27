package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.dto.EnvironmentAddDTO;
import com.atcontain.qaforge.dto.EnvironmentUpdateDTO;
import com.atcontain.qaforge.dto.PageResult;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.dto.StatusDTO;
import com.atcontain.qaforge.entity.Environment;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.EnvironmentService;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.vo.EnvironmentVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/environment")
public class EnvironmentController {

    private final EnvironmentService environmentService;
    private final ProjectService projectService;

    public EnvironmentController(EnvironmentService environmentService, ProjectService projectService) {
        this.environmentService = environmentService;
        this.projectService = projectService;
    }

    @GetMapping("/list")
    public Result<PageResult<EnvironmentVO>> list(@RequestParam Integer projectId,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Environment> page = new Page<>(pageNum, pageSize);
        Page<Environment> result = environmentService.page(
                page,
                new LambdaQueryWrapper<Environment>()
                        .eq(Environment::getProjectId, projectId)
                        .eq(Environment::getStatus, 1)
                        .orderByDesc(Environment::getCreateTime)
        );

        List<EnvironmentVO> list = result.getRecords().stream()
                .map(this::buildEnvironmentVO)
                .toList();

        PageResult<EnvironmentVO> pageResult = new PageResult<>();
        pageResult.setRecords(list);
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());
        pageResult.setTotal((int) result.getTotal());

        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<EnvironmentVO> environmentDetailInfo(@PathVariable("id") Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Environment environment = environmentService.getOne(
                new LambdaQueryWrapper<Environment>()
                        .eq(Environment::getId, id)
                        .eq(Environment::getStatus, 1)
        );
        if (environment == null) {
            return Result.error(404, "Environment not found");
        }

        Project project = getOwnedProject(environment.getProjectId(), userId);
        if (project == null) {
            return Result.error(404, "Environment not found");
        }

        return Result.success(buildEnvironmentVO(environment));
    }

    @PostMapping("/add")
    public Result<Void> addEnvironment(@RequestBody @Valid EnvironmentAddDTO environmentAddDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = getOwnedProject(environmentAddDTO.getProjectId(), userId);
        if (project == null) {
            return Result.error(404, "Project not found");
        }

        Environment environment = new Environment();
        BeanUtils.copyProperties(environmentAddDTO, environment);
        environment.setStatus(1);
        environment.setCreateBy(userId);
        environment.setUpdateBy(userId);
        environment.setCreateTime(LocalDateTime.now());
        environment.setUpdateTime(LocalDateTime.now());
        environmentService.save(environment);

        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateEnvironment(@RequestBody @Valid EnvironmentUpdateDTO environmentUpdateDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Environment oldEnvironment = environmentService.getOne(
                new LambdaQueryWrapper<Environment>()
                        .eq(Environment::getId, environmentUpdateDTO.getId())
                        .eq(Environment::getStatus, 1)
        );
        if (oldEnvironment == null) {
            return Result.error(404, "Environment not found");
        }

        Project project = getOwnedProject(oldEnvironment.getProjectId(), userId);
        if (project == null) {
            return Result.error(404, "Environment not found");
        }

        Environment environment = new Environment();
        BeanUtils.copyProperties(environmentUpdateDTO, environment);
        environment.setUpdateBy(userId);
        environment.setUpdateTime(LocalDateTime.now());
        environment.setProjectId(oldEnvironment.getProjectId());
        environmentService.updateById(environment);

        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteEnvironment(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Environment environment = environmentService.getById(id);
        if (environment == null) {
            return Result.error(404, "Environment not found");
        }

        Project project = getOwnedProject(environment.getProjectId(), userId);
        if (project == null) {
            return Result.error(404, "Environment not found");
        }

        environment.setUpdateBy(userId);
        environment.setUpdateTime(LocalDateTime.now());
        environmentService.updateById(environment);
        environmentService.removeById(id);

        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateEnvironmentStatus(@RequestBody @Valid StatusDTO statusDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Environment oldEnvironment = environmentService.getById(statusDTO.getId());
        if (oldEnvironment == null) {
            return Result.error(404, "Environment not found");
        }

        Project project = getOwnedProject(oldEnvironment.getProjectId(), userId);
        if (project == null) {
            return Result.error(404, "Environment not found");
        }

        Environment environment = new Environment();
        environment.setId(statusDTO.getId());
        environment.setStatus(statusDTO.getStatus());
        environment.setUpdateBy(userId);
        environment.setUpdateTime(LocalDateTime.now());
        environmentService.updateById(environment);

        return Result.success();
    }

    private Project getOwnedProject(Integer projectId, Integer userId) {
        return projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, projectId)
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );
    }

    private EnvironmentVO buildEnvironmentVO(Environment environment) {
        EnvironmentVO environmentVO = new EnvironmentVO();
        BeanUtils.copyProperties(environment, environmentVO);

        Project project = projectService.getById(environment.getProjectId());
        if (project != null) {
            environmentVO.setProjectName(project.getProjectName());
        }

        return environmentVO;
    }
}
