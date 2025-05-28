package com.api.chatapi.domain.dtos.chat;

import com.api.chatapi.domain.enums.AppContext;

public record CreateChatRequest(
        Long participantUserId,
        AppContext appContext
) {
}
