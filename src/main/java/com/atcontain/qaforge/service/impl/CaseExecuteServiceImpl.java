package com.atcontain.qaforge.service.impl;

import com.atcontain.qaforge.builder.HttpRequestBuilder;
import com.atcontain.qaforge.dto.HttpExecuteRequest;
import com.atcontain.qaforge.dto.HttpExecuteResult;
import com.atcontain.qaforge.entity.*;
import com.atcontain.qaforge.executor.AssertionExecutor;
import com.atcontain.qaforge.executor.ExtractExecutor;
import com.atcontain.qaforge.executor.HttpExecutor;
import com.atcontain.qaforge.service.*;
import com.atcontain.qaforge.vo.CaseExecuteResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CaseExecuteServiceImpl implements CaseExecuteService {

    private final ApiCaseService apiCaseService;
    private final ApiInfoService apiInfoService;
    private final EnvironmentService environmentService;
    private final ProjectService projectService;
    private final ApiAssertionService apiAssertionService;
    private final ApiExtractService apiExtractService;
    private final EnvVariableService envVariableService;
    private final HttpRequestBuilder requestBuilder;
    private final HttpExecutor httpExecutor;
    private final AssertionExecutor assertionExecutor;
    private final ExtractExecutor extractExecutor;
    private final RunRecordService runRecordService;
    private final RunDetailService runDetailService;


    public CaseExecuteServiceImpl(ApiCaseService apiCaseService,
                                  ApiInfoService apiInfoService,
                                  EnvironmentService environmentService,
                                  ProjectService projectService,
                                  ApiAssertionService apiAssertionService,
                                  ApiExtractService apiExtractService,
                                  EnvVariableService envVariableService,
                                  HttpRequestBuilder requestBuilder,
                                  HttpExecutor httpExecutor,
                                  AssertionExecutor assertionExecutor,
                                  ExtractExecutor extractExecutor,
                                  RunRecordService runRecordService,
                                  RunDetailService runDetailService) {
        this.apiCaseService = apiCaseService;
        this.apiInfoService = apiInfoService;
        this.environmentService = environmentService;
        this.projectService = projectService;
        this.apiAssertionService = apiAssertionService;
        this.apiExtractService = apiExtractService;
        this.envVariableService = envVariableService;
        this.requestBuilder = requestBuilder;
        this.httpExecutor = httpExecutor;
        this.assertionExecutor = assertionExecutor;
        this.extractExecutor = extractExecutor;
        this.runRecordService = runRecordService;
        this.runDetailService = runDetailService;
    }


    @Override
    public CaseExecuteResultVO execute(Integer caseId, Integer envId, Integer userId) {
        ApiCase apiCase = apiCaseService.getOne(
                new LambdaQueryWrapper<ApiCase>()
                        .eq(ApiCase::getId, caseId)
                        .eq(ApiCase::getStatus, 1)
        );
        if(apiCase == null) {
            throw new IllegalArgumentException("Case not found");
        }
        ApiInfo apiInfo = apiInfoService.getOne(
                new LambdaQueryWrapper<ApiInfo>()
                        .eq(ApiInfo::getId, apiCase.getApiId())
                        .eq(ApiInfo::getStatus, 1)
        );
        if(apiInfo == null) {
            throw new IllegalArgumentException("Api info not found");
        }
        Environment environment = environmentService.getOne(
                new LambdaQueryWrapper<Environment>()
                        .eq(Environment::getId, envId)
                        .eq(Environment::getStatus, 1)
        );
        if(environment == null) {
            throw new IllegalArgumentException("Environment not found");
        }
        Project project = projectService.getOne(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getId, apiCase.getProjectId())
                        .eq(Project::getOwnerId, userId)
                        .eq(Project::getStatus, 1)
        );
        if(project == null) {
            throw new IllegalArgumentException("Project not found");
        }
        List<ApiAssertion> assertions = apiAssertionService.list(
                new LambdaQueryWrapper<ApiAssertion>()
                        .eq(ApiAssertion::getCaseId, caseId)
                        .eq(ApiAssertion::getStatus, 1)
                        .orderByAsc(ApiAssertion::getSortOrder)
        );

        int assertionTotal = 0;
        int assertionPass = 0;
        int assertionFail = 0;

        assertionTotal = assertions == null ? 0 : assertions.size();


        HttpExecuteRequest build = requestBuilder.build(environment, apiInfo, apiCase);
        HttpExecuteResult execute = httpExecutor.execute(build);


        if(assertions != null && !assertions.isEmpty()) {
            for(ApiAssertion assertion : assertions) {
                log.info("执行断言：{}", assertion);
                boolean pass = assertionExecutor.execute(assertion, execute.getResponseBody(), execute.getStatusCode());
                if(pass) {
                    assertionPass ++;
                }else {
                    assertionFail ++;
                }
            }

        }
        // ========== 响应数据提取 ==========
        String extractErrorMessage = null;
        try {
            List<ApiExtract> extracts = apiExtractService.list(
                    new LambdaQueryWrapper<ApiExtract>()
                            .eq(ApiExtract::getCaseId, caseId)
                            .eq(ApiExtract::getStatus, 1)
                            .orderByAsc(ApiExtract::getSortOrder)
            );
            if (extracts != null && !extracts.isEmpty()) {
                Map<String, String> extracted = extractExecutor.execute(extracts, execute);
                // 回写到环境变量表
                for (Map.Entry<String, String> entry : extracted.entrySet()) {
                    upsertEnvVariable(apiCase.getProjectId(), envId,
                            entry.getKey(), entry.getValue(), userId);
                }
                log.info("成功提取并存储 {} 个变量", extracted.size());
            }
        } catch (RuntimeException e) {
            extractErrorMessage = e.getMessage();
            log.error("提取执行失败: {}", e.getMessage());
        }
        // ====================================

        boolean requestSuccess = Boolean.TRUE.equals(execute.getSuccess());
        boolean requestError = !requestSuccess;
        boolean httpSuccess = isHttpSuccess(execute);
        boolean pass = httpSuccess && assertionFail == 0 && extractErrorMessage == null;
        String errorMessage = pass ? null
                : (extractErrorMessage != null ? extractErrorMessage
                    : buildExecuteErrorMessage(execute, assertionTotal, assertionFail));

        LocalDateTime now = LocalDateTime.now();

        RunRecord runRecord = new RunRecord();
        runRecord.setProjectId(apiCase.getProjectId());
        runRecord.setEnvironmentId(environment.getId());
        runRecord.setCaseId(apiCase.getId());
        runRecord.setRunType("CASE");
        runRecord.setTotalCount(1);
        runRecord.setSuccessCount(pass ? 1 : 0);
        runRecord.setFailCount(!pass && !requestError ? 1 : 0);
        runRecord.setTimeoutCount(0);
        runRecord.setErrorCount(requestError ? 1 : 0);
        runRecord.setPassRate(pass ? 100.0 : 0.0);
        runRecord.setTotalTime(execute.getDurationMs() == null ? 0 : execute.getDurationMs().intValue());
        runRecord.setStatus(pass ? "PASS" : "FAIL");
        runRecord.setStartTime(now);
        runRecord.setEndTime(now);
        runRecord.setCreateBy(userId);
        runRecord.setUpdateBy(userId);
        runRecord.setCreateTime(now);
        runRecord.setUpdateTime(now);

        runRecordService.save(runRecord);

        RunDetail runDetail = new RunDetail();
        runDetail.setRunRecordId(runRecord.getId());
        runDetail.setProjectId(apiCase.getProjectId());
        runDetail.setEnvironmentId(environment.getId());
        runDetail.setCaseId(apiCase.getId());
        runDetail.setApiId(apiInfo.getId());

        runDetail.setRequestUrl(execute.getRequestUrl());
        runDetail.setRequestMethod(execute.getRequestMethod());
        runDetail.setRequestHeaders(execute.getRequestHeaders());
        runDetail.setRequestBody(execute.getRequestBody());
        runDetail.setRequestParams(build.getParams());

        runDetail.setResponseStatus(execute.getStatusCode());
        runDetail.setResponseHeaders(execute.getResponseHeaders());
        runDetail.setResponseBody(execute.getResponseBody());
        runDetail.setResponseTime(execute.getDurationMs() == null ? 0 : execute.getDurationMs().intValue());

        runDetail.setAssertResult(pass ? "PASS" : "FAIL");
        runDetail.setErrorMessage(errorMessage);
        runDetail.setStatus(pass ? "PASS" : "FAIL");

        runDetail.setCreateBy(userId);
        runDetail.setUpdateBy(userId);
        runDetail.setCreateTime(now);
        runDetail.setUpdateTime(now);

        runDetailService.save(runDetail);

        return CaseExecuteResultVO.builder()
                .caseId(apiCase.getId())
                .caseName(apiCase.getCaseName())
                .apiId(apiInfo.getId())
                .apiName(apiInfo.getApiName())
                .envId(environment.getId())
                .envName(environment.getEnvName())
                .pass(pass)
                .statusCode(execute.getStatusCode())
                .responseBody(execute.getResponseBody())
                .assertionTotal(assertionTotal)
                .assertionPass(assertionPass)
                .assertionFail(assertionFail)
                .durationMs(execute.getDurationMs())
                .errorMessage(errorMessage)
                .runRecordId(runRecord.getId())
                .build();
    }

    private boolean isHttpSuccess(HttpExecuteResult execute) {
        Integer statusCode = execute.getStatusCode();
        return Boolean.TRUE.equals(execute.getSuccess())
                && statusCode != null
                && statusCode >= 200
                && statusCode < 300;
    }

    private String buildExecuteErrorMessage(HttpExecuteResult execute, int assertionTotal, int assertionFail) {
        if (!Boolean.TRUE.equals(execute.getSuccess())) {
            String message = execute.getErrorMessage();
            return message == null || message.isBlank() ? "HTTP request failed" : message;
        }
        if (!isHttpSuccess(execute)) {
            Integer statusCode = execute.getStatusCode();
            return statusCode == null ? "HTTP response status code is empty" : "HTTP status code is " + statusCode;
        }
        if (assertionFail > 0) {
            return "Assertion failed: " + assertionFail + "/" + assertionTotal;
        }
        return null;
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
            log.info("更新环境变量 [{}] = {}", key, value);
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
            log.info("创建环境变量 [{}] = {}", key, value);
        }
    }
}
