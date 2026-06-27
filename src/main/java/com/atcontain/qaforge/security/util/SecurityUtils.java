package com.atcontain.qaforge.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 工具类，从 SecurityContext 中获取当前登录用户信息。
 */
public class SecurityUtils {

    /**
     * 从 SecurityContext 中获取当前登录用户 ID。
     * userId 由 JwtAuthenticationFilter 在认证成功后通过 authentication.setDetails(userId) 存入。
     *
     * @return 当前用户 ID，未认证时返回 null
     */
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Integer) {
            return (Integer) authentication.getDetails();
        }
        return null;
    }
}
