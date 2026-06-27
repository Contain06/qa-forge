package com.atcontain.qaforge.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestPlanBindCasesDTO {
    @NotNull(message = "Plan id cannot be null")
    private Integer planId;

    @NotEmpty(message = "Case ids cannot be empty")
    private List<@NotNull(message = "Case id cannot be null") Integer> caseIds;
}
