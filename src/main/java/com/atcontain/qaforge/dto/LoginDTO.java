package com.atcontain.qaforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {
    @NotBlank(message = "Username cannot be blank")
    @Size(max = 50, message = "Username length cannot exceed 50")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 50, message = "Password length must be between 6 and 50")
    private String password;
}
