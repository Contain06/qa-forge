package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.dto.ApiInfoAddDTO;
import com.atcontain.qaforge.dto.ApiInfoUpdateDTO;
import com.atcontain.qaforge.dto.PageResult;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.dto.StatusDTO;
import com.atcontain.qaforge.entity.ApiInfo;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.ApiInfoService;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.util.JsonPayloadUtils;
import com.atcontain.qaforge.vo.ApiInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
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
@RequestMapping("/api-info")
public class ApiInfoController {

    private final ApiInfoService apiInfoService;
    private final ProjectService projectService;

    public ApiInfoController(ApiInfoService apiInfoService, ProjectService projectService) {
        this.apiInfoService = apiInfoService;
        this.projectService = projectService;
    }

    @GetMapping("/list")
    public Result<PageResult<ApiInfoVO>> list(@RequestParam Integer projectId,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String apiName,
                                              @RequestParam(required = false) String requestMethod,
                                              @RequestParam(required = false) Integer status) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = getOwnedProject(projectId, userId);
        if (project == null) {
            return Result.error(404, "Project not found");
        }

        Page<ApiInfo> page = new Page<>(pageNum, pageSize);
        Page<ApiInfo> result = apiInfoService.page(
                page,
                new LambdaQueryWrapper<ApiInfo>()
                        .eq(ApiInfo::getProjectId, projectId)
                        .like(StringUtils.hasText(apiName), ApiInfo::getApiName, apiName)
                        .eq(StringUtils.hasText(requestMethod), ApiInfo::getRequestMethod, requestMethod)
                        .eq(status != null, ApiInfo::getStatus, status)
                        .orderByDesc(ApiInfo::getCreateTime)
        );

        List<ApiInfoVO> voList = result.getRecords().stream()
                .map(apiInfo -> buildApiInfoVO(apiInfo, project))
                .toList();

        PageResult<ApiInfoVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal((int) result.getTotal());
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());

        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<ApiInfoVO> detail(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiInfo apiInfo = getOwnedApiInfo(id, userId);
        if (apiInfo == null) {
            return Result.error(404, "API not found");
        }

        return Result.success(buildApiInfoVO(apiInfo));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid ApiInfoAddDTO apiInfoAddDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = getOwnedProject(apiInfoAddDTO.getProjectId(), userId);
        if (project == null) {
            return Result.error(404, "Project not found");
        }

        ApiInfo apiInfo = new ApiInfo();
        BeanUtils.copyProperties(apiInfoAddDTO, apiInfo);
        fillJsonContent(apiInfo, apiInfoAddDTO.getRequestHeaders(), apiInfoAddDTO.getRequestParams(),
                apiInfoAddDTO.getRequestBody());
        apiInfo.setStatus(1);
        apiInfo.setCreateBy(userId);
        apiInfo.setUpdateBy(userId);
        apiInfo.setCreateTime(LocalDateTime.now());
        apiInfo.setUpdateTime(LocalDateTime.now());
        apiInfoService.save(apiInfo);

        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid ApiInfoUpdateDTO apiInfoUpdateDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiInfo oldApiInfo = getOwnedApiInfo(apiInfoUpdateDTO.getId(), userId);
        if (oldApiInfo == null) {
            return Result.error(404, "API not found");
        }

        ApiInfo apiInfo = new ApiInfo();
        BeanUtils.copyProperties(apiInfoUpdateDTO, apiInfo);
        apiInfo.setRequestHeaders(JsonPayloadUtils.toJsonString(apiInfoUpdateDTO.getRequestHeaders(), oldApiInfo.getRequestHeaders()));
        apiInfo.setRequestParams(JsonPayloadUtils.toJsonString(apiInfoUpdateDTO.getRequestParams(), oldApiInfo.getRequestParams()));
        apiInfo.setRequestBody(JsonPayloadUtils.toJsonString(apiInfoUpdateDTO.getRequestBody(), oldApiInfo.getRequestBody()));
        apiInfo.setProjectId(oldApiInfo.getProjectId());
        apiInfo.setUpdateBy(userId);
        apiInfo.setUpdateTime(LocalDateTime.now());
        apiInfoService.updateById(apiInfo);

        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiInfo apiInfo = getOwnedApiInfo(id, userId);
        if (apiInfo == null) {
            return Result.error(404, "API not found");
        }

        apiInfo.setUpdateBy(userId);
        apiInfo.setUpdateTime(LocalDateTime.now());
        apiInfoService.updateById(apiInfo);
        apiInfoService.removeById(id);

        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody @Valid StatusDTO statusDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiInfo oldApiInfo = getOwnedApiInfo(statusDTO.getId(), userId);
        if (oldApiInfo == null) {
            return Result.error(404, "API not found");
        }

        ApiInfo apiInfo = new ApiInfo();
        apiInfo.setId(statusDTO.getId());
        apiInfo.setStatus(statusDTO.getStatus());
        apiInfo.setUpdateBy(userId);
        apiInfo.setUpdateTime(LocalDateTime.now());
        apiInfoService.updateById(apiInfo);

        return Result.success();
    }

    private ApiInfo getOwnedApiInfo(Integer apiInfoId, Integer userId) {
        ApiInfo apiInfo = apiInfoService.getById(apiInfoId);
        if (apiInfo == null) {
            return null;
        }

        Project project = getOwnedProject(apiInfo.getProjectId(), userId);
        if (project == null) {
            return null;
        }

        return apiInfo;
    }

    private Project getOwnedProject(Integer projectId, Integer userId) {
        return projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, projectId)
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );
    }

    private ApiInfoVO buildApiInfoVO(ApiInfo apiInfo) {
        Project project = projectService.getById(apiInfo.getProjectId());
        return buildApiInfoVO(apiInfo, project);
    }

    private ApiInfoVO buildApiInfoVO(ApiInfo apiInfo, Project project) {
        ApiInfoVO apiInfoVO = new ApiInfoVO();
        BeanUtils.copyProperties(apiInfo, apiInfoVO);
        apiInfoVO.setRequestHeaders(JsonPayloadUtils.toJsonNode(apiInfo.getRequestHeaders()));
        apiInfoVO.setRequestParams(JsonPayloadUtils.toJsonNode(apiInfo.getRequestParams()));
        apiInfoVO.setRequestBody(JsonPayloadUtils.toJsonNode(apiInfo.getRequestBody()));

        if (project != null) {
            apiInfoVO.setProjectName(project.getProjectName());
        }

        return apiInfoVO;
    }

    private void fillJsonContent(ApiInfo apiInfo, JsonNode requestHeaders, JsonNode requestParams, JsonNode requestBody) {
        apiInfo.setRequestHeaders(JsonPayloadUtils.toJsonString(requestHeaders));
        apiInfo.setRequestParams(JsonPayloadUtils.toJsonString(requestParams));
        apiInfo.setRequestBody(JsonPayloadUtils.toJsonString(requestBody));
    }
}
