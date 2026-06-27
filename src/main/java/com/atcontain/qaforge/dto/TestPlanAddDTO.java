package com.atcontain.qaforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestPlanAddDTO {
    @NotNull(message = "Project id cannot be null")
    private Integer projectId;

    @NotNull(message = "Environment id cannot be null")
    private Integer environmentId;

    @NotBlank(message = "Plan name cannot be blank")
    private String planName;

    private String description;
}
