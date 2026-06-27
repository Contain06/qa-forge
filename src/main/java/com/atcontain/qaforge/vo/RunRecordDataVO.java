package com.atcontain.qaforge.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunRecordDataVO {

    // 筛选下总RunRecord记录条数
    private Long totalCount;
    // 成功用例总数
    private Integer successCount;
    // 断言失败总数
    private Integer failCount;
    // 全部记录总耗时(ms)
    private Long totalTime;
    // 异常+超时总数
    private Integer errorCount;
    // 通过率
    private Double passRate;
}
