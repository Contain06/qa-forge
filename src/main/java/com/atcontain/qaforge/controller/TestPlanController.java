package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.builder.HttpRequestBuilder;
import com.atcontain.qaforge.dto.HttpExecuteRequest;
import com.atcontain.qaforge.dto.HttpExecuteResult;
import com.atcontain.qaforge.dto.PageResult;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.dto.StatusDTO;
import com.atcontain.qaforge.dto.TestPlanAddDTO;
import com.atcontain.qaforge.dto.TestPlanBindCasesDTO;
import com.atcontain.qaforge.dto.TestPlanUpdateDTO;
import com.atcontain.qaforge.entity.ApiAssertion;
import com.atcontain.qaforge.entity.ApiCase;
import com.atcontain.qaforge.entity.ApiExtract;
import com.atcontain.qaforge.entity.ApiInfo;
import com.atcontain.qaforge.entity.EnvVariable;
import com.atcontain.qaforge.entity.Environment;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.entity.RunDetail;
import com.atcontain.qaforge.entity.RunRecord;
import com.atcontain.qaforge.entity.TestPlanCase;
import com.atcontain.qaforge.entity.TestPlan;
import com.atcontain.qaforge.executor.AssertionExecutor;
import com.atcontain.qaforge.executor.ExtractExecutor;
import com.atcontain.qaforge.executor.HttpExecutor;
import com.atcontain.qaforge.mapper.TestCasePlanMapper;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.ApiAssertionService;
import com.atcontain.qaforge.service.ApiCaseService;
import com.atcontain.qaforge.service.ApiExtractService;
import com.atcontain.qaforge.service.ApiInfoService;
import com.atcontain.qaforge.service.EnvVariableService;
import com.atcontain.qaforge.service.EnvironmentService;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.service.RunDetailService;
import com.atcontain.qaforge.service.RunRecordService;
import com.atcontain.qaforge.service.TestCasePlanService;
import com.atcontain.qaforge.service.TestPlanService;
import com.atcontain.qaforge.vo.ApiCaseVO;
import com.atcontain.qaforge.vo.RunDetailVO;
import com.atcontain.qaforge.vo.RunRecordVO;
import com.atcontain.qaforge.vo.RunReportVO;
import com.atcontain.qaforge.vo.TestPlanDetailVO;
import com.atcontain.qaforge.vo.TestPlanVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/test-plan")
public class TestPlanController {

    private final TestPlanService testPlanService;
    private final TestCasePlanService testCasePlanService;
    private final TestCasePlanMapper testCasePlanMapper;
    private final ProjectService projectService;
    private final EnvironmentService environmentService;
    private final ApiCaseService apiCaseService;
    private final ApiInfoService apiInfoService;
    private final ApiAssertionService apiAssertionService;
    private final ApiExtractService apiExtractService;
    private final EnvVariableService envVariableService;
    private final RunRecordService runRecordService;
    private final RunDetailService runDetailService;
    private final HttpRequestBuilder requestBuilder;
    private final HttpExecutor httpExecutor;
    private final AssertionExecutor assertionExecutor;
    private final ExtractExecutor extractExecutor;

