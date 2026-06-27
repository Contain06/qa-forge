package com.atcontain.qaforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCaseImportItemDTO {
    private String caseName;
    private String caseLevel;
    private String requestHeaders;
    private String requestParams;
    private String requestBody;
    private String expectedResult;
    private String description;
}
