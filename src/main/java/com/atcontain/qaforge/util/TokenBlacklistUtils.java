package com.atcontain.qaforge.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class TokenBlacklistUtils {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "token:blacklist:";

    public TokenBlacklistUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addJti(String jti, long ttlMillis) {
        redisTemplate.opsForValue().set(PREFIX + jti, "1", Duration.ofMillis(ttlMillis));
    }

    public boolean containsJti(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
    }

    public void removeJti(String jti) {
        redisTemplate.delete(PREFIX + jti);
    }
}
