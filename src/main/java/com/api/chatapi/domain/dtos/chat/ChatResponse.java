package com.api.chatapi.domain.dtos.chat;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
public class ChatResponse implements Serializable {
    private Long id;
    private Long creatorUserId;
    private Long participantUserId;
    private String appContext;
    private LocalDateTime lastReadByCreator;
    private LocalDateTime lastReadByParticipant;
    private boolean isDeletedByCreator;
    private boolean isDeletedByParticipant;
    private boolean isArchivedByCreator;
    private boolean isArchivedByParticipant;
    private boolean isMutedByCreator;
    private boolean isMutedByParticipant;
}
