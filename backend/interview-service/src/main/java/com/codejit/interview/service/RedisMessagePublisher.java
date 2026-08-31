package com.codejit.interview.service;

import com.codejit.common.dto.interview.LiveInterviewEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisMessagePublisher.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public static final String INTERVIEW_TOPIC = "codejit-interview-channel";

    public RedisMessagePublisher(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publishEvent(Long roomId, LiveInterviewEvent event) {
        try {
            if (redisTemplate != null) {
                String json = objectMapper.writeValueAsString(event);
                redisTemplate.convertAndSend(INTERVIEW_TOPIC, roomId + ":" + json);
            }
        } catch (Exception e) {
            log.warn("Failed to publish to Redis Pub/Sub: {}", e.getMessage());
        }
    }
}