    public TestPlanController(TestPlanService testPlanService,
                              TestCasePlanService testCasePlanService,
                              TestCasePlanMapper testCasePlanMapper,
                              ProjectService projectService,
                              EnvironmentService environmentService,
                              ApiCaseService apiCaseService,
                              ApiInfoService apiInfoService,
                              ApiAssertionService apiAssertionService,
                              ApiExtractService apiExtractService,
                              EnvVariableService envVariableService,
                              RunRecordService runRecordService,
                              RunDetailService runDetailService,
                              HttpRequestBuilder requestBuilder,
                              HttpExecutor httpExecutor,
                              AssertionExecutor assertionExecutor,
                              ExtractExecutor extractExecutor) {
        this.testPlanService = testPlanService;
        this.testCasePlanService = testCasePlanService;
        this.testCasePlanMapper = testCasePlanMapper;
        this.projectService = projectService;
        this.environmentService = environmentService;
        this.apiCaseService = apiCaseService;
        this.apiInfoService = apiInfoService;
        this.apiAssertionService = apiAssertionService;
        this.apiExtractService = apiExtractService;
        this.envVariableService = envVariableService;
        this.runRecordService = runRecordService;
        this.runDetailService = runDetailService;
        this.requestBuilder = requestBuilder;
        this.httpExecutor = httpExecutor;
        this.assertionExecutor = assertionExecutor;
        this.extractExecutor = extractExecutor;
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid TestPlanAddDTO testPlanAddDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = getOwnedProject(testPlanAddDTO.getProjectId(), userId);
        if (project == null) {
            return Result.error(404, "Project not found");
        }

        Environment environment = getOwnedEnvironment(
                testPlanAddDTO.getEnvironmentId(),
                testPlanAddDTO.getProjectId()
        );
        if (environment == null) {
            return Result.error(404, "Environment not found");
        }

        LocalDateTime now = LocalDateTime.now();
        TestPlan testPlan = new TestPlan();
        BeanUtils.copyProperties(testPlanAddDTO, testPlan);
        testPlan.setStatus(1);
        testPlan.setCreateBy(userId);
        testPlan.setUpdateBy(userId);
        testPlan.setCreateTime(now);
        testPlan.setUpdateTime(now);
        testPlanService.save(testPlan);

        return Result.success();
    }

    @GetMapping("/list")
    public Result<PageResult<TestPlanVO>> list(@RequestParam Integer projectId,
                                               @RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize,
                                               @RequestParam(required = false) String planName,
                                               @RequestParam(required = false) Integer status) {
        Integer userId = SecurityUtils.getCurrentUserId();
        Project project = getOwnedProject(projectId, userId);
        if (project == null) {
            return Result.error(404, "Project not found");
        }

        Page<TestPlan> page = new Page<>(pageNum, pageSize);
        Page<TestPlan> result = testPlanService.page(
                page,
                new LambdaQueryWrapper<TestPlan>()
                        .eq(TestPlan::getProjectId, projectId)
                        .like(StringUtils.hasText(planName), TestPlan::getPlanName, planName)
                        .eq(status != null, TestPlan::getStatus, status)
                        .orderByDesc(TestPlan::getCreateTime)
        );

        List<TestPlanVO> voList = result.getRecords().stream()
                .map(testPlan -> buildTestPlanVO(testPlan, project))
                .toList();

        PageResult<TestPlanVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal((int) result.getTotal());
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());

        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<TestPlanDetailVO> detail(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        TestPlan testPlan = getOwnedTestPlan(id, userId);
        if (testPlan == null) {
            return Result.error(404, "Test plan not found");
        }

        TestPlanDetailVO detailVO = TestPlanDetailVO.builder()
                .plan(buildTestPlanVO(testPlan))
                .cases(getPlanCaseVOList(testPlan.getId()))
                .build();

        return Result.success(detailVO);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid TestPlanUpdateDTO testPlanUpdateDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        TestPlan oldTestPlan = getOwnedTestPlan(testPlanUpdateDTO.getId(), userId);
        if (oldTestPlan == null) {
            return Result.error(404, "Test plan not found");
        }

        Environment environment = getOwnedEnvironment(
                testPlanUpdateDTO.getEnvironmentId(),
                oldTestPlan.getProjectId()
        );
        if (environment == null) {
            return Result.error(404, "Environment not found");
        }

        TestPlan testPlan = new TestPlan();
        BeanUtils.copyProperties(testPlanUpdateDTO, testPlan);
        testPlan.setProjectId(oldTestPlan.getProjectId());
        testPlan.setUpdateBy(userId);
        testPlan.setUpdateTime(LocalDateTime.now());
        testPlanService.updateById(testPlan);

        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        TestPlan testPlan = getOwnedTestPlan(id, userId);
        if (testPlan == null) {
            return Result.error(404, "Test plan not found");
        }

        testPlan.setUpdateBy(userId);
        testPlan.setUpdateTime(LocalDateTime.now());
        testPlanService.updateById(testPlan);
        testPlanService.removeById(id);

        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody @Valid StatusDTO statusDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        TestPlan oldTestPlan = getOwnedTestPlan(statusDTO.getId(), userId);
        if (oldTestPlan == null) {
            return Result.error(404, "Test plan not found");
        }

        TestPlan testPlan = new TestPlan();
        testPlan.setId(statusDTO.getId());
        testPlan.setStatus(statusDTO.getStatus());
        testPlan.setUpdateBy(userId);
        testPlan.setUpdateTime(LocalDateTime.now());
        testPlanService.updateById(testPlan);

        return Result.success();
    }

