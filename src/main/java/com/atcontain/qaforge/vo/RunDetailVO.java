package com.atcontain.qaforge.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunDetailVO {
    private Integer id;
    private Integer runRecordId;

    private Integer projectId;
    private String projectName;

    private Integer environmentId;
    private String envName;

    private Integer planId;

    private Integer caseId;
    private String caseName;

    private Integer apiId;
    private String apiName;

    private String requestUrl;
    private String requestMethod;
    private String requestHeaders;
    private String requestParams;
    private String requestBody;

    private Integer responseStatus;
    private String responseHeaders;
    private String responseBody;
    private Integer responseTime;

    private String assertResult;
    private String errorMessage;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
