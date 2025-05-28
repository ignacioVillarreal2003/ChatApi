package com.api.chatapi.application.mappers;

import com.api.chatapi.domain.dtos.userBlock.UserBlockResponse;
import com.api.chatapi.domain.models.UserBlock;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserBlockResponseMapper implements Function<UserBlock, UserBlockResponse> {

    @Override
    public UserBlockResponse apply(UserBlock userBlock) {
        return UserBlockResponse.builder()
                .id(userBlock.getId())
                .blockerUserId(userBlock.getBlockerUserId())
                .blockedUserId(userBlock.getBlockedUserId())
                .appContext(userBlock.getAppContext())
                .build();
    }
}
