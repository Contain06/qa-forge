package com.atcontain.qaforge.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class  CaseExecuteResultVO {
    private Integer caseId;
    private String caseName;

    private Integer apiId;
    private String apiName;

    private Integer envId;
    private String envName;

    private Boolean pass;

    private Integer statusCode;
    private String responseBody;

    private Integer assertionTotal;
    private Integer assertionPass;
    private Integer assertionFail;

    private Long durationMs;
    private String errorMessage;
    private Integer runRecordId;

}
