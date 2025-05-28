package com.api.chatapi.application.services;

import com.api.chatapi.application.helpers.ChatAuthorizationHelper;
import com.api.chatapi.domain.dtos.chat.ChatDetailsResponse;
import com.api.chatapi.domain.dtos.chat.ChatResponse;
import com.api.chatapi.domain.dtos.chat.CreateChatRequest;
import com.api.chatapi.domain.dtos.chat.UpdateChatRequest;
import com.api.chatapi.application.mappers.ChatDetailsResponseMapper;
import com.api.chatapi.application.mappers.ChatResponseMapper;
import com.api.chatapi.domain.models.Chat;
import com.api.chatapi.infrastructure.persistence.repositories.ChatRepository;
import com.api.chatapi.infrastructure.persistence.repositories.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ChatResponseMapper chatResponseMapper;
    private final ChatDetailsResponseMapper chatDetailsResponseMapper;
    private final UserBlockService userBlockService;
    private final ChatAuthorizationHelper chatAuthorizationHelper;

    public Page<ChatResponse> getChats(String appContext, Boolean archived, Pageable pageable) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        Page<Chat> chats = chatRepository.findAllByAppContextAndUser(appContext, currentUserId, archived, pageable);

        return chats.map(chatResponseMapper);
    }

    public ChatDetailsResponse getChat(Long id) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        Chat chat = chatRepository.findByIdAndUser(id, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return chatDetailsResponseMapper.apply(chat);
    }

    public ChatDetailsResponse createChat(CreateChatRequest request) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        userBlockService.validateUsersNotBlocked(request.participantUserId(), request.appContext());

        Chat chat = chatRepository
                .findBetweenUsersAndAppContext(currentUserId, request.participantUserId(), request.appContext().toString());

        if (chat != null) {
            return restoreDeletedChat(chat);
        }

        Chat newChat = Chat.builder()
                .appContext(request.appContext())
                .creatorUserId(currentUserId)
                .participantUserId(request.participantUserId())
                .build();

        return chatDetailsResponseMapper.apply(chatRepository.save(newChat));
    }

    private ChatDetailsResponse restoreDeletedChat(Chat chat) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        boolean isCreator = chat.isCreator(currentUserId);
        boolean isParticipant = chat.isParticipant(currentUserId);

        if (!isCreator && !isParticipant) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (isCreator && chat.isDeletedByCreator()) {
            chat.setDeletedByCreator(false);
        }
        if (isParticipant && chat.isDeletedByParticipant()) {
            chat.setDeletedByParticipant(false);
        }

        return chatDetailsResponseMapper.apply(chatRepository.save(chat));
    }

    public ChatDetailsResponse updateChat(Long id, UpdateChatRequest request) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        Chat chat = chatRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isCreator = chat.isCreator(currentUserId);
        boolean isParticipant = chat.isParticipant(currentUserId);

        chatAuthorizationHelper.validateUserAccessToChat(chat);

        boolean updated = false;

        if (isCreator && !chat.isDeletedByCreator()) {
            applyUpdates(chat, request, true);
            updated = true;
        }

        if (isParticipant && !chat.isDeletedByParticipant()) {
            applyUpdates(chat, request, false);
            updated = true;
        }

        if (!updated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        Chat savedChat = chatRepository.save(chat);
        return chatDetailsResponseMapper.apply(savedChat);
    }

    private void applyUpdates(Chat chat, UpdateChatRequest request, boolean isCreator) {
        if (request.isArchived() != null) {
            if (isCreator) {
                chat.setArchivedByCreator(request.isArchived());
            }
            else {
                chat.setArchivedByParticipant(request.isArchived());
            }
        }
        if (request.isMuted() != null) {
            if (isCreator) {
                chat.setMutedByCreator(request.isMuted());
            }
            else {
                chat.setMutedByParticipant(request.isMuted());
            }
        }
        if (request.isRead() != null) {
            if (isCreator) {
                chat.setLastReadByCreator(LocalDateTime.now());
            }
            else {
                chat.setLastReadByParticipant(LocalDateTime.now());
            }
        }
    }

    @Transactional
    public void deleteChat(Long id) {
        Long currentUserId = chatAuthorizationHelper.getCurrentUserId();

        Chat chat = chatRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isCreator = chat.isCreator(currentUserId);
        boolean isParticipant = chat.isParticipant(currentUserId);

        chatAuthorizationHelper.validateUserAccessToChat(chat);

        if (isCreator) {
            chat.setDeletedByCreator(true);
            messageRepository.deletedBySender(id, currentUserId);
            messageRepository.deletedByReceiver(id, currentUserId);
            chatRepository.save(chat);
        }

        if (isParticipant) {
            chat.setDeletedByParticipant(true);
            messageRepository.deletedBySender(id, currentUserId);
            messageRepository.deletedByReceiver(id, currentUserId);
            chatRepository.save(chat);
        }
    }
}
