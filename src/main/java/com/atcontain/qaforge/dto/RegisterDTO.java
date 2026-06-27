package com.atcontain.qaforge.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
public class RegisterDTO {
    @Pattern(regexp = "^\\S{5,16}$", message = "用户名必须是5-16位非空白字符")
    @NotBlank
    private String username;
    @Size(max = 20, message = "别名长度不能超过20位")
    private String nickname;
    @Pattern(regexp = "^\\S{5,16}$", message = "密码必须是5-16位非空白字符")
    @NotBlank
    private String password;
    @Pattern(regexp = "^\\S{5,16}$", message = "确认密码必须是5-16位非空白字符")
    @NotBlank
    private String rePassword;
    @Pattern(regexp = "^1[3-9]\\d{9}$")
    @NotBlank
    private String phone;
    @Email
    @NotBlank
    private String email;

}
