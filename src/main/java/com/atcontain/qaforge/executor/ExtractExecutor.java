package com.atcontain.qaforge.executor;

import com.atcontain.qaforge.dto.HttpExecuteResult;
import com.atcontain.qaforge.entity.ApiExtract;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 响应数据提取执行器。
 * 从 HTTP 响应中按照 ApiExtract 规则提取数据，支持 JSONPath 和正则表达式两种方式。
 */
@Slf4j
@Component
public class ExtractExecutor {

    /**
     * 批量执行提取规则。
     *
     * @param extracts   本次需要执行的提取规则列表（通常来自 api_extract 表）
     * @param httpResult 上一步 HTTP 请求的完整响应
     * @return variableKey → extractedValue 的映射
     * @throws RuntimeException 当 required=true 的提取规则执行失败时抛出
     */
    public Map<String, String> execute(List<ApiExtract> extracts, HttpExecuteResult httpResult) {
        Map<String, String> result = new LinkedHashMap<>();

        if (extracts == null || extracts.isEmpty()) {
            return result;
        }

        for (ApiExtract extract : extracts) {
            // 跳过已禁用的提取规则
            if (extract.getStatus() == null || extract.getStatus() != 1) {
                log.debug("跳过已禁用的提取规则：{}", extract.getExtractName());
                continue;
            }

            try {
                String value = extractSingle(extract, httpResult);
                if (value != null) {
                    result.put(extract.getVariableKey(), value);
                    log.info("提取成功 [{}] {}: {} → {}", extract.getExtractType(),
                            extract.getExtractName(), extract.getVariableKey(), value);
                } else if (isRequired(extract)) {
                    throw new RuntimeException(
                            String.format("必须提取项 [%s] 未匹配到任何内容", extract.getExtractName()));
                }
            } catch (RuntimeException e) {
                // required 的规则失败直接抛出，中断用例执行
                if (isRequired(extract)) {
                    throw e;
                }
                // 非 required 的规则失败只记日志，不影响用例通过
                log.warn("提取规则 [{}] 执行失败（非必须，已忽略）: {}", extract.getExtractName(), e.getMessage());
            }
        }

        return result;
    }

    /**
     * 执行单条提取规则。
     */
    private String extractSingle(ApiExtract extract, HttpExecuteResult httpResult) {
        // 1. 确定提取源文本
        String sourceText = resolveSourceText(extract.getSource(), httpResult);
        if (!StringUtils.hasText(sourceText)) {
            log.warn("提取源 [{}] 为空，跳过规则 [{}]", extract.getSource(), extract.getExtractName());
            return null;
        }

        // 2. 根据提取类型分发
        if ("JSON_PATH".equalsIgnoreCase(extract.getExtractType())) {
            return extractByJsonPath(sourceText, extract.getExpression(), extract.getExtractName());
        }

        if ("REGEX".equalsIgnoreCase(extract.getExtractType())) {
            return extractByRegex(sourceText, extract.getExpression(),
                    extract.getMatch_group(), extract.getExtractName());
        }

        throw new IllegalArgumentException("不支持的提取类型: " + extract.getExtractType());
    }

    /**
     * JSONPath 提取。
     * 使用 jayway JsonPath 库，兼容标准 JSONPath 语法。
     * 示例表达式：$.data.token、$.items[0].id、$.total
     */
    private String extractByJsonPath(String json, String expression, String ruleName) {
        try {
            Object value = JsonPath.read(json, expression);

            if (value == null) {
                return null;
            }

            // 如果已经是字符串，直接返回
            if (value instanceof String) {
                return (String) value;
            }

            // 对象或数组返回其 JSON 表示
            return value.toString();

        } catch (PathNotFoundException e) {
            log.warn("JSONPath [{}] 在响应中未找到匹配路径，规则 [{}]", expression, ruleName);
            return null;
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("JSONPath 提取失败 [%s]: %s", expression, e.getMessage()), e);
        }
    }

    /**
     * 正则表达式提取。
     * 使用 Java 标准 Pattern/Matcher，提取第一个匹配的捕获组。
     * matchGroup 为 null 或 0 时返回完整匹配；>=1 时返回对应捕获组。
     * 示例：从 Set-Cookie 头提取 token → expression="token=([^;]+)" matchGroup=1
     */
    private String extractByRegex(String text, String pattern, Integer matchGroup, String ruleName) {
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(text);

            if (!m.find()) {
                log.warn("正则 [{}] 未匹配到内容，规则 [{}]", pattern, ruleName);
                return null;
            }

            int group = (matchGroup == null || matchGroup < 0) ? 0 : matchGroup;

            if (group > m.groupCount()) {
                log.warn("正则捕获组 {} 不存在（共 {} 组），返回全文匹配，规则 [{}]",
                        group, m.groupCount(), ruleName);
                return m.group(0);
            }

            return m.group(group);

        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("正则提取失败 [%s]: %s", pattern, e.getMessage()), e);
        }
    }

    /**
     * 根据 source 字段获取提取源文本。
     */
    private String resolveSourceText(String source, HttpExecuteResult httpResult) {
        if ("RESPONSE_HEADERS".equalsIgnoreCase(source)) {
            return httpResult.getResponseHeaders();
        }
        // 默认从 RESPONSE_BODY 提取
        return httpResult.getResponseBody();
    }

    private boolean isRequired(ApiExtract extract) {
        return extract.getRequired() != null && extract.getRequired() == 1;
    }
}
