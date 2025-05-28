package com.api.chatapi.domain.dtos.message;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateMessageRequest {
    private String text;
    private Boolean isDeleted;
}
