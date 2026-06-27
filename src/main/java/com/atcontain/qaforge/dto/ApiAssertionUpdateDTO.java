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
public class ApiAssertionUpdateDTO {
    @NotNull(message = "Assertion id cannot be null")
    private Integer id;

    @NotBlank(message = "Assert type cannot be blank")
    @Pattern(regexp = "^(STATUS_CODE|JSON_PATH|BODY)$", message = "Assert type must be STATUS_CODE, JSON_PATH or BODY")
    private String assertType;

    /*
        表达式
    */
    private String expression;

    @NotBlank(message = "Assert operator cannot be blank")
    @Pattern(
            regexp = "^(=|!=|>=|>|<=|<|contains|exists|notEmpty)$",
            message = "Assert operator is invalid"
    )
    private String assertOperator;

    private String expectedValue;

    private Integer sortOrder;
}
