package com.api.chatapi.unit;

import com.api.chatapi.application.helpers.ChatAuthorizationHelper;
import com.api.chatapi.application.mappers.ChatDetailsResponseMapper;
import com.api.chatapi.application.mappers.ChatResponseMapper;
import com.api.chatapi.application.services.ChatService;
import com.api.chatapi.application.services.UserBlockService;
import com.api.chatapi.domain.dtos.chat.ChatDetailsResponse;
import com.api.chatapi.domain.dtos.chat.ChatResponse;
import com.api.chatapi.domain.dtos.chat.CreateChatRequest;
import com.api.chatapi.domain.dtos.chat.UpdateChatRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
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

    @Mock
    private UserBlockService userBlockService;

    @Mock
    private ChatAuthorizationHelper chatAuthorizationHelper;

    @InjectMocks
    private ChatService chatService;

    private AppContext appContext;
    private Long creatorUserId;
    private Long participantUserId;
    private Chat chat;
    private List<Chat> chats;
    private List<Chat> archivedChats;

    @BeforeEach
    void setUp() {
        appContext = AppContext.RESCUE_ME;
        creatorUserId = 1L;
        participantUserId = 2L;
        chat = Chat.builder()
                .id(1L)
                .appContext(appContext)
                .creatorUserId(creatorUserId)
                .participantUserId(participantUserId)
                .build();
        Chat chat1 = Chat.builder()
                .id(2L)
                .appContext(appContext)
                .creatorUserId(creatorUserId)
                .participantUserId(3L)
                .build();
        Chat chat2 = Chat.builder()
                .id(3L)
                .appContext(appContext)
                .creatorUserId(creatorUserId)
                .participantUserId(4L)
                .build();
        Chat chat3 = Chat.builder()
                .id(4L)
                .appContext(appContext)
                .creatorUserId(creatorUserId)
                .participantUserId(5L)
                .isArchivedByCreator(true)
                .build();
        chats = Arrays.asList(chat, chat1, chat2);
        archivedChats = Collections.singletonList(chat3);
    }

    @Test
    void getChats_ShouldReturnNonEmptyPage_WhenChatsExists() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Chat> chatPage = new PageImpl<>(chats, pageable, 1);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findAllByAppContextAndUser(appContext.toString(), creatorUserId, null, pageable))
                .thenReturn(chatPage);
        when(chatResponseMapper.apply(any(Chat.class)))
                .thenAnswer(invocation -> {
                    Chat c = invocation.getArgument(0);
                    return ChatResponse.builder().id(c.getId()).build();
                });

        Page<ChatResponse> result = chatService.getChats(appContext.toString(), null, pageable);

        verify(chatResponseMapper, times(chats.size())).apply(any(Chat.class));
        assertEquals(chats.get(0).getId(), result.getContent().get(0).getId());
        assertEquals(3, result.getTotalElements());
    }

    @Test
    void getChats_ShouldReturnNonEmptyPage_WhenArchivedChatsExist() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Chat> chatPage = new PageImpl<>(archivedChats, pageable, archivedChats.size());

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findAllByAppContextAndUser(appContext.toString(), creatorUserId, true, pageable))
                .thenReturn(chatPage);
        when(chatResponseMapper.apply(any(Chat.class)))
                .thenAnswer(invocation -> {
                    Chat c = invocation.getArgument(0);
                    return ChatResponse.builder().id(c.getId()).build();
                });

        Page<ChatResponse> result = chatService.getChats(appContext.toString(), true, pageable);

        verify(chatResponseMapper, times(archivedChats.size())).apply(any(Chat.class));
        assertEquals(archivedChats.get(0).getId(), result.getContent().get(0).getId());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getChats_ShouldReturnEmptyPage_WhenNoChatsExist() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Chat> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findAllByAppContextAndUser(appContext.toString(), creatorUserId, null, pageable))
                .thenReturn(emptyPage);

        Page<ChatResponse> result = chatService.getChats(appContext.toString(), null, pageable);

        verify(chatResponseMapper, never()).apply(any(Chat.class));
        assertTrue(result.isEmpty());
    }

    @Test
    void getChat_ShouldReturnChat_WhenChatExistsAndUserIsInvolved() {
        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findByIdAndUser(chat.getId(), creatorUserId))
                .thenReturn(Optional.of(chat));
        when(chatDetailsResponseMapper.apply(any(Chat.class)))
                .thenAnswer(invocation -> {
                    Chat c = invocation.getArgument(0);
                    return ChatDetailsResponse.builder().id(c.getId()).build();
                });

        ChatDetailsResponse result = chatService.getChat(chat.getId());

        verify(chatDetailsResponseMapper).apply(chat);
        assertNotNull(result);
        assertEquals(chat.getId(), result.getId());
    }

    @Test
    void getChat_ShouldThrowNotFound_WhenChatDoesNotExistForUser() {
        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findByIdAndUser(chat.getId(), creatorUserId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                chatService.getChat(chat.getId()));

        verify(chatDetailsResponseMapper, never()).apply(any(Chat.class));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void createChat_ShouldReturnChat_WhenSuccessfullyCreated() {
        CreateChatRequest request = new CreateChatRequest(participantUserId, appContext);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        doNothing().when(userBlockService)
                .validateUsersNotBlocked(participantUserId, appContext);
        when(chatRepository.findBetweenUsersAndAppContext(creatorUserId, participantUserId, appContext.toString()))
                .thenReturn(null);
        when(chatRepository.save(any(Chat.class)))
                .thenReturn(chat);
        when(chatDetailsResponseMapper.apply(chat))
                .thenReturn(ChatDetailsResponse.builder().id(chat.getId()).build());

        ChatDetailsResponse response = chatService.createChat(request);

        assertNotNull(response);
        assertEquals(chat.getId(), response.getId());
        verify(chatRepository).save(any(Chat.class));
        verify(chatDetailsResponseMapper).apply(chat);
    }

    @Test
    void createChat_ShouldRestoreAndReturnChat_WhenExistingChatIsDeletedAndUserIsInvolved() {
        CreateChatRequest request = new CreateChatRequest(participantUserId, appContext);

        chat.setDeletedByCreator(true);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        doNothing().when(userBlockService)
                .validateUsersNotBlocked(participantUserId, appContext);
        when(chatRepository.findBetweenUsersAndAppContext(creatorUserId, participantUserId, appContext.toString()))
                .thenReturn(chat);
        when(chatRepository.save(chat))
                .thenReturn(chat);
        when(chatDetailsResponseMapper.apply(chat))
                .thenReturn(ChatDetailsResponse.builder().id(chat.getId()).build());

        ChatDetailsResponse response = chatService.createChat(request);

        assertNotNull(response);
        assertEquals(chat.getId(), response.getId());
        verify(chatRepository).save(chat);
        verify(chatDetailsResponseMapper).apply(chat);
    }

    @Test
    void createChat_ShouldThrowConflict_WhenUsersAreBlocked() {
        CreateChatRequest request = new CreateChatRequest(participantUserId, appContext);

        when(chatAuthorizationHelper.getCurrentUserId()).thenReturn(creatorUserId);
        doThrow(new ResponseStatusException(HttpStatus.CONFLICT))
                .when(userBlockService).validateUsersNotBlocked(participantUserId, appContext);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> chatService.createChat(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(chatRepository, never()).findBetweenUsersAndAppContext(any(), any(), any());
        verify(chatDetailsResponseMapper, never()).apply(any());
    }

    @Test
    public void updateChat_ShouldReturnChat_WhenChatExistsAndUserIsInvolved() {
        UpdateChatRequest request = new UpdateChatRequest(true, false, true);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findById(chat.getId()))
                .thenReturn(Optional.of(chat));
        doNothing().when(chatAuthorizationHelper)
                .validateUserAccessToChat(chat);
        when(chatRepository.save(chat))
                .thenReturn(chat);
        when(chatDetailsResponseMapper.apply(chat))
                .thenReturn(ChatDetailsResponse.builder().id(chat.getId()).build());

        ChatDetailsResponse response = chatService.updateChat(chat.getId(), request);

        assertNotNull(response);
        assertEquals(chat.getId(), response.getId());
        verify(chatRepository).save(chat);
        verify(chatAuthorizationHelper).validateUserAccessToChat(chat);
        verify(chatDetailsResponseMapper).apply(chat);
    }

    @Test
    public void updateChat_ShouldReturnNotFound_WhenChatDoesNotExist() {
        UpdateChatRequest request = new UpdateChatRequest(true, false, true);

        when(chatAuthorizationHelper.getCurrentUserId()).thenReturn(creatorUserId);
        when(chatRepository.findById(chat.getId())).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> chatService.updateChat(chat.getId(), request));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(chatRepository).findById(chat.getId());
        verify(chatAuthorizationHelper, never()).validateUserAccessToChat(any());
    }

    @Test
    public void updateChat_ShouldReturnConflict_WhenRequestIsEmpty() {
        UpdateChatRequest request = new UpdateChatRequest(null, null, null);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findById(chat.getId()))
                .thenReturn(Optional.of(chat));
        doNothing().when(chatAuthorizationHelper)
                .validateUserAccessToChat(chat);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> chatService.updateChat(chat.getId(), request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(chatRepository, never()).save(any());
        verify(chatDetailsResponseMapper, never()).apply(any());
    }

    @Test
    public void updateChat_ShouldReturnUnauthorized_WhenUserNotCreatorOrParticipant() {
        UpdateChatRequest request = new UpdateChatRequest(true, false, true);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(99L);
        when(chatRepository.findById(chat.getId()))
                .thenReturn(Optional.of(chat));
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED))
                .when(chatAuthorizationHelper).validateUserAccessToChat(chat);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> chatService.updateChat(chat.getId(), request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(chatRepository, never()).save(any());
    }

    @Test
    public void deleteChat_ShouldMarkAsDeletedAndSave_WhenUserIsCreator() {
        chat.setCreatorUserId(creatorUserId);
        chat.setParticipantUserId(participantUserId);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findById(chat.getId()))
                .thenReturn(Optional.of(chat));
        doNothing().when(chatAuthorizationHelper)
                .validateUserAccessToChat(chat);

        assertDoesNotThrow(() -> chatService.deleteChat(chat.getId()));

        verify(messageRepository).deletedBySender(chat.getId(), creatorUserId);
        verify(messageRepository).deletedByReceiver(chat.getId(), creatorUserId);
        verify(chatRepository).save(chat);
    }

    @Test
    public void deleteChat_ShouldMarkAsDeletedAndSave_WhenUserIsParticipant() {
        chat.setCreatorUserId(participantUserId);
        chat.setParticipantUserId(creatorUserId);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(participantUserId);
        when(chatRepository.findById(chat.getId()))
                .thenReturn(Optional.of(chat));
        doNothing().when(chatAuthorizationHelper)
                .validateUserAccessToChat(chat);

        assertDoesNotThrow(() -> chatService.deleteChat(chat.getId()));

        verify(messageRepository).deletedBySender(chat.getId(), participantUserId);
        verify(messageRepository).deletedByReceiver(chat.getId(), participantUserId);
        verify(chatRepository).save(chat);
    }

    @Test
    public void deleteChat_ShouldHandleBothRoles_WhenUserIsCreatorAndParticipant() {
        chat.setCreatorUserId(creatorUserId);
        chat.setParticipantUserId(creatorUserId);

        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findById(chat.getId()))
                .thenReturn(Optional.of(chat));
        doNothing().when(chatAuthorizationHelper)
                .validateUserAccessToChat(chat);

        assertDoesNotThrow(() -> chatService.deleteChat(chat.getId()));

        verify(messageRepository, times(2)).deletedBySender(chat.getId(), creatorUserId);
        verify(messageRepository, times(2)).deletedByReceiver(chat.getId(), creatorUserId);
        verify(chatRepository, times(2)).save(chat);
    }

    @Test
    public void deleteChat_ShouldReturnNotFound_WhenChatDoesNotExist() {
        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findById(chat.getId()))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                chatService.deleteChat(chat.getId()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void deleteChat_ShouldReturnUnauthorized_WhenUserNotCreatorOrParticipant() {
        when(chatAuthorizationHelper.getCurrentUserId())
                .thenReturn(creatorUserId);
        when(chatRepository.findById(chat.getId()))
                .thenReturn(Optional.of(chat));
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED))
                .when(chatAuthorizationHelper).validateUserAccessToChat(chat);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> chatService.deleteChat(chat.getId()));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        verify(chatRepository, never()).save(any());
        verify(messageRepository, never()).deletedBySender(any(), any());
        verify(messageRepository, never()).deletedByReceiver(any(), any());
    }
}
