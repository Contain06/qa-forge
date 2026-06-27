package com.atcontain.qaforge.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestPlanDetailVO {
    private TestPlanVO plan;
    private List<ApiCaseVO> cases;
}
