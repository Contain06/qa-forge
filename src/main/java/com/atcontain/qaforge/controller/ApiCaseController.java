package com.atcontain.qaforge.controller;

import com.atcontain.qaforge.dto.ApiCaseAddDTO;
import com.atcontain.qaforge.dto.ApiCaseImportItemDTO;
import com.atcontain.qaforge.dto.ApiCaseUpdateDTO;
import com.atcontain.qaforge.dto.BatchDeleteDTO;
import com.atcontain.qaforge.dto.PageResult;
import com.atcontain.qaforge.dto.Result;
import com.atcontain.qaforge.dto.StatusDTO;
import com.atcontain.qaforge.entity.ApiCase;
import com.atcontain.qaforge.entity.ApiInfo;
import com.atcontain.qaforge.entity.Project;
import com.atcontain.qaforge.security.util.SecurityUtils;
import com.atcontain.qaforge.service.ApiCaseService;
import com.atcontain.qaforge.service.ApiInfoService;
import com.atcontain.qaforge.service.CaseExecuteService;
import com.atcontain.qaforge.service.ProjectService;
import com.atcontain.qaforge.util.JsonPayloadUtils;
import com.atcontain.qaforge.vo.ApiCaseVO;
import com.atcontain.qaforge.vo.CaseExecuteResultVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-case")
public class ApiCaseController {

    private final ApiCaseService apiCaseService;
    private final ApiInfoService apiInfoService;
    private final ProjectService projectService;
    private final CaseExecuteService caseExecuteService;
    private final ObjectMapper objectMapper;

    public ApiCaseController(ApiCaseService apiCaseService,
                             ApiInfoService apiInfoService,
                             ProjectService projectService,
                             CaseExecuteService caseExecuteService,
                             ObjectMapper objectMapper) {
        this.apiCaseService = apiCaseService;
        this.apiInfoService = apiInfoService;
        this.projectService = projectService;
        this.caseExecuteService = caseExecuteService;
        this.objectMapper = objectMapper;
    }
    // 按接口查找用例
    @GetMapping("/list")
    public Result<PageResult<ApiCaseVO>> list(@RequestParam Integer apiId,
                                              @RequestParam(defaultValue = "1") Integer pageNum,
                                              @RequestParam(defaultValue = "10") Integer pageSize,
                                              @RequestParam(required = false) String caseName,
                                              @RequestParam(required = false) String caseLevel,
                                              @RequestParam(required = false) Integer status) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiInfo apiInfo = getOwnedApiInfo(apiId, userId);
        if (apiInfo == null) {
            return Result.error(404, "API not found");
        }

        Page<ApiCase> page = new Page<>(pageNum, pageSize);
        Page<ApiCase> result = apiCaseService.page(
                page,
                new LambdaQueryWrapper<ApiCase>()
                        .eq(ApiCase::getApiId, apiId)
                        .eq(ApiCase::getProjectId, apiInfo.getProjectId())
                        .like(StringUtils.hasText(caseName), ApiCase::getCaseName, caseName)
                        .eq(StringUtils.hasText(caseLevel), ApiCase::getCaseLevel, caseLevel)
                        .eq(status != null, ApiCase::getStatus, status)
                        .orderByDesc(ApiCase::getCreateTime)
        );

        List<ApiCaseVO> voList = result.getRecords().stream()
                .map(this::buildApiCaseVO)
                .toList();

