package com.api.chatapi.api.controllers;

import com.api.chatapi.domain.dtos.chat.ChatDetailsResponse;
import com.api.chatapi.domain.dtos.chat.ChatResponse;
import com.api.chatapi.domain.dtos.chat.CreateChatRequest;
import com.api.chatapi.domain.dtos.chat.UpdateChatRequest;
import com.api.chatapi.application.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{appContext}")
    public ResponseEntity<Page<ChatResponse>> getChats(@PathVariable String appContext,
                                                       @RequestParam(required = false) Boolean archived,
                                                       Pageable pageable) {
        Page<ChatResponse> chats = chatService.getChats(appContext, archived, pageable);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatDetailsResponse> getChat(@PathVariable Long id) {
        ChatDetailsResponse chat = chatService.getChat(id);
        return ResponseEntity.ok(chat);
    }

    @PostMapping()
    public ResponseEntity<ChatDetailsResponse> createChat(@RequestBody CreateChatRequest chatRequest) {
        ChatDetailsResponse chat = chatService.createChat(chatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(chat);
    }


    @PutMapping("/{id}")
    public ResponseEntity<ChatDetailsResponse> updateChat(@PathVariable Long id, @RequestBody UpdateChatRequest chatRequest) {
        ChatDetailsResponse chat = chatService.updateChat(id, chatRequest);
        return ResponseEntity.ok(chat);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        chatService.deleteChat(id);
        return ResponseEntity.noContent().build();
    }
}
