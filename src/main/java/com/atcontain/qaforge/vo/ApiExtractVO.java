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
public class ApiExtractVO {
    private Integer id;
    private Integer projectId;
    private Integer caseId;
    private String extractName;
    private String variableKey;
    private String source;
    private String extractType;
    private String expression;
    private Integer matchGroup;
    private Integer required;
    private Integer sortOrder;
    private Integer status;

    // 关联展示字段
    private String projectName;
    private String caseName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
