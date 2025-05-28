package com.api.chatapi.domain.dtos.chat;

public record UpdateChatRequest(
        Boolean isArchived,
        Boolean isMuted,
        Boolean isRead
) {
}