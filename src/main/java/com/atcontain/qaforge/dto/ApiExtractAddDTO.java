package com.atcontain.qaforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiExtractAddDTO {
    @NotNull(message = "Project id cannot be null")
    private Integer projectId;

    @NotNull(message = "Case id cannot be null")
    private Integer caseId;

    @NotBlank(message = "Extract name cannot be blank")
    private String extractName;

    @NotBlank(message = "Variable key cannot be blank")
    private String variableKey;

    @NotBlank(message = "Source cannot be blank")
    @Pattern(regexp = "^(RESPONSE_BODY|RESPONSE_HEADERS)$",
            message = "Source must be RESPONSE_BODY or RESPONSE_HEADERS")
    private String source;

    @NotBlank(message = "Extract type cannot be blank")
    @Pattern(regexp = "^(JSON_PATH|REGEX)$",
            message = "Extract type must be JSON_PATH or REGEX")
    private String extractType;

    @NotBlank(message = "Expression cannot be blank")
    private String expression;

    private Integer matchGroup;

    private Integer required;

    private Integer sortOrder;
}
