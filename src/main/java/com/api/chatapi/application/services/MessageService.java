package com.api.chatapi.application.services;

import com.api.chatapi.application.helpers.ChatAuthorizationHelper;
import com.api.chatapi.application.websocket.NotificationService;
import com.api.chatapi.domain.dtos.message.CreateMessageRequest;
import com.api.chatapi.domain.dtos.message.MessageResponse;
import com.api.chatapi.domain.dtos.message.UpdateMessageRequest;
import com.api.chatapi.application.mappers.MessageResponseMapper;
import com.api.chatapi.domain.enums.NotificationStatus;
import com.api.chatapi.domain.models.Chat;
import com.api.chatapi.domain.models.Message;
import com.api.chatapi.domain.models.Notification;
import com.api.chatapi.infrastructure.persistence.repositories.ChatRepository;
import com.api.chatapi.infrastructure.persistence.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessageResponseMapper messageResponseMapper;
    private final ChatAuthorizationHelper chatAuthorizationHelper;
    private final NotificationService notificationService;

    public MessageResponse createMessage(CreateMessageRequest request) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        chatAuthorizationHelper.validateUserAccessToChat(chat);

        Message newMessage = Message.builder()
                .senderId(currentUserId)
                .chat(chat)
                .text(request.getText())
                .build();

        Message message = messageRepository.save(newMessage);

        MessageResponse messageResponse = messageResponseMapper.apply(message);

        notificationService.sendNotification(message.getId(),
                new Notification(NotificationStatus.ADDED, messageResponse));

        return messageResponse;
    }

    public MessageResponse updateMessage(Long id, UpdateMessageRequest request) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        chatAuthorizationHelper.validateUserAccessToChat(message.getChat());

        boolean isSender = message.getSenderId().equals(currentUserId);

        if (isSender) {
            updateAsSender(message, request);
        }
        else {
            updateAsReceiver(message, request);
        }

        messageRepository.save(message);

        MessageResponse messageResponse = messageResponseMapper.apply(message);

        notificationService.sendNotification(message.getId(),
                new Notification(NotificationStatus.UPDATED, messageResponse));

        return messageResponse;
    }

    private void updateAsSender(Message message, UpdateMessageRequest request) {
        if (request.getIsDeleted() != null) {
            message.setDeletedBySender(request.getIsDeleted());
        }
        if (request.getText() != null && !message.isDeletedBySender()) {
            message.setText(request.getText());
        } else if (request.getText() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    private void updateAsReceiver(Message message, UpdateMessageRequest request) {
        if (request.getIsDeleted() != null) {
            message.setDeletedByReceiver(request.getIsDeleted());
        }
        if (request.getText() != null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    public void deleteMessage(Long id) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        Message existingMessage = messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!existingMessage.getSenderId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Chat chat = existingMessage.getChat();

        chatAuthorizationHelper.validateUserAccessToChat(chat);

        existingMessage.setDeletedBySender(true);
        existingMessage.setDeletedByReceiver(true);

        messageRepository.save(existingMessage);

        MessageResponse messageResponse = messageResponseMapper.apply(existingMessage);

        notificationService.sendNotification(existingMessage.getId(),
                new Notification(NotificationStatus.DELETED, messageResponse));
    }
}
