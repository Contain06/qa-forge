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
@TableName("run_detail")
public class RunDetail {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer runRecordId;
    private Integer projectId;
    private Integer environmentId;
    private Integer planId;
    private Integer caseId;
    private Integer apiId;
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
    private Integer createBy;
    private Integer updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

}
