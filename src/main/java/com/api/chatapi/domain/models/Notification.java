package com.api.chatapi.domain.models;

import com.api.chatapi.domain.dtos.message.MessageResponse;
import com.api.chatapi.domain.enums.NotificationStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private NotificationStatus status;
    private MessageResponse message;
}
