package com.ssafy.flip.domain.connect.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class WebTriggerProducer {

    private final KafkaTemplate<String, String> template;

    public void run(String payload) {
        CompletableFuture<SendResult<String, String>> future =
                template.send("web-trigger", payload);  // JSON 메시지 전체 전송

        future.thenAccept(result -> {
            RecordMetadata metadata = result.getRecordMetadata();
            System.out.println("✅ Kafka 전송 성공: " + metadata.topic() +
                    "-" + metadata.partition() + "@" + metadata.offset());
        }).exceptionally(ex -> {
            System.err.println("❌ Kafka 전송 실패: " + ex.getMessage());
            return null;
        });
    }
}

