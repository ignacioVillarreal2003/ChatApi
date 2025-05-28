package com.api.chatapi.interceptors;

import com.api.chatapi.application.helpers.ChatAuthorizationHelper;
import com.api.chatapi.application.websocket.StompPrincipal;
import com.api.chatapi.application.services.ChatService;
import com.api.chatapi.config.authentication.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final ChatAuthorizationHelper chatAuthorizationHelper;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                Long userId = jwtService.extractUserId(token);
                accessor.setUser(new StompPrincipal(userId.toString()));
            } else {
                throw new IllegalArgumentException("No JWT token found in WebSocket headers");
            }
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            if (destination != null && destination.startsWith("/chat/")) {
                long chatId = Long.parseLong(destination.substring("/chat/".length()));
                try {
                    Long userId = Long.parseLong(accessor.getUser().getName());
                    if (!chatAuthorizationHelper.isAuthorizedToParticipant(chatId, userId)) {
                        throw new IllegalArgumentException("Unauthorized subscription to this petition's chat");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid petition id in chat subscription");
                }
            }
        }

        return message;
    }
}