    @Transactional
    @PostMapping("/bind-cases")
    public Result<Void> bindCases(@RequestBody @Valid TestPlanBindCasesDTO bindCasesDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        TestPlan testPlan = getOwnedTestPlan(bindCasesDTO.getPlanId(), userId);
        if (testPlan == null) {
            return Result.error(404, "Test plan not found");
        }

        List<Integer> caseIds = new ArrayList<>(new LinkedHashSet<>(bindCasesDTO.getCaseIds()));
        List<TestPlanCase> relations = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < caseIds.size(); i++) {
            ApiCase apiCase = getOwnedApiCase(caseIds.get(i), testPlan.getProjectId());
            if (apiCase == null) {
                return Result.error(404, "Case not found");
            }

            TestPlanCase relation = new TestPlanCase();
            relation.setPlanId(testPlan.getId());
            relation.setCaseId(apiCase.getId());
            relation.setSortOrder(i + 1);
            relation.setCreateBy(userId);
            relation.setUpdateBy(userId);
            relation.setCreateTime(now);
            relation.setUpdateTime(now);
            relations.add(relation);
        }

        // 物理删除旧绑定：@TableLogic 的逻辑删除会导致唯一键冲突
        testCasePlanMapper.physicalDeleteByPlanId(testPlan.getId());
        testCasePlanService.saveBatch(relations);

