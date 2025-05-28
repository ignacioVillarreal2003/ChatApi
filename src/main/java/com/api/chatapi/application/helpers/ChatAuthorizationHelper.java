package com.api.chatapi.application.helpers;

import com.api.chatapi.application.services.UserBlockService;
import com.api.chatapi.config.authentication.AuthenticatedUserProvider;
import com.api.chatapi.domain.dtos.chat.ChatDetailsResponse;
import com.api.chatapi.domain.models.Chat;
import com.api.chatapi.infrastructure.persistence.repositories.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class ChatAuthorizationHelper {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserBlockService userBlockService;
    private final ChatRepository chatRepository;

    public Long getCurrentUserId() {
        return authenticatedUserProvider.getUserId();
    }

    public void validateUserAccessToChat(Chat chat) {
        boolean isCreator = chat.isCreator(getCurrentUserId());
        boolean isParticipant = chat.isParticipant(getCurrentUserId());

        if (!isCreator && !isParticipant) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (isCreator) {
            userBlockService.validateUsersNotBlocked(chat.getParticipantUserId(), chat.getAppContext());
        }

        if (isParticipant) {
            userBlockService.validateUsersNotBlocked(chat.getCreatorUserId(), chat.getAppContext());
        }
    }

    public boolean isAuthorizedToParticipant(Long chatId, Long userId) {
        Chat chat = chatRepository.findById(chatId).orElse(null);

        if (chat == null) {
            return false;
        }
        return chat.isCreator(userId) || chat.isParticipant(userId);
    }
}
