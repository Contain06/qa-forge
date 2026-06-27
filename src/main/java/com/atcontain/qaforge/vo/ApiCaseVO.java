package com.atcontain.qaforge.vo;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiCaseVO {
    private Integer id;
    private Integer projectId;
    private String projectName;
    private Integer apiId;
    private String apiName;
    private String caseName;
    private String caseLevel;
    @Schema(type = "object", description = "JSON request headers")
    private JsonNode requestHeaders;

    @Schema(type = "object", description = "JSON request params")
    private JsonNode requestParams;

    @Schema(type = "object", description = "JSON request body")
    @TableField(updateStrategy = FieldStrategy.NOT_NULL)
    private JsonNode requestBody;

    @Schema(type = "object", description = "JSON expected result")
    private JsonNode expectedResult;
    private String description;
    private Integer status;
    private Integer createBy;
    private Integer updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
