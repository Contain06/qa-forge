package com.atcontain.qaforge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpExecuteRequest {
    private String method;
    @URL
    private String url;
    private String headers;
    private String params;
    private String body;
}
