package com.ptit.grading.notification.consumer;

import com.ptit.grading.notification.model.Notification;
import com.ptit.grading.notification.repository.NotificationRepository;
import com.ptit.grading.notification.service.NotificationService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;
    private final Gson gson;

    @KafkaListener(
        topics = "${kafka.topic.notifications:notifications}",
        groupId = "${spring.kafka.consumer.group-id:notification-group}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        log.info("Received notification: key={}, partition={}", record.key(), record.partition());

        try {
            Map<String, Object> message = gson.fromJson(record.value(), Map.class);

            String userId = (String) message.get("userId");
            String type = (String) message.get("type");
            String title = (String) message.get("title");
            String body = (String) message.get("body");

            notificationService.saveAndPush(
                UUID.fromString(userId),
                type,
                title,
                body
            );
        } catch (Exception e) {
            log.error("Failed to process notification", e);
        }
    }
}
