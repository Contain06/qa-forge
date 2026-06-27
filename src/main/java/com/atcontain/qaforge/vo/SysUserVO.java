package com.atcontain.qaforge.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysUserVO {
    private Integer id;
    private String username;
    private String nickname;
    private String token;
}
