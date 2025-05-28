package com.api.chatapi.domain.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "message")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Message extends Auditable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private boolean isDeletedBySender = false;

    @Column(nullable = false)
    private boolean isDeletedByReceiver = false;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
}
