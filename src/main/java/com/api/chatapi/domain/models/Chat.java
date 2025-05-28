package com.api.chatapi.domain.models;

import com.api.chatapi.domain.enums.AppContext;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "chat",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"appContext", "creatorUserId", "participantUserId"})
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Chat extends Auditable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppContext appContext;

    @Column(nullable = false)
    private Long creatorUserId;

    @Column(nullable = false)
    private Long participantUserId;

    @Column
    private LocalDateTime lastReadByCreator;

    @Column
    private LocalDateTime lastReadByParticipant;

    @Column(nullable = false)
    private boolean isDeletedByCreator = false;

    @Column(nullable = false)
    private boolean isDeletedByParticipant = false;

    @Column(nullable = false)
    private boolean isArchivedByCreator = false;

    @Column(nullable = false)
    private boolean isArchivedByParticipant = false;

    @Column(nullable = false)
    private boolean isMutedByCreator = false;

    @Column(nullable = false)
    private boolean isMutedByParticipant = false;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    public boolean isCreator(Long userId) {
        return creatorUserId != null && creatorUserId.equals(userId);
    }

    public boolean isParticipant(Long userId) {
        return participantUserId != null && participantUserId.equals(userId);
    }

    public boolean isUserInChat(Long userId) {
        return isCreator(userId) || isParticipant(userId);
    }
}
