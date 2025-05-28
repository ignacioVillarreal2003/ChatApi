package com.api.chatapi.application.mappers;

import com.api.chatapi.domain.dtos.chat.ChatResponse;
import com.api.chatapi.domain.models.Chat;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ChatResponseMapper implements Function<Chat, ChatResponse> {

    @Override
    public ChatResponse apply(Chat chat) {
        return ChatResponse.builder()
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
                .build();
    }
}