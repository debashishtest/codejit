package com.codejit.execution.service;

import com.codejit.common.event.SubmissionResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaSubmissionResultProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaSubmissionResultProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    public static final String RESULT_TOPIC = "codejit.submission-results";

    public KafkaSubmissionResultProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishResult(SubmissionResultEvent event) {
        try {
            if (kafkaTemplate != null) {
                kafkaTemplate.send(RESULT_TOPIC, String.valueOf(event.getSubmissionId()), event);
                log.info("Published submission result to Kafka topic {}: id={}", RESULT_TOPIC, event.getSubmissionId());
            }
        } catch (Exception e) {
            log.warn("Kafka result publishing skipped or unavailable: {}", e.getMessage());
        }
    }
}

