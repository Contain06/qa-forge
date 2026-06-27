package com.atcontain.qaforge.security.filter;

import com.atcontain.qaforge.security.util.JwtTokenUtil;
import com.atcontain.qaforge.util.TokenBlacklistUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenBlacklistUtils tokenBlacklistService;

    public JwtAuthenticationFilter(TokenBlacklistUtils tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // 登录/注册/登出/Swagger 接口直接放行
        if (requestURI.contains("/user/login") ||
                requestURI.contains("/user/register") ||
                requestURI.contains("/user/logout") ||
                requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.contains("/doc.html") ||
                requestURI.contains("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 从 Authorization 请求头解析 JWT token
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token格式错误\"}");
            return;
        }

        String token = bearerToken.substring(7);
        Claims claims;
        try {
            claims = JwtTokenUtil.getClaims(token);
        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token无效或已过期\"}");
            return;
        }

        // 检查 token 是否在黑名单中（已登出）
        if (tokenBlacklistService.containsJti(claims.getId())) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"Token已失效，请重新登录\"}");
            return;
        }
        Integer userId = claims.get("userId", Integer.class);
        String username = claims.get("username", String.class);
        /**
         * 构建已认证的身份令牌UsernamePasswordAuthenticationToken对象
         * principal存用户名;
         * credentials置空清除明文密码
         * 暂不携带权限
        */
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.NO_AUTHORITIES);
        /**
         * 存储用户Id于Details扩展字段
         * 后续通过 SecurityUtils.getCurrentUserId() 能够读取数据
         * */
        authenticationToken.setDetails(userId);
        // 通过 SecurityContextHolder 对象将 authentication数据存储到不同线程的线程池
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
