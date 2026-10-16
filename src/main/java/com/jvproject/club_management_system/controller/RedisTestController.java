package com.jvproject.club_management_system.controller;



import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/dev/redis")
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    public RedisTestController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/test")
    public Map<String, Object> testRedis() {
        String key = "dev:redis:connection-test";
        String value = "Redis is connected";

        // Write a value with a 60-second TTL.
        redisTemplate.opsForValue()
                .set(key, value, Duration.ofSeconds(60));

        // Read the value back from Redis.
        String retrievedValue =
                redisTemplate.opsForValue().get(key);

        // Check its remaining lifetime.
        Long ttl = redisTemplate.getExpire(
                key, TimeUnit.SECONDS
        );

        return Map.of(
                "status",
                Objects.equals(value, retrievedValue)
                        ? "UP" : "ERROR",
                "key", key,
                "value", retrievedValue == null
                        ? "" : retrievedValue,
                "ttlSeconds", ttl == null ? -2L : ttl
        );
    }
}