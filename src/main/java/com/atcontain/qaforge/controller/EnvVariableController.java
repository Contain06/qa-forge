package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.dto.EnvVariableAddDTO;
import com.atcontain.qaforge.dto.EnvVariableUpdateDTO;
import com.atcontain.qaforge.dto.PageResult;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.dto.StatusDTO;
import com.atcontain.qaforge.entity.EnvVariable;
import com.atcontain.qaforge.entity.Environment;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.EnvVariableService;
import com.atcontain.qaforge.service.EnvironmentService;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.vo.EnvVariableVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
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
@RequestMapping("/env-variable")
public class EnvVariableController {

    private final EnvVariableService envVariableService;
    private final EnvironmentService environmentService;
    private final ProjectService projectService;

    public EnvVariableController(EnvVariableService envVariableService,
                                 EnvironmentService environmentService,
                                 ProjectService projectService) {
        this.envVariableService = envVariableService;
        this.environmentService = environmentService;
        this.projectService = projectService;
    }

    @GetMapping("/list")
    public Result<PageResult<EnvVariableVO>> list(@RequestParam Integer environmentId,
                                                  @RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                                  @RequestParam(required = false) String variableKey,
                                                  @RequestParam(defaultValue = "1") Integer status) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Environment environment = getOwnedEnvironment(environmentId, userId);
        if (environment == null) {
            return Result.error(404, "Environment not found");
        }

        Page<EnvVariable> page = new Page<>(pageNum, pageSize);
        Page<EnvVariable> result = envVariableService.page(
                page,
                new LambdaQueryWrapper<EnvVariable>()
                        .eq(EnvVariable::getProjectId, environment.getProjectId())
                        .eq(EnvVariable::getEnvironmentId, environmentId)
                        .like(StringUtils.hasText(variableKey), EnvVariable::getVariableKey, variableKey)
                        .eq(status != null, EnvVariable::getStatus, status)
                        .orderByDesc(EnvVariable::getCreateTime)
        );

        List<EnvVariableVO> voList = result.getRecords().stream()
                .map(this::buildEnvVariableVO)
                .toList();

        PageResult<EnvVariableVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal((int) result.getTotal());
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());

        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<EnvVariableVO> detail(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        EnvVariable envVariable = getOwnedEnvVariable(id, userId);
        if (envVariable == null) {
            return Result.error(404, "Variable not found");
        }

        return Result.success(buildEnvVariableVO(envVariable));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid EnvVariableAddDTO envVariableAddDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Environment environment = getOwnedEnvironment(envVariableAddDTO.getEnvironmentId(), userId);
        if (environment == null || !environment.getProjectId().equals(envVariableAddDTO.getProjectId())) {
            return Result.error(404, "Environment not found");
        }

        if (existsVariableKey(envVariableAddDTO.getEnvironmentId(), envVariableAddDTO.getVariableKey(), null)) {
            return Result.error(409, "Variable key already exists");
        }

        LocalDateTime now = LocalDateTime.now();
        EnvVariable envVariable = new EnvVariable();
        BeanUtils.copyProperties(envVariableAddDTO, envVariable);
        envVariable.setStatus(1);
        envVariable.setCreateBy(userId);
        envVariable.setUpdateBy(userId);
        envVariable.setCreateTime(now);
        envVariable.setUpdateTime(now);
        envVariableService.save(envVariable);

        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid EnvVariableUpdateDTO envVariableUpdateDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        EnvVariable oldEnvVariable = getOwnedEnvVariable(envVariableUpdateDTO.getId(), userId);
        if (oldEnvVariable == null) {
            return Result.error(404, "Variable not found");
        }

        if (existsVariableKey(
                oldEnvVariable.getEnvironmentId(),
                envVariableUpdateDTO.getVariableKey(),
                envVariableUpdateDTO.getId())) {
            return Result.error(409, "Variable key already exists");
        }

        EnvVariable envVariable = new EnvVariable();
        BeanUtils.copyProperties(envVariableUpdateDTO, envVariable);
        envVariable.setProjectId(oldEnvVariable.getProjectId());
        envVariable.setEnvironmentId(oldEnvVariable.getEnvironmentId());
        envVariable.setUpdateBy(userId);
        envVariable.setUpdateTime(LocalDateTime.now());
        envVariableService.updateById(envVariable);

        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        EnvVariable envVariable = getOwnedEnvVariable(id, userId);
        if (envVariable == null) {
            return Result.error(404, "Variable not found");
        }

        envVariable.setUpdateBy(userId);
        envVariable.setUpdateTime(LocalDateTime.now());
        envVariableService.updateById(envVariable);
        envVariableService.removeById(id);

        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody @Valid StatusDTO statusDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        EnvVariable oldEnvVariable = getOwnedEnvVariable(statusDTO.getId(), userId);
        if (oldEnvVariable == null) {
            return Result.error(404, "Variable not found");
        }

        EnvVariable envVariable = new EnvVariable();
        envVariable.setId(statusDTO.getId());
        envVariable.setStatus(statusDTO.getStatus());
        envVariable.setUpdateBy(userId);
        envVariable.setUpdateTime(LocalDateTime.now());
        envVariableService.updateById(envVariable);

        return Result.success();
    }

    private EnvVariable getOwnedEnvVariable(Integer id, Integer userId) {
        EnvVariable envVariable = envVariableService.getById(id);
        if (envVariable == null) {
            return null;
        }

        Environment environment = getOwnedEnvironment(envVariable.getEnvironmentId(), userId);
        if (environment == null || !environment.getProjectId().equals(envVariable.getProjectId())) {
            return null;
        }

        return envVariable;
    }

    private Environment getOwnedEnvironment(Integer environmentId, Integer userId) {
        Environment environment = environmentService.getOne(
                new LambdaQueryWrapper<Environment>()
                        .eq(Environment::getId, environmentId)
                        .eq(Environment::getStatus, 1)
        );
        if (environment == null) {
            return null;
        }

        Project project = getOwnedProject(environment.getProjectId(), userId);
        if (project == null) {
            return null;
        }

        return environment;
    }

    private Project getOwnedProject(Integer projectId, Integer userId) {
        return projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, projectId)
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );
    }

    private boolean existsVariableKey(Integer environmentId, String variableKey, Integer excludeId) {
        Long count = envVariableService.count(
                new LambdaQueryWrapper<EnvVariable>()
                        .eq(EnvVariable::getEnvironmentId, environmentId)
                        .eq(EnvVariable::getVariableKey, variableKey)
                        .ne(excludeId != null, EnvVariable::getId, excludeId)
        );
        return count != null && count > 0;
    }

    private EnvVariableVO buildEnvVariableVO(EnvVariable envVariable) {
        EnvVariableVO envVariableVO = new EnvVariableVO();
        BeanUtils.copyProperties(envVariable, envVariableVO);

        Project project = projectService.getById(envVariable.getProjectId());
        if (project != null) {
            envVariableVO.setProjectName(project.getProjectName());
        }

        Environment environment = environmentService.getById(envVariable.getEnvironmentId());
        if (environment != null) {
            envVariableVO.setEnvName(environment.getEnvName());
        }

        return envVariableVO;
    }
}
