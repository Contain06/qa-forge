package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.dto.ApiExtractAddDTO;
import com.atcontain.qaforge.dto.ApiExtractUpdateDTO;
import com.atcontain.qaforge.dto.BatchDeleteDTO;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.dto.StatusDTO;
import com.atcontain.qaforge.entity.ApiCase;
import com.atcontain.qaforge.entity.ApiExtract;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.ApiCaseService;
import com.atcontain.qaforge.service.ApiExtractService;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.vo.ApiExtractVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api-extract")
public class ApiExtractController {

    private final ApiExtractService apiExtractService;
    private final ApiCaseService apiCaseService;
    private final ProjectService projectService;

    public ApiExtractController(ApiExtractService apiExtractService,
                                ApiCaseService apiCaseService,
                                ProjectService projectService) {
        this.apiExtractService = apiExtractService;
        this.apiCaseService = apiCaseService;
        this.projectService = projectService;
    }

    /**
     * 查询某个用例下的所有提取规则。
     */
    @GetMapping("/list")
    public Result<List<ApiExtractVO>> list(@RequestParam Integer caseId) {
        Integer userId = SecurityUtils.getCurrentUserId();

        if (getOwnedApiCase(caseId, userId) == null) {
            return Result.error(404, "Case not found");
        }

        List<ApiExtract> extracts = apiExtractService.list(
                new LambdaQueryWrapper<ApiExtract>()
                        .eq(ApiExtract::getCaseId, caseId)
                        .eq(ApiExtract::getStatus, 1)
                        .orderByAsc(ApiExtract::getSortOrder)
                        .orderByDesc(ApiExtract::getCreateTime)
        );

        List<ApiExtractVO> voList = extracts.stream()
                .map(this::buildVO)
                .toList();

        return Result.success(voList);
    }

    @GetMapping("/detail/{id}")
    public Result<ApiExtractVO> detail(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiExtract extract = getOwnedApiExtract(id, userId);
        if (extract == null) {
            return Result.error(404, "Extract rule not found");
        }
        return Result.success(buildVO(extract));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid ApiExtractAddDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();

        if (getOwnedApiCase(dto.getCaseId(), userId) == null) {
            return Result.error(404, "Case not found");
        }

        LocalDateTime now = LocalDateTime.now();
        ApiExtract extract = new ApiExtract();
        BeanUtils.copyProperties(dto, extract);
        extract.setStatus(1);
        extract.setCreateBy(userId);
        extract.setUpdateBy(userId);
        extract.setCreateTime(now);
        extract.setUpdateTime(now);
        apiExtractService.save(extract);

        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid ApiExtractUpdateDTO dto) {
        Integer userId = SecurityUtils.getCurrentUserId();

        ApiExtract old = getOwnedApiExtract(dto.getId(), userId);
        if (old == null) {
            return Result.error(404, "Extract rule not found");
        }

        ApiExtract extract = new ApiExtract();
        BeanUtils.copyProperties(dto, extract);
        extract.setProjectId(old.getProjectId());
        extract.setCaseId(old.getCaseId());
        extract.setUpdateBy(userId);
        extract.setUpdateTime(LocalDateTime.now());
        apiExtractService.updateById(extract);

        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();

        ApiExtract extract = getOwnedApiExtract(id, userId);
        if (extract == null) {
            return Result.error(404, "Extract rule not found");
        }

        extract.setUpdateBy(userId);
        extract.setUpdateTime(LocalDateTime.now());
        apiExtractService.updateById(extract);
        apiExtractService.removeById(id);

        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody @Valid StatusDTO statusDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();

        ApiExtract old = getOwnedApiExtract(statusDTO.getId(), userId);
        if (old == null) {
            return Result.error(404, "Extract rule not found");
        }

        ApiExtract extract = new ApiExtract();
        extract.setId(statusDTO.getId());
        extract.setStatus(statusDTO.getStatus());
        extract.setUpdateBy(userId);
        extract.setUpdateTime(LocalDateTime.now());
        apiExtractService.updateById(extract);

        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody @Valid BatchDeleteDTO batchDeleteDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        List<ApiExtract> list = new ArrayList<>();

        for (Integer id : batchDeleteDTO.getIds()) {
            ApiExtract extract = getOwnedApiExtract(id, userId);
            if (extract == null) {
                return Result.error(404, "Extract rule not found");
            }
            extract.setUpdateBy(userId);
            extract.setUpdateTime(LocalDateTime.now());
            list.add(extract);
        }

        apiExtractService.updateBatchById(list);
        apiExtractService.removeByIds(batchDeleteDTO.getIds());

        return Result.success();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验提取规则归属：提取 → 用例 → 项目 → 用户所有权。
     */
    private ApiExtract getOwnedApiExtract(Integer extractId, Integer userId) {
        ApiExtract extract = apiExtractService.getById(extractId);
        if (extract == null) {
            return null;
        }
        if (getOwnedApiCase(extract.getCaseId(), userId) == null) {
            return null;
        }
        return extract;
    }

    /**
     * 校验用例归属：用例 → 项目 → 用户所有权。
     */
    private ApiCase getOwnedApiCase(Integer caseId, Integer userId) {
        ApiCase apiCase = apiCaseService.getOne(
                new LambdaQueryWrapper<ApiCase>()
                        .eq(ApiCase::getId, caseId)
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

        return apiCase;
    }

    private ApiExtractVO buildVO(ApiExtract extract) {
        ApiExtractVO vo = new ApiExtractVO();
        BeanUtils.copyProperties(extract, vo);

        Project project = projectService.getById(extract.getProjectId());
        if (project != null) {
            vo.setProjectName(project.getProjectName());
        }

        ApiCase apiCase = apiCaseService.getById(extract.getCaseId());
        if (apiCase != null) {
            vo.setCaseName(apiCase.getCaseName());
        }

        return vo;
    }
}
