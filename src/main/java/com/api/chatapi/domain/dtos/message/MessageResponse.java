package com.api.chatapi.domain.dtos.message;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class MessageResponse implements Serializable {
    private Long id;
    private String text;
    private Long senderId;
}
