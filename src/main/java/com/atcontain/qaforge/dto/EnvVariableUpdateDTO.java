package com.atcontain.qaforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvVariableUpdateDTO {
    @NotNull(message = "Variable id cannot be null")
    private Integer id;

    @NotBlank(message = "Variable key cannot be blank")
    @Size(max = 100, message = "Variable key length cannot exceed 100")
    @Pattern(regexp = "^[A-Za-z_][A-Za-z0-9_.-]*$", message = "Variable key format is invalid")
    private String variableKey;

    private String variableValue;

    @Size(max = 255, message = "Description length cannot exceed 255")
    private String description;
}
