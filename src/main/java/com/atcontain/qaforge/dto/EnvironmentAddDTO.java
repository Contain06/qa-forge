package com.atcontain.qaforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentAddDTO {
    @NotNull(message = "Project id cannot be null")
    private Integer projectId;

    @NotBlank(message = "Environment name cannot be blank")
    private String envName;

    @NotBlank(message = "Base URL cannot be blank")
    @URL(message = "Base URL format is invalid")
    private String baseUrl;

    private String description;
}
