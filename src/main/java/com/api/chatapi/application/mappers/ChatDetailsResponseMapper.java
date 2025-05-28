package com.api.chatapi.application.mappers;

import com.api.chatapi.domain.dtos.chat.ChatDetailsResponse;
import com.api.chatapi.domain.models.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatDetailsResponseMapper implements Function<Chat, ChatDetailsResponse> {

    private final MessageResponseMapper messageResponseMapper;

    @Override
    public ChatDetailsResponse apply(Chat chat) {
        return ChatDetailsResponse.builder()
                .id(chat.getId())
                .creatorUserId(chat.getCreatorUserId())
                .participantUserId(chat.getParticipantUserId())
                .appContext(chat.getAppContext().toString())
                .lastReadByCreator(chat.getLastReadByCreator())
                .lastReadByParticipant(chat.getLastReadByParticipant())
                .isDeletedByCreator(chat.isDeletedByCreator())
                .isDeletedByParticipant(chat.isDeletedByParticipant())
                .isArchivedByCreator(chat.isArchivedByCreator())
                .isArchivedByParticipant(chat.isArchivedByParticipant())
                .isMutedByCreator(chat.isMutedByCreator())
                .isMutedByParticipant(chat.isMutedByParticipant())
                .messages(chat.getMessages()
                        .stream()
                        .map(messageResponseMapper)
                        .collect(Collectors.toList()))
                .build();
    }
}
