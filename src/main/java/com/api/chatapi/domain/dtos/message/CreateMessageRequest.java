package com.api.chatapi.domain.dtos.message;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CreateMessageRequest {
    private Long chatId;
    private String text;
}
