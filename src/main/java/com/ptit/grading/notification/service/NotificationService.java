package com.ptit.grading.notification.service;

import com.ptit.grading.notification.model.Notification;
import com.ptit.grading.notification.repository.NotificationRepository;
import com.ptit.grading.notification.websocket.NotificationWebSocketHandler;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;
    private final Gson gson;

    @Transactional
    public void saveAndPush(UUID userId, String type, String title, String body) {
        // Save to DB
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .body(body)
                .read(false)
                .build();
        notificationRepository.save(notification);

        // Push via WebSocket
        Map<String, Object> message = Map.of(
            "type", type,
            "title", title,
            "body", body,
            "timestamp", System.currentTimeMillis()
        );
        webSocketHandler.sendToUser(userId.toString(), gson.toJson(message));

        log.info("Notification sent to user {}: {}", userId, title);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getHistory(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
}