        PageResult<ApiCaseVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal((int) result.getTotal());
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());

        return Result.success(pageResult);
    }

    // 按项目查找用例
    @GetMapping("/listProjectCase")
    public Result<PageResult<ApiCaseVO>> listProjectCase(@RequestParam Integer projectId,
                                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                                         @RequestParam(required = false) Integer apiId,
                                                         @RequestParam(required = false) String caseName,
                                                         @RequestParam(required = false) String caseLevel,
                                                         @RequestParam(required = false) Integer status) {
        Integer userId = SecurityUtils.getCurrentUserId();

        Project project = getOwnedProject(projectId, userId);
        if (project == null) {
            return Result.error(404, "Project not found");
        }

        if (apiId != null && getOwnedApiInfo(apiId, projectId, userId) == null) {
            return Result.error(404, "API not found");
        }

        Page<ApiCase> page = new Page<>(pageNum, pageSize);
        Page<ApiCase> result = apiCaseService.page(
                page,
                new LambdaQueryWrapper<ApiCase>()
                        .eq(ApiCase::getProjectId, projectId)
                        .eq(apiId != null, ApiCase::getApiId, apiId)
                        .like(StringUtils.hasText(caseName), ApiCase::getCaseName, caseName)
                        .eq(StringUtils.hasText(caseLevel), ApiCase::getCaseLevel, caseLevel)
                        .eq(status != null, ApiCase::getStatus, status)
                        .orderByDesc(ApiCase::getCreateTime)
        );

        List<ApiCaseVO> voList = result.getRecords().stream()
                .map(this::buildApiCaseVO)
                .toList();

        PageResult<ApiCaseVO> pageResult = new PageResult<>();
        pageResult.setRecords(voList);
        pageResult.setTotal((int) result.getTotal());
        pageResult.setPageNum((int) result.getCurrent());
        pageResult.setPageSize((int) result.getSize());

        return Result.success(pageResult);
    }


    @GetMapping("/detail/{id}")
    public Result<ApiCaseVO> detail(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiCase apiCase = getOwnedApiCase(id, userId);
        if (apiCase == null) {
            return Result.error(404, "Case not found");
        }

        return Result.success(buildApiCaseVO(apiCase));
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestBody @Valid ApiCaseAddDTO apiCaseAddDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiInfo apiInfo = getOwnedApiInfo(apiCaseAddDTO.getApiId(), apiCaseAddDTO.getProjectId(), userId);
        if (apiInfo == null) {
            return Result.error(404, "API not found");
        }

        ApiCase apiCase = new ApiCase();
        BeanUtils.copyProperties(apiCaseAddDTO, apiCase);
        fillJsonContent(apiCase, apiCaseAddDTO.getRequestHeaders(), apiCaseAddDTO.getRequestParams(),
                apiCaseAddDTO.getRequestBody(), apiCaseAddDTO.getExpectedResult());
        fillCreateFields(apiCase, userId);
        apiCaseService.save(apiCase);

        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody @Valid ApiCaseUpdateDTO apiCaseUpdateDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiCase oldApiCase = getOwnedApiCase(apiCaseUpdateDTO.getId(), userId);
        if (oldApiCase == null) {
            return Result.error(404, "Case not found");
        }

        ApiCase apiCase = new ApiCase();
        BeanUtils.copyProperties(apiCaseUpdateDTO, apiCase);
        apiCase.setRequestHeaders(JsonPayloadUtils.toJsonString(apiCaseUpdateDTO.getRequestHeaders(), oldApiCase.getRequestHeaders()));
        apiCase.setRequestParams(JsonPayloadUtils.toJsonString(apiCaseUpdateDTO.getRequestParams(), oldApiCase.getRequestParams()));
        apiCase.setRequestBody(JsonPayloadUtils.toJsonString(apiCaseUpdateDTO.getRequestBody(), oldApiCase.getRequestBody()));
        apiCase.setExpectedResult(JsonPayloadUtils.toJsonString(apiCaseUpdateDTO.getExpectedResult(), oldApiCase.getExpectedResult()));
        /* 保留原来的项目和接口归属，普通修改不允许把用例移动到其他接口下。 */
        apiCase.setProjectId(oldApiCase.getProjectId());
        apiCase.setApiId(oldApiCase.getApiId());
        apiCase.setUpdateBy(userId);
        apiCase.setUpdateTime(LocalDateTime.now());
        apiCaseService.updateById(apiCase);

        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiCase apiCase = getOwnedApiCase(id, userId);
        if (apiCase == null) {
            return Result.error(404, "Case not found");
        }

        apiCase.setUpdateBy(userId);
        apiCase.setUpdateTime(LocalDateTime.now());
        apiCaseService.updateById(apiCase);
        apiCaseService.removeById(id);

        return Result.success();
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody @Valid BatchDeleteDTO batchDeleteDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        List<ApiCase> apiCases = new ArrayList<>();
        for (Integer id : batchDeleteDTO.getIds()) {
            ApiCase apiCase = getOwnedApiCase(id, userId);
            if (apiCase == null) {
                return Result.error(404, "Case not found");
            }
            apiCase.setUpdateBy(userId);
            apiCase.setUpdateTime(LocalDateTime.now());
            apiCases.add(apiCase);
        }

        apiCaseService.updateBatchById(apiCases);
        apiCaseService.removeByIds(batchDeleteDTO.getIds());

        return Result.success();
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody @Valid StatusDTO statusDTO) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiCase oldApiCase = getOwnedApiCase(statusDTO.getId(), userId);
        if (oldApiCase == null) {
            return Result.error(404, "Case not found");
        }

        ApiCase apiCase = new ApiCase();
        apiCase.setId(statusDTO.getId());
        apiCase.setStatus(statusDTO.getStatus());
        apiCase.setUpdateBy(userId);
        apiCase.setUpdateTime(LocalDateTime.now());
        apiCaseService.updateById(apiCase);

        return Result.success();
    }

    @PostMapping(value = "/batch-add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, List<Integer>>> batchAdd(@RequestParam Integer projectId,
                                                       @RequestParam Integer apiId,
                                                       @RequestParam("file") MultipartFile file) {
        Integer userId = SecurityUtils.getCurrentUserId();
        ApiInfo apiInfo = getOwnedApiInfo(apiId, projectId, userId);
        if (apiInfo == null) {
            return Result.error(404, "API not found");
        }

        List<ApiCaseImportItemDTO> importItems;
        try {
            importItems = parseImportFile(file);
        } catch (IOException | IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }

        if (importItems.isEmpty()) {
            return Result.error(400, "Import file has no cases");
        }

        List<ApiCase> apiCases = new ArrayList<>();
        for (ApiCaseImportItemDTO item : importItems) {
            if (!StringUtils.hasText(item.getCaseName())) {
                return Result.error(400, "Case name cannot be blank");
            }

            ApiCase apiCase = new ApiCase();
            BeanUtils.copyProperties(item, apiCase);
            apiCase.setProjectId(projectId);
            apiCase.setApiId(apiId);
            fillCreateFields(apiCase, userId);
            apiCases.add(apiCase);
        }

        apiCaseService.saveBatch(apiCases);

        Map<String, List<Integer>> data = new HashMap<>();
        data.put("ids", apiCases.stream().map(ApiCase::getId).toList());
        return Result.success(data);
    }

    @PostMapping("/execute/{caseId}")
    public Result<CaseExecuteResultVO> execute(@PathVariable Integer caseId,
                                               @RequestParam Integer envId) {
        Integer userId = SecurityUtils.getCurrentUserId();
        CaseExecuteResultVO result = caseExecuteService.execute(caseId, envId, userId);
        return Result.success(result);
    }

    private void fillCreateFields(ApiCase apiCase, Integer userId) {
        fillDefaultCaseContent(apiCase);
        apiCase.setStatus(1);
        apiCase.setCreateBy(userId);
        apiCase.setUpdateBy(userId);
        apiCase.setCreateTime(LocalDateTime.now());
        apiCase.setUpdateTime(LocalDateTime.now());
    }
    /* 给 ApiCase 接口测试用例对象填充默认空值，避免字段为 null 或空白字符串，保证后续 JSON 解析、页面渲染、数据库存储不会报错。 */
    private void fillDefaultCaseContent(ApiCase apiCase) {
        if (!StringUtils.hasText(apiCase.getRequestHeaders())) {
            apiCase.setRequestHeaders("{}");
        }
        if (!StringUtils.hasText(apiCase.getRequestParams())) {
            apiCase.setRequestParams("{}");
        }
        if (!StringUtils.hasText(apiCase.getRequestBody())) {
            apiCase.setRequestBody("{}");
        }
        if (!StringUtils.hasText(apiCase.getExpectedResult())) {
            apiCase.setExpectedResult("{}");
        }
        if (apiCase.getDescription() == null) {
            apiCase.setDescription("");
        }
    }

    private void fillJsonContent(ApiCase apiCase,
                                 JsonNode requestHeaders,
                                 JsonNode requestParams,
                                 JsonNode requestBody,
                                 JsonNode expectedResult) {
        apiCase.setRequestHeaders(JsonPayloadUtils.toJsonString(requestHeaders));
        apiCase.setRequestParams(JsonPayloadUtils.toJsonString(requestParams));
        apiCase.setRequestBody(JsonPayloadUtils.toJsonString(requestBody));
        apiCase.setExpectedResult(JsonPayloadUtils.toJsonString(expectedResult));
    }

    private ApiCase getOwnedApiCase(Integer caseId, Integer userId) {
        ApiCase apiCase = apiCaseService.getById(caseId);
        if (apiCase == null) {
            return null;
        }

        ApiInfo apiInfo = getOwnedApiInfo(apiCase.getApiId(), apiCase.getProjectId(), userId);
        if (apiInfo == null) {
            return null;
        }

        return apiCase;
    }

    private ApiInfo getOwnedApiInfo(Integer apiId, Integer userId) {
        ApiInfo apiInfo = apiInfoService.getById(apiId);
        if (apiInfo == null || apiInfo.getStatus() == null || apiInfo.getStatus() != 1) {
            return null;
        }

        Project project = getOwnedProject(apiInfo.getProjectId(), userId);
        if (project == null) {
            return null;
        }

        return apiInfo;
    }

    private ApiInfo getOwnedApiInfo(Integer apiId, Integer projectId, Integer userId) {
        ApiInfo apiInfo = apiInfoService.getOne(
                new LambdaQueryWrapper<ApiInfo>()
                        .eq(ApiInfo::getId, apiId)
                        .eq(ApiInfo::getProjectId, projectId)
                        .eq(ApiInfo::getStatus, 1)
        );
        if (apiInfo == null) {
            return null;
        }

        Project project = getOwnedProject(projectId, userId);
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

    private ApiCaseVO buildApiCaseVO(ApiCase apiCase) {
        ApiCaseVO apiCaseVO = new ApiCaseVO();
        BeanUtils.copyProperties(apiCase, apiCaseVO);
        apiCaseVO.setRequestHeaders(JsonPayloadUtils.toJsonNode(apiCase.getRequestHeaders()));
        apiCaseVO.setRequestParams(JsonPayloadUtils.toJsonNode(apiCase.getRequestParams()));
        apiCaseVO.setRequestBody(JsonPayloadUtils.toJsonNode(apiCase.getRequestBody()));
        apiCaseVO.setExpectedResult(JsonPayloadUtils.toJsonNode(apiCase.getExpectedResult()));

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

    private List<ApiCaseImportItemDTO> parseImportFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Import file cannot be empty");
        }

        String filename = file.getOriginalFilename();
        if (!StringUtils.hasText(filename)) {
            throw new IllegalArgumentException("Import file name cannot be empty");
        }

        String content = new String(file.getBytes(), StandardCharsets.UTF_8).replace("﻿", "");
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".json")) {
            return parseJsonItems(content);
        }
        if (lowerFilename.endsWith(".csv")) {
            return parseCsvItems(content);
        }

        throw new IllegalArgumentException("Only CSV and JSON files are supported");
    }

    private List<ApiCaseImportItemDTO> parseJsonItems(String content) throws IOException {
        JsonNode root = objectMapper.readTree(content);
        if (!root.isArray()) {
            throw new IllegalArgumentException("JSON file must be an array");
        }

        List<ApiCaseImportItemDTO> items = new ArrayList<>();
        for (JsonNode node : root) {
            ApiCaseImportItemDTO item = new ApiCaseImportItemDTO();
            item.setCaseName(readText(node, "caseName"));
            item.setCaseLevel(readText(node, "caseLevel"));
            item.setRequestHeaders(readJsonLikeText(node, "requestHeaders"));
            item.setRequestParams(readJsonLikeText(node, "requestParams"));
            item.setRequestBody(readJsonLikeText(node, "requestBody"));
            item.setExpectedResult(readText(node, "expectedResult"));
            item.setDescription(readText(node, "description"));
            items.add(item);
        }
        return items;
    }

    private String readText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isTextual() ? value.asText() : value.toString();
    }

    private String readJsonLikeText(JsonNode node, String fieldName) throws IOException {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return "{}";
        }
        return value.isTextual() ? value.asText() : objectMapper.writeValueAsString(value);
    }

    private List<ApiCaseImportItemDTO> parseCsvItems(String content) {
        List<String> lines = content.lines()
                .filter(StringUtils::hasText)
                .toList();
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV file cannot be empty");
        }

        List<String> headers = parseCsvLine(lines.get(0));
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndex.put(headers.get(i).trim(), i);
        }

        List<ApiCaseImportItemDTO> items = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            List<String> values = parseCsvLine(lines.get(i));
            ApiCaseImportItemDTO item = new ApiCaseImportItemDTO();
            item.setCaseName(readCsvValue(values, headerIndex, "caseName"));
            item.setCaseLevel(readCsvValue(values, headerIndex, "caseLevel"));
            item.setRequestHeaders(defaultJson(readCsvValue(values, headerIndex, "requestHeaders")));
            item.setRequestParams(defaultJson(readCsvValue(values, headerIndex, "requestParams")));
            item.setRequestBody(defaultJson(readCsvValue(values, headerIndex, "requestBody")));
            item.setExpectedResult(readCsvValue(values, headerIndex, "expectedResult"));
            item.setDescription(readCsvValue(values, headerIndex, "description"));
            items.add(item);
        }
        return items;
    }

    private String readCsvValue(List<String> values, Map<String, Integer> headerIndex, String headerName) {
        Integer index = headerIndex.get(headerName);
        if (index == null || index >= values.size()) {
            return null;
        }
        String value = values.get(index);
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String defaultJson(String value) {
        return StringUtils.hasText(value) ? value : "{}";
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString());
        return values;
    }
}
