package com.atcontain.qaforge.controller;


import com.atcontain.qaforge.dto.*;
import com.atcontain.qaforge.entity.ApiAssertion;
import com.atcontain.qaforge.entity.ApiCase;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.ApiAssertionService;
import com.atcontain.qaforge.service.ApiCaseService;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.vo.ApiAssertionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api-assertion")
public class ApiAssertionController {

    private final ApiAssertionService apiAssertionService;
    private final ApiCaseService apiCaseService;
    private final ProjectService projectService;


    public ApiAssertionController(ApiAssertionService apiAssertionService, ApiCaseService apiCaseService,
                                  ProjectService projectService) {
        this.apiAssertionService = apiAssertionService;
        this.apiCaseService = apiCaseService;
        this.projectService = projectService;
    }

    @GetMapping("/list")
    public Result<List<ApiAssertionVO>> list(@RequestParam Integer caseId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiCase apiCase = apiCaseService.getById(caseId);
        if (apiCase == null) {
            return Result.error(404, "Case not found");
        }

        Project project = projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, apiCase.getProjectId())
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );

        if (project == null) {
            return Result.error(404, "Case not found");
        }

        List<ApiAssertion> assertions = apiAssertionService.list(
                new LambdaQueryWrapper<ApiAssertion>()
                        .eq(ApiAssertion::getCaseId, caseId)
                        .eq(ApiAssertion::getStatus, 1)
                        .orderByAsc(ApiAssertion::getSortOrder)
                        .orderByDesc(ApiAssertion::getCreateTime)
        );

        List<ApiAssertionVO> voList = assertions.stream().map(assertion -> {
            ApiAssertionVO vo = new ApiAssertionVO();
            BeanUtils.copyProperties(assertion, vo);
            return vo;
        }).toList();

        return Result.success(voList);
    }

    @PostMapping("/add")
    public Result<Void> addApiAssertion(@RequestBody @Valid ApiAssertionAddDTO apiAssertionAddDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiCase apiCase = apiCaseService.getOne(new LambdaQueryWrapper<ApiCase>()
                .eq(ApiCase::getId, apiAssertionAddDTO.getCaseId())
                .eq(ApiCase::getStatus, 1));
        if (apiCase == null) {
            return Result.error(404, "Case not found");
        }
        Project project = projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, apiCase.getProjectId())
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );
        if (project == null) {
            return Result.error(404, "Case not found");
        }
        ApiAssertion apiAssertion = new ApiAssertion();
        BeanUtils.copyProperties(apiAssertionAddDTO, apiAssertion);
        apiAssertion.setStatus(1);
        apiAssertion.setCreateBy(userId);
        apiAssertion.setUpdateBy(userId);
        apiAssertion.setCreateTime(LocalDateTime.now());
        apiAssertion.setUpdateTime(LocalDateTime.now());

        apiAssertionService.save(apiAssertion);

        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> updateApiAssertion(@RequestBody @Valid ApiAssertionUpdateDTO apiAssertionUpdateDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiAssertion apiAssertion = getOwnedApiAssertion(apiAssertionUpdateDTO.getId(), userId);
        if (apiAssertion == null) {
            return Result.error(404, "Assertion not found");
        }

        BeanUtils.copyProperties(apiAssertionUpdateDTO, apiAssertion);
        apiAssertion.setUpdateBy(userId);
        apiAssertion.setUpdateTime(LocalDateTime.now());

        apiAssertionService.updateById(apiAssertion);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteApiAssertion(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiAssertion assertion = getOwnedApiAssertion(id, userId);
        if (assertion == null) {
            return Result.error(404, "Assertion not found");
        }

        assertion.setUpdateBy(userId);
        assertion.setUpdateTime(LocalDateTime.now());
        apiAssertionService.updateById(assertion);

        apiAssertionService.removeById(id);
        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody @Valid StatusDTO statusDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiAssertion assertion = getOwnedApiAssertion(statusDTO.getId(), userId);
        if (assertion == null) {
            return Result.error(404, "Assertion not found");
        }
        assertion.setStatus(statusDTO.getStatus());
        assertion.setUpdateBy(userId);
        assertion.setUpdateTime(LocalDateTime.now());
        apiAssertionService.updateById(assertion);

        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDeleteAssertion(@RequestBody @Valid BatchDeleteDTO batchDeleteDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        List<ApiAssertion> assertions = new ArrayList<>();

        for (Integer id : batchDeleteDTO.getIds()) {
            ApiAssertion assertion = getOwnedApiAssertion(id, userId);
            if (assertion == null) {
                return Result.error(404, "Assertion not found");
            }

            assertion.setUpdateBy(userId);
            assertion.setUpdateTime(LocalDateTime.now());
            assertions.add(assertion);
        }

        apiAssertionService.updateBatchById(assertions);
        apiAssertionService.removeByIds(batchDeleteDTO.getIds());

        return Result.success();
    }



    private ApiAssertion getOwnedApiAssertion(Integer assertionId, Integer userId) {
        ApiAssertion assertion = apiAssertionService.getById(assertionId);
        if (assertion == null) {
            return null;
        }

        ApiCase apiCase = apiCaseService.getOne(
                new LambdaQueryWrapper<ApiCase>()
                        .eq(ApiCase::getId, assertion.getCaseId())
                        .eq(ApiCase::getStatus, 1)
        );
        if (apiCase == null) {
            return null;
        }

        Project project = projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, apiCase.getProjectId())
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );
        if (project == null) {
            return null;
        }

        return assertion;
    }
}
