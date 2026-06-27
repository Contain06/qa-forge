package com.atcontain.qaforge.entity;


import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_assertion")
public class ApiAssertion {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer caseId;

    private String assertType;

    private String expression;

    private String assertOperator;

    private String expectedValue;

    private Integer sortOrder;

    private Integer status;

    private Integer createBy;

    private Integer updateBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
