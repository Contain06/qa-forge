package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.dto.PageResult;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.entity.ApiCase;
import com.atcontain.qaforge.entity.ApiInfo;
import com.atcontain.qaforge.entity.Environment;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.entity.RunDetail;
import com.atcontain.qaforge.entity.RunRecord;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.ApiCaseService;
import com.atcontain.qaforge.service.ApiInfoService;
import com.atcontain.qaforge.service.EnvironmentService;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.service.RunDetailService;
import com.atcontain.qaforge.service.RunRecordService;
import com.atcontain.qaforge.vo.RunDetailVO;
import com.atcontain.qaforge.vo.RunRecordDataVO;
import com.atcontain.qaforge.vo.RunRecordVO;
import com.atcontain.qaforge.vo.RunReportVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/run-record")
public class RunRecordController {

    private final RunRecordService runRecordService;
    private final RunDetailService runDetailService;
    private final ProjectService projectService;
    private final EnvironmentService environmentService;
    private final ApiCaseService apiCaseService;
    private final ApiInfoService apiInfoService;

    public RunRecordController(RunRecordService runRecordService,
                               RunDetailService runDetailService,
                               ProjectService projectService,
                               EnvironmentService environmentService,
                               ApiCaseService apiCaseService,
                               ApiInfoService apiInfoService) {
        this.runRecordService = runRecordService;
        this.runDetailService = runDetailService;
        this.projectService = projectService;
        this.environmentService = environmentService;
        this.apiCaseService = apiCaseService;
        this.apiInfoService = apiInfoService;
    }

    @GetMapping("/list")
    public Result<PageResult<RunRecordVO>> list(@RequestParam Integer projectId,
                                                @RequestParam(defaultValue = "1") Integer pageNum,
                                                @RequestParam(defaultValue = "10") Integer pageSize,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(required = false) String runType) {
        Integer userId = SecurityUtils.getCurrentUserId();

        Project project = projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, projectId)
                        .eq(Project::getStatus, 1)
                        .eq(Project::getOwnerId, userId)
        );
        if(project == null) {
            return Result.error(404, "Project not found");
        }

        Page<RunRecord> page = new Page<>(pageNum, pageSize);
        Page<RunRecord> result = runRecordService.page(
                page,
                new LambdaQueryWrapper<RunRecord>()
                        .eq(RunRecord::getProjectId, projectId)
                        .eq(StringUtils.hasText(status), RunRecord::getStatus, status)
                        .eq(StringUtils.hasText(runType), RunRecord::getRunType, runType)
                        .orderByDesc(RunRecord::getCreateTime)
        );
        log.info("实际的值为：{}，预期的值为：{}",runType, "PLAN / CASE");
        List<RunRecordVO> voList = result.getRecords().stream()
                .map(runRecord -> buildRunRecordVO(runRecord, project))
                .toList();

        PageResult<RunRecordVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());
        pageResult.setTotal((int) result.getTotal());
        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<RunReportVO> detail(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();

        RunRecord runRecord = runRecordService.getById(id);
        if (runRecord == null) {
            return Result.error(404, "Run record not found");
        }

        Project project = projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, runRecord.getProjectId())
                        .eq(Project::getStatus, 1)
                        .eq(Project::getOwnerId, userId)
        );
        if (project == null) {
            return Result.error(404, "Run record not found");
        }

        List<RunDetail> detailList = runDetailService.list(
                new LambdaQueryWrapper<RunDetail>()
                        .eq(RunDetail::getRunRecordId, id)
                        .orderByAsc(RunDetail::getId)
        );

        RunRecordVO recordVO = buildRunRecordVO(runRecord, project);
        List<RunDetailVO> detailVOList = detailList.stream()
                .map(this::buildRunDetailVO)
                .toList();

        RunReportVO runReportVO = RunReportVO.builder()
                .record(recordVO)
                .details(detailVOList)
                .build();

        return Result.success(runReportVO);
    }

    @GetMapping("/listData")
    public Result<RunRecordDataVO> listData(@RequestParam Integer projectId,
                                        @RequestParam(required = false) String status,
                                        @RequestParam(required = false) String runType) {
        RunRecordDataVO runRecordDataVO = runRecordService.listData(projectId, status, runType);
        if (runRecordDataVO == null) {
            runRecordDataVO = RunRecordDataVO.builder()
                    .totalCount(0L)
                    .successCount(0)
                    .failCount(0)
                    .errorCount(0)
                    .totalTime(0L)
                    .passRate(0.0)
                    .build();
        }
        return Result.success(runRecordDataVO);
    }

    private RunRecordVO buildRunRecordVO(RunRecord runRecord, Project project) {
        RunRecordVO runRecordVO = new RunRecordVO();
        BeanUtils.copyProperties(runRecord, runRecordVO);
        runRecordVO.setProjectName(project.getProjectName());

        Environment environment = environmentService.getById(runRecord.getEnvironmentId());
        if (environment != null) {
            runRecordVO.setEnvName(environment.getEnvName());
        }

        return runRecordVO;
    }

    private RunDetailVO buildRunDetailVO(RunDetail runDetail) {
        RunDetailVO runDetailVO = new RunDetailVO();
        BeanUtils.copyProperties(runDetail, runDetailVO);

        Project project = projectService.getById(runDetail.getProjectId());
        if (project != null) {
            runDetailVO.setProjectName(project.getProjectName());
        }

        Environment environment = environmentService.getById(runDetail.getEnvironmentId());
        if (environment != null) {
            runDetailVO.setEnvName(environment.getEnvName());
        }

        ApiCase apiCase = apiCaseService.getById(runDetail.getCaseId());
        if (apiCase != null) {
            runDetailVO.setCaseName(apiCase.getCaseName());
        }

        ApiInfo apiInfo = apiInfoService.getById(runDetail.getApiId());
        if (apiInfo != null) {
            runDetailVO.setApiName(apiInfo.getApiName());
        }

        return runDetailVO;
    }
}
