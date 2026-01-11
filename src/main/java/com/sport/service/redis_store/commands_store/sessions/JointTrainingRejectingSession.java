package com.sport.service.redis_store.commands_store.sessions;

import com.sport.service.dto.JointTrainingRejectingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JointTrainingRejectingSession {

    @Qualifier("botCommandSessionRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    private static final long TTL_SECONDS = 300;

    private String key(Long adminId) {
        return "JT:REJECT:ADMIN:" + adminId;
    }

    public void start(Long adminId, Long jtId) {
        JointTrainingRejectingDto session = new JointTrainingRejectingDto(
                adminId,
                jtId,
                System.currentTimeMillis());

        redisTemplate.opsForValue().set(
                key(adminId),
                session,
                TTL_SECONDS,
                TimeUnit.SECONDS);
    }

    public boolean isWaiting(Long adminId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key(adminId)));
    }

    public JointTrainingRejectingDto get(Long adminId) {
        Object obj = redisTemplate.opsForValue().get(key(adminId));
        return obj == null ? null : (JointTrainingRejectingDto) obj;
    }

    public void clear(Long adminId) {
        redisTemplate.delete(key(adminId));
    }
}