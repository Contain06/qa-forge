package com.atcontain.qaforge.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("api_case")
public class ApiCase {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer projectId;
    private Integer apiId;
    private String caseName;
    private String caseLevel;
    private String requestHeaders;
    private String requestParams;
    private String requestBody;
    private String expectedResult;
    private String description;
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
