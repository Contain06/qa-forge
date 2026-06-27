package com.atcontain.qaforge.builder;

import com.atcontain.qaforge.dto.HttpExecuteRequest;
import com.atcontain.qaforge.entity.ApiCase;
import com.atcontain.qaforge.entity.ApiInfo;
import com.atcontain.qaforge.entity.EnvVariable;
import com.atcontain.qaforge.entity.Environment;
import com.atcontain.qaforge.service.EnvVariableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HttpRequestBuilder {

    private final ObjectMapper objectMapper;
    private final EnvVariableService envVariableService;

    /** 匹配 {{变量名}} 的占位符模式 */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");

    public HttpRequestBuilder(ObjectMapper objectMapper, EnvVariableService envVariableService) {
        this.objectMapper = objectMapper;
        this.envVariableService = envVariableService;
    }

    /**
     * 组装最终 HTTP 请求。
     * 合并环境、接口模板、测试用例的配置，并替换 {{变量}} 占位符。
     */
    public HttpExecuteRequest build(Environment environment, ApiInfo apiInfo, ApiCase apiCase) {
        String method = apiInfo.getRequestMethod();
        String url = buildUrl(environment.getBaseUrl(), apiInfo.getApiPath());

        Map<String, Object> headers = new LinkedHashMap<>();
        headers.putAll(parseJsonMap(apiInfo.getRequestHeaders()));
        headers.putAll(parseJsonMap(apiCase.getRequestHeaders()));

        Map<String, Object> params = new LinkedHashMap<>();
        params.putAll(parseJsonMap(apiInfo.getRequestParams()));
        params.putAll(parseJsonMap(apiCase.getRequestParams()));

        String body = StringUtils.hasText(apiCase.getRequestBody())
                ? apiCase.getRequestBody()
                : apiInfo.getRequestBody();

        // ========== 变量替换：将 {{key}} 替换为环境变量值 ==========
        Map<String, String> variables = loadEnvVariables(environment.getId());
        if (!variables.isEmpty()) {
            url = substitute(url, variables);
            body = substitute(body, variables);
            headers = substituteMap(headers, variables);
            params = substituteMap(params, variables);
        }

        return HttpExecuteRequest.builder()
                .method(method)
                .url(url)
                .headers(toJson(headers))
                .params(toJson(params))
                .body(StringUtils.hasText(body) ? body : "{}")
                .build();
    }

    /**
     * 加载指定环境下的所有启用变量，组装成 key→value 映射。
     */
    private Map<String, String> loadEnvVariables(Integer environmentId) {
        Map<String, String> variables = new LinkedHashMap<>();
        List<EnvVariable> list = envVariableService.list(
                new LambdaQueryWrapper<EnvVariable>()
                        .eq(EnvVariable::getEnvironmentId, environmentId)
                        .eq(EnvVariable::getStatus, 1)
        );
        for (EnvVariable v : list) {
            variables.put(v.getVariableKey(), v.getVariableValue());
        }
        log.debug("加载环境变量 {} 个，envId={}", variables.size(), environmentId);
        return variables;
    }

    /**
     * 对字符串中的 {{key}} 进行替换。
     * 未找到对应变量时保留原占位符（不中断请求），方便调试。
     */
    private String substitute(String text, Map<String, String> variables) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        Matcher m = VARIABLE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            String key = m.group(1);
            String replacement = variables.getOrDefault(key, m.group(0));
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 对 Map 中每个 value 进行 {{key}} 变量替换。
     */
    private Map<String, Object> substituteMap(Map<String, Object> map, Map<String, String> variables) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, substitute((String) value, variables));
            } else {
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * 拼接完整请求地址。
     */
    private String buildUrl(String baseUrl, String apiPath) {
        if (!StringUtils.hasText(baseUrl)) {
            throw new IllegalArgumentException("Base URL cannot be blank");
        }
        if (!StringUtils.hasText(apiPath)) {
            throw new IllegalArgumentException("API path cannot be blank");
        }

        if (baseUrl.endsWith("/") && apiPath.startsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1) + apiPath;
        }
        if (!baseUrl.endsWith("/") && !apiPath.startsWith("/")) {
            return baseUrl + "/" + apiPath;
        }
        return baseUrl + apiPath;
    }

    /**
     * JSON 字符串 → Map。
     */
    private Map<String, Object> parseJsonMap(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Request config must be a JSON object");
        }
    }

    /**
     * Map → JSON 字符串。
     */
    private String toJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Request config cannot be converted to JSON");
        }
    }
}
