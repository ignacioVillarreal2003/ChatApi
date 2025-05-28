package com.api.chatapi.api.controllers;

import com.api.chatapi.domain.dtos.message.CreateMessageRequest;
import com.api.chatapi.domain.dtos.message.MessageResponse;
import com.api.chatapi.domain.dtos.message.UpdateMessageRequest;
import com.api.chatapi.application.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping()
    public ResponseEntity<MessageResponse> createMessage(@RequestBody CreateMessageRequest messageRequest) {
        MessageResponse message = messageService.createMessage(messageRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> updateMessage(@PathVariable Long id, @RequestBody UpdateMessageRequest messageRequest) {
        MessageResponse message = messageService.updateMessage(id, messageRequest);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
