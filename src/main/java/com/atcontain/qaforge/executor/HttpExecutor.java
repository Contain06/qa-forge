package com.atcontain.qaforge.executor;

import com.atcontain.qaforge.dto.HttpExecuteRequest;
import com.atcontain.qaforge.dto.HttpExecuteResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HttpExecutor {

    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient = new OkHttpClient();

    public HttpExecutor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HttpExecuteResult execute(HttpExecuteRequest request) {
        String url = buildUrlWithParams(request.getUrl(), request.getParams());
        Request.Builder requestBuilder = new Request.Builder()
                .url(url);
        Map<String, Object> headers = parseJsonMap(request.getHeaders());
        addHeaders(requestBuilder, headers);
        String method = request.getMethod().toUpperCase();
        PreparedRequestBody preparedBody = prepareRequestBody(request.getBody(), headers);

        switch (method) {
            case "GET":
                requestBuilder.get();
                break;
            case "POST":
                requestBuilder.post(preparedBody.requestBody());
                break;
            case "PUT":
                requestBuilder.put(preparedBody.requestBody());
                break;
            case "PATCH":
                requestBuilder.patch(preparedBody.requestBody());
                break;
            case "DELETE":
                requestBuilder.delete();
                break;
            default:
                throw new IllegalArgumentException("Unsupported request method: " + method);
        }
        Request okHttpRequest = requestBuilder.build();

        long start = System.currentTimeMillis();

        try (Response response = okHttpClient.newCall(okHttpRequest).execute()) {
            long durationMs = System.currentTimeMillis() - start;

            Integer statusCode = response.code();
            String responseHeaders = response.headers().toString();
            String body = response.body() == null ? "" : response.body().string();

            HttpExecuteResult result = new HttpExecuteResult();
            result.setSuccess(true);
            result.setStatusCode(statusCode);
            result.setResponseHeaders(responseHeaders);
            result.setResponseBody(body);
            result.setDurationMs(durationMs);

            result.setRequestMethod(request.getMethod());
            result.setRequestUrl(okHttpRequest.url().toString());
            result.setRequestHeaders(request.getHeaders());
            result.setRequestBody(preparedBody.bodyText());

            return result;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - start;

            HttpExecuteResult result = new HttpExecuteResult();
            result.setSuccess(false);
            result.setStatusCode(0);
            result.setResponseHeaders("");
            result.setResponseBody("");
            result.setDurationMs(durationMs);
            result.setErrorMessage(e.getMessage());

            result.setRequestMethod(request.getMethod());
            result.setRequestUrl(request.getUrl());
            result.setRequestHeaders(request.getHeaders());
            result.setRequestBody(preparedBody.bodyText());

            return result;
        }

    }

    private void addHeaders(Request.Builder requestBuilder, Map<String, Object> headers) {
        headers.forEach((key, value) -> {
            if (StringUtils.hasText(key) && value != null) {
                requestBuilder.header(key, String.valueOf(value));
            }
        });
    }

    private String buildUrlWithParams(String url, String paramsJson) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        Map<String, Object> params = parseJsonMap(paramsJson);
        params.forEach((k, v) -> {
            if (v != null) {
                urlBuilder.addQueryParameter(k, v.toString());
            }
        });
        return urlBuilder.build().toString();
    }

    private Map<String, Object> parseJsonMap(String json) {
        if (!StringUtils.hasText(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private PreparedRequestBody prepareRequestBody(String body, Map<String, Object> headers) {
        String contentType = findHeader(headers, "Content-Type");
        if (!StringUtils.hasText(contentType)) {
            contentType = findHeader(headers, "content-type");
        }
        if (!StringUtils.hasText(contentType)) {
            contentType = "application/json; charset=utf-8";
        }

        String normalizedContentType = contentType.toLowerCase();
        if (normalizedContentType.contains("application/x-www-form-urlencoded")) {
            return buildFormUrlEncodedBody(body, contentType);
        }

        MediaType mediaType = MediaType.parse(contentType);
        if (mediaType == null) {
            mediaType = MediaType.parse("application/json; charset=utf-8");
        }
        String bodyText = body == null ? "" : body;
        return new PreparedRequestBody(RequestBody.create(bodyText, mediaType), bodyText);
    }

    private PreparedRequestBody buildFormUrlEncodedBody(String body, String contentType) {
        Map<String, Object> formValues;
        try {
            formValues = parseJsonMap(body);
        } catch (RuntimeException e) {
            MediaType mediaType = MediaType.parse(contentType);
            String bodyText = body == null ? "" : body;
            return new PreparedRequestBody(RequestBody.create(bodyText, mediaType), bodyText);
        }

        FormBody.Builder formBuilder = new FormBody.Builder(StandardCharsets.UTF_8);
        StringBuilder bodyText = new StringBuilder();
        formValues.forEach((key, value) -> {
            if (StringUtils.hasText(key) && value != null) {
                String stringValue = stringifyValue(value);
                formBuilder.add(key, stringValue);
                if (!bodyText.isEmpty()) {
                    bodyText.append("&");
                }
                bodyText.append(urlEncode(key)).append("=").append(urlEncode(stringValue));
            }
        });
        return new PreparedRequestBody(formBuilder.build(), bodyText.toString());
    }

    private String findHeader(Map<String, Object> headers, String name) {
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name) && entry.getValue() != null) {
                return String.valueOf(entry.getValue());
            }
        }
        return null;
    }

    private String stringifyValue(Object value) {
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record PreparedRequestBody(RequestBody requestBody, String bodyText) {
    }
}
