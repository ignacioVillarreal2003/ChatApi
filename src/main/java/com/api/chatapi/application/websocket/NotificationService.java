package com.api.chatapi.application.websocket;

import com.api.chatapi.domain.models.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(long chatId, Notification notification) {
        log.info("Send notification to chat {} with status {}", chatId, notification.getStatus());
        messagingTemplate.convertAndSend("/chat/" + chatId, notification);
    }
}
