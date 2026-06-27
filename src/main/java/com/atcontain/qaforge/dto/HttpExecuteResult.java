package com.atcontain.qaforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpExecuteResult {
    private Boolean success;

    private String requestMethod;
    private String requestUrl;
    private String requestHeaders;
    private String requestBody;

    private Integer statusCode;
    private String responseHeaders;
    private String responseBody;

    private Long durationMs;
    private Boolean isSuccess;
    private String errorMessage;
}
