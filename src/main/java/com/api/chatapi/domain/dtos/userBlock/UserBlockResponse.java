package com.api.chatapi.domain.dtos.userBlock;

import com.api.chatapi.domain.enums.AppContext;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class UserBlockResponse implements Serializable {
    private Long id;
    private Long blockerUserId;
    private Long blockedUserId;
    private AppContext appContext;
}
