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
public class RunRecordVO {
    private Integer id;
    private Integer projectId;
    private String projectName;
    private Integer environmentId;
    private String envName;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
