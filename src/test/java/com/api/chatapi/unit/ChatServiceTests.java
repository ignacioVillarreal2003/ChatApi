package com.api.chatapi.unit;

import com.api.chatapi.application.mappers.ChatDetailsResponseMapper;
import com.api.chatapi.application.mappers.ChatResponseMapper;
import com.api.chatapi.application.services.ChatService;
import com.api.chatapi.domain.dtos.chat.ChatDetailsResponse;
import com.api.chatapi.domain.dtos.chat.ChatResponse;
import com.api.chatapi.domain.enums.AppContext;
import com.api.chatapi.domain.models.Chat;
import com.api.chatapi.infrastructure.persistence.repositories.ChatRepository;
import com.api.chatapi.infrastructure.persistence.repositories.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTests {

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatResponseMapper chatResponseMapper;

    @Mock
    private ChatDetailsResponseMapper chatDetailsResponseMapper;

    @InjectMocks
    private ChatService chatService;

    private AppContext appContext;
    private Long creatorUserId;
    private Chat chat;

    @BeforeEach
    void setUp() {
        appContext = AppContext.RESCUE_ME;
        creatorUserId = 1L;

        chat = Chat.builder()
                .id(1L)
                .appContext(appContext)
                .creatorUserId(creatorUserId)
                .participantUserId(2L)
                .build();
    }

    @Test
    void getChats_ShouldReturnNonEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Chat> chatPage = new PageImpl<>(List.of(chat), pageable, 1);

        when(chatRepository.findAllByAppContextAndUser(appContext.toString(), creatorUserId, null, pageable))
                .thenReturn(chatPage);
        when(chatResponseMapper.apply(chat))
                .thenReturn(ChatResponse.builder().id(chat.getId()).build());

        Page<ChatResponse> result = chatService.getChats(appContext.toString(), null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(chat.getId(), result.getContent().get(0).getId());
    }

    @Test
    void getChats_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Chat> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(chatRepository.findAllByAppContextAndUser(appContext.toString(), creatorUserId, null, pageable))
                .thenReturn(emptyPage);

        Page<ChatResponse> result = chatService.getChats(appContext.toString(), null, pageable);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getChat_ShouldReturnChat_WhenChatExistsAndUserIsInvolved() {
        when(chatRepository.findById(1L))
                .thenReturn(Optional.of(chat));
        when(chatResponseMapper.apply(chat))
                .thenReturn(ChatResponse.builder().id(1L).build());

        ChatDetailsResponse result = chatService.getChat(1L);

        assertNotNull(result);
    }

    @Test
    public void getChat_ShouldReturnUnauthorized_WhenChatExistsAndUserIsNotInvolved() {

    }

    @Test
    public void getChat_ShouldReturnNotFound_WhenChatDoesNotExist() {

    }

    @Test
    public void createChat_ShouldReturnChat_WhenSuccessfullyCreated() {

    }

    @Test
    public void createChat_ShouldReturnChat_WhenChatExistsAndUserIsInvolvedAndIsDeleted() {

    }

    @Test
    public void createChat_ShouldReturnConflict_WhenChatExistsAndUserIsInvolvedAndIsNotDeleted() {

    }

    @Test
    public void updateChat_ShouldReturnChat_WhenChatExistAndUserIsInvolved() {

    }

    @Test
    public void updateChat_ShouldReturnNotFound_WhenChatDoesNotExist() {

    }

    @Test
    public void updateChat_ShouldReturnUnauthorized_WhenChatExistAndUserIsNotInvolved() {

    }

    @Test
    public void deleteChat_ShouldReturnVoid_WhenSuccessfullyDeleted() {

    }

    @Test
    public void deleteChat_ShouldReturnNotFound_WhenChatDoesNotExist() {

    }
}
