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
@TableName("run_record")
public class RunRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer projectId;
    private Integer environmentId;
    private Integer planId;
    private Integer caseId;
    private String runType;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Integer timeoutCount;
    private Integer errorCount;
    private Double passRate;
    private Integer totalTime;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer createBy;
    private Integer updateBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

}
