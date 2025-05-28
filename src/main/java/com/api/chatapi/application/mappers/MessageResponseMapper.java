package com.api.chatapi.application.mappers;

import com.api.chatapi.domain.dtos.message.MessageResponse;
import com.api.chatapi.domain.models.Message;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class MessageResponseMapper implements Function<Message, MessageResponse> {

    @Override
    public MessageResponse apply(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .text(message.getText())
                .senderId(message.getSenderId())
                .build();
    }
}
