package com.api.chatapi.domain.dtos.userBlock;

import com.api.chatapi.domain.enums.AppContext;

public record CreateUserBlockRequest(
        Long blockedUserId,
        AppContext appContext
) {
}
