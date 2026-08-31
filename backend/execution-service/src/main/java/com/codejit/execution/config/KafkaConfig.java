package com.codejit.execution.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic submissionsTopic() {
        return TopicBuilder.name("codejit.submissions")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic submissionResultsTopic() {
        return TopicBuilder.name("codejit.submission-results")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