        return Result.success();
    }

    @DeleteMapping("/remove-case")
    public Result<Void> removeCase(@RequestParam Integer planId,
                                   @RequestParam Integer caseId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        TestPlan testPlan = getOwnedTestPlan(planId, userId);
        if (testPlan == null) {
            return Result.error(404, "Test plan not found");
        }

        testCasePlanService.remove(
                new LambdaQueryWrapper<TestPlanCase>()
                        .eq(TestPlanCase::getPlanId, planId)
                        .eq(TestPlanCase::getCaseId, caseId)
        );

        return Result.success();
    }

    @GetMapping("/cases/{planId}")
    public Result<List<ApiCaseVO>> cases(@PathVariable Integer planId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        TestPlan testPlan = getOwnedTestPlan(planId, userId);
        if (testPlan == null) {
            return Result.error(404, "Test plan not found");
        }

        return Result.success(getPlanCaseVOList(planId));
    }

    @Transactional
    @PostMapping("/execute/{planId}")
    public Result<RunReportVO> execute(@PathVariable Integer planId,
                                       @RequestParam(required = false) Integer envId) {
        Integer userId = SecurityUtils.getCurrentUserId();

        TestPlan testPlan = getOwnedTestPlan(planId, userId);
        if (testPlan == null || testPlan.getStatus() == null || testPlan.getStatus() != 1) {
            return Result.error(404, "Test plan not found");
        }

        Integer executeEnvId = envId == null ? testPlan.getEnvironmentId() : envId;
        Environment environment = getOwnedEnvironment(executeEnvId, testPlan.getProjectId());
        if (environment == null) {
            return Result.error(404, "Environment not found");
        }

        List<TestPlanCase> relations = testCasePlanService.list(
                new LambdaQueryWrapper<TestPlanCase>()
                        .eq(TestPlanCase::getPlanId, planId)
                        .orderByAsc(TestPlanCase::getSortOrder)
        );
        if (relations.isEmpty()) {
            return Result.error(400, "Test plan has no cases");
        }

        LocalDateTime startTime = LocalDateTime.now();
        RunRecord runRecord = new RunRecord();
        runRecord.setProjectId(testPlan.getProjectId());
        runRecord.setEnvironmentId(environment.getId());
        runRecord.setPlanId(testPlan.getId());
        runRecord.setCaseId(0);
        runRecord.setRunType("PLAN");
        runRecord.setTotalCount(relations.size());
        runRecord.setSuccessCount(0);
        runRecord.setFailCount(0);
        runRecord.setTimeoutCount(0);
        runRecord.setErrorCount(0);
        runRecord.setPassRate(0.0);
        runRecord.setTotalTime(0);
        runRecord.setStatus("RUNNING");
        runRecord.setStartTime(startTime);
        runRecord.setCreateBy(userId);
        runRecord.setUpdateBy(userId);
        runRecord.setCreateTime(startTime);
        runRecord.setUpdateTime(startTime);
        runRecordService.save(runRecord);

        int successCount = 0;
        int failCount = 0;
        int errorCount = 0;
        int totalTime = 0;
        List<RunDetail> runDetails = new ArrayList<>();

        for (TestPlanCase relation : relations) {
            RunDetail runDetail = executePlanCase(
                    runRecord.getId(),
                    testPlan,
                    environment,
                    relation.getCaseId(),
                    userId
            );

            runDetails.add(runDetail);
            totalTime += runDetail.getResponseTime() == null ? 0 : runDetail.getResponseTime();

            if ("PASS".equals(runDetail.getStatus())) {
                successCount++;
            } else {
                failCount++;
                if (StringUtils.hasText(runDetail.getErrorMessage())) {
                    errorCount++;
                }
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        runRecord.setSuccessCount(successCount);
        runRecord.setFailCount(failCount);
        runRecord.setErrorCount(errorCount);
        runRecord.setPassRate(relations.isEmpty() ? 0.0 : successCount * 100.0 / relations.size());
        runRecord.setTotalTime(totalTime);
        runRecord.setStatus(failCount == 0 ? "PASS" : "FAIL");
        runRecord.setEndTime(endTime);
        runRecord.setUpdateBy(userId);
        runRecord.setUpdateTime(endTime);
        runRecordService.updateById(runRecord);

        RunRecordVO recordVO = buildRunRecordVO(runRecord);
        List<RunDetailVO> detailVOList = runDetails.stream()
                .map(this::buildRunDetailVO)
                .toList();

        RunReportVO reportVO = RunReportVO.builder()
                .record(recordVO)
                .details(detailVOList)
                .build();

        return Result.success(reportVO);
    }

    private RunDetail executePlanCase(Integer runRecordId,
                                      TestPlan testPlan,
                                      Environment environment,
                                      Integer caseId,
                                      Integer userId) {
        LocalDateTime now = LocalDateTime.now();
        RunDetail runDetail = new RunDetail();
        runDetail.setRunRecordId(runRecordId);
        runDetail.setProjectId(testPlan.getProjectId());
        runDetail.setEnvironmentId(environment.getId());
        runDetail.setPlanId(testPlan.getId());
        runDetail.setCaseId(caseId);
        runDetail.setCreateBy(userId);
        runDetail.setUpdateBy(userId);
        runDetail.setCreateTime(now);
        runDetail.setUpdateTime(now);

        try {
            ApiCase apiCase = getOwnedApiCase(caseId, testPlan.getProjectId());
            if (apiCase == null) {
                fillFailedRunDetail(runDetail, "Case not found");
                runDetailService.save(runDetail);
                return runDetail;
            }

            ApiInfo apiInfo = getOwnedApiInfo(apiCase.getApiId(), testPlan.getProjectId());
            if (apiInfo == null) {
                runDetail.setApiId(apiCase.getApiId());
                fillFailedRunDetail(runDetail, "API not found");
                runDetailService.save(runDetail);
                return runDetail;
            }

            runDetail.setApiId(apiInfo.getId());

            List<ApiAssertion> assertions = apiAssertionService.list(
                    new LambdaQueryWrapper<ApiAssertion>()
                            .eq(ApiAssertion::getCaseId, apiCase.getId())
                            .eq(ApiAssertion::getStatus, 1)
                            .orderByAsc(ApiAssertion::getSortOrder)
            );

            HttpExecuteRequest request = requestBuilder.build(environment, apiInfo, apiCase);
            HttpExecuteResult httpResult = httpExecutor.execute(request);

            int assertionFail = 0;
            for (ApiAssertion assertion : assertions) {
                boolean assertionPass = assertionExecutor.execute(
                        assertion,
                        httpResult.getResponseBody(),
                        httpResult.getStatusCode()
                );
                if (!assertionPass) {
                    assertionFail++;
                }
            }

            // ========== 响应数据提取 ==========
            String extractErrorMessage = null;
            try {
                List<ApiExtract> extracts = apiExtractService.list(
                        new LambdaQueryWrapper<ApiExtract>()
                                .eq(ApiExtract::getCaseId, apiCase.getId())
                                .eq(ApiExtract::getStatus, 1)
                                .orderByAsc(ApiExtract::getSortOrder)
                );
                if (extracts != null && !extracts.isEmpty()) {
                    Map<String, String> extracted = extractExecutor.execute(extracts, httpResult);
                    for (Map.Entry<String, String> entry : extracted.entrySet()) {
                        upsertEnvVariable(testPlan.getProjectId(), environment.getId(),
                                entry.getKey(), entry.getValue(), userId);
                    }
                }
            } catch (RuntimeException e) {
                extractErrorMessage = e.getMessage();
            }
            // ====================================

            boolean httpSuccess = Boolean.TRUE.equals(httpResult.getSuccess());
            boolean pass = httpSuccess && assertionFail == 0 && extractErrorMessage == null;

            runDetail.setRequestUrl(httpResult.getRequestUrl());
            runDetail.setRequestMethod(httpResult.getRequestMethod());
            runDetail.setRequestHeaders(httpResult.getRequestHeaders());
            runDetail.setRequestParams(request.getParams());
            runDetail.setRequestBody(httpResult.getRequestBody());
            runDetail.setResponseStatus(httpResult.getStatusCode());
            runDetail.setResponseHeaders(httpResult.getResponseHeaders());
            runDetail.setResponseBody(httpResult.getResponseBody());
            runDetail.setResponseTime(httpResult.getDurationMs() == null ? 0 : httpResult.getDurationMs().intValue());
            runDetail.setAssertResult(pass ? "PASS" : "FAIL");
            runDetail.setErrorMessage(pass ? null
                    : (extractErrorMessage != null ? extractErrorMessage
                        : buildExecuteErrorMessage(httpResult, assertions.size(), assertionFail)));
            runDetail.setStatus(pass ? "PASS" : "FAIL");
        } catch (Exception e) {
            fillFailedRunDetail(runDetail, e.getMessage());
        }

        runDetailService.save(runDetail);
        return runDetail;
    }

    private String buildExecuteErrorMessage(HttpExecuteResult httpResult, int assertionTotal, int assertionFail) {
        if (!Boolean.TRUE.equals(httpResult.getSuccess())) {
            return httpResult.getErrorMessage();
        }
        if (assertionTotal == 0) {
            return "No assertion configured";
        }
        if (assertionFail > 0) {
            return "Assertion failed: " + assertionFail + "/" + assertionTotal;
        }
        return null;
    }

    private void fillFailedRunDetail(RunDetail runDetail, String errorMessage) {
        runDetail.setResponseTime(0);
        runDetail.setAssertResult("FAIL");
        runDetail.setStatus("FAIL");
        runDetail.setErrorMessage(errorMessage);
    }

    private Project getOwnedProject(Integer projectId, Integer userId) {
        return projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, projectId)
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );
    }

    private TestPlan getOwnedTestPlan(Integer planId, Integer userId) {
        TestPlan testPlan = testPlanService.getById(planId);
        if (testPlan == null) {
            return null;
        }

        Project project = getOwnedProject(testPlan.getProjectId(), userId);
        if (project == null) {
            return null;
        }

        return testPlan;
    }

    private Environment getOwnedEnvironment(Integer environmentId, Integer projectId) {
        return environmentService.getOne(
                new LambdaQueryWrapper<Environment>()
                        .eq(Environment::getId, environmentId)
                        .eq(Environment::getProjectId, projectId)
                        .eq(Environment::getStatus, 1)
        );
    }

    private ApiCase getOwnedApiCase(Integer caseId, Integer projectId) {
        return apiCaseService.getOne(
                new LambdaQueryWrapper<ApiCase>()
                        .eq(ApiCase::getId, caseId)
                        .eq(ApiCase::getProjectId, projectId)
                        .eq(ApiCase::getStatus, 1)
        );
    }

    private ApiInfo getOwnedApiInfo(Integer apiId, Integer projectId) {
        return apiInfoService.getOne(
                new LambdaQueryWrapper<ApiInfo>()
                        .eq(ApiInfo::getId, apiId)
                        .eq(ApiInfo::getProjectId, projectId)
                        .eq(ApiInfo::getStatus, 1)
        );
    }

    private TestPlanVO buildTestPlanVO(TestPlan testPlan) {
        Project project = projectService.getById(testPlan.getProjectId());
        return buildTestPlanVO(testPlan, project);
    }

    private TestPlanVO buildTestPlanVO(TestPlan testPlan, Project project) {
        TestPlanVO testPlanVO = new TestPlanVO();
        BeanUtils.copyProperties(testPlan, testPlanVO);

        if (project != null) {
            testPlanVO.setProjectName(project.getProjectName());
        }

        Environment environment = environmentService.getById(testPlan.getEnvironmentId());
        if (environment != null) {
            testPlanVO.setEnvName(environment.getEnvName());
        }

        long caseCount = testCasePlanService.count(
                new LambdaQueryWrapper<TestPlanCase>()
                        .eq(TestPlanCase::getPlanId, testPlan.getId())
        );
        testPlanVO.setCaseCount((int) caseCount);

        return testPlanVO;
    }

    private List<ApiCaseVO> getPlanCaseVOList(Integer planId) {
        List<TestPlanCase> relations = testCasePlanService.list(
                new LambdaQueryWrapper<TestPlanCase>()
                        .eq(TestPlanCase::getPlanId, planId)
                        .orderByAsc(TestPlanCase::getSortOrder)
        );

        return relations.stream()
                .map(relation -> apiCaseService.getById(relation.getCaseId()))
                .filter(apiCase -> apiCase != null)
                .map(this::buildApiCaseVO)
                .toList();
    }

    private ApiCaseVO buildApiCaseVO(ApiCase apiCase) {
        ApiCaseVO apiCaseVO = new ApiCaseVO();
        BeanUtils.copyProperties(apiCase, apiCaseVO);

        Project project = projectService.getById(apiCase.getProjectId());
        if (project != null) {
            apiCaseVO.setProjectName(project.getProjectName());
        }

        ApiInfo apiInfo = apiInfoService.getById(apiCase.getApiId());
        if (apiInfo != null) {
            apiCaseVO.setApiName(apiInfo.getApiName());
        }

        return apiCaseVO;
    }

    private RunRecordVO buildRunRecordVO(RunRecord runRecord) {
        RunRecordVO runRecordVO = new RunRecordVO();
        BeanUtils.copyProperties(runRecord, runRecordVO);

        Project project = projectService.getById(runRecord.getProjectId());
        if (project != null) {
            runRecordVO.setProjectName(project.getProjectName());
        }

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

    /**
     * 将提取到的变量值写入 env_variable 表（存在则更新，不存在则新增）。
     */
    private void upsertEnvVariable(Integer projectId, Integer envId,
                                   String key, String value, Integer userId) {
        EnvVariable existing = envVariableService.getOne(
                new LambdaQueryWrapper<EnvVariable>()
                        .eq(EnvVariable::getEnvironmentId, envId)
                        .eq(EnvVariable::getVariableKey, key)
        );
        LocalDateTime now = LocalDateTime.now();

        if (existing != null) {
            existing.setVariableValue(value);
            existing.setUpdateBy(userId);
            existing.setUpdateTime(now);
            envVariableService.updateById(existing);
        } else {
            EnvVariable newVar = new EnvVariable();
            newVar.setProjectId(projectId);
            newVar.setEnvironmentId(envId);
            newVar.setVariableKey(key);
            newVar.setVariableValue(value);
            newVar.setDescription("Auto-extracted from test execution");
            newVar.setStatus(1);
            newVar.setCreateBy(userId);
            newVar.setUpdateBy(userId);
            newVar.setCreateTime(now);
            newVar.setUpdateTime(now);
            envVariableService.save(newVar);
        }
    }
}
