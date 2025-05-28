package com.api.chatapi.domain.models;

import com.api.chatapi.domain.enums.AppContext;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "userBlock",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"blockerUserId", "blockedUserId", "appContext"})
        }
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserBlock extends Auditable<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long blockerUserId;

    @Column(nullable = false)
    private Long blockedUserId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AppContext appContext;
}
