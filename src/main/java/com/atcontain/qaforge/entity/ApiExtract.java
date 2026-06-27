package com.atcontain.qaforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("api_extract")
public class ApiExtract {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer projectId;
    private Integer caseId;
    private String extractName;
    private String variableKey;
    private String source;
    private String extractType;
    private String expression;
    private Integer match_group;
    private Integer required;
    private Integer sortOrder;
    private Integer status;
    private Integer createBy;
    private Integer updateBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer deleted;
}
