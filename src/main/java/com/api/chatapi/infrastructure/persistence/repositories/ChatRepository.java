package com.api.chatapi.infrastructure.persistence.repositories;

import com.api.chatapi.domain.models.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
    select c.id, c.appContext, c.creatorUserId, c.participantUserId,
        c.isArchivedByParticipant, c.isArchivedByCreator, c.isDeletedByCreator,
        c.isDeletedByParticipant, c.lastReadByCreator, c.lastReadByParticipant,
        c.isMutedByCreator, c.isMutedByParticipant from Chat c
    where c.appContext = :appContext
        and ((c.creatorUserId = :userId and c.isDeletedByCreator = false and (:archived is null or c.isArchivedByCreator = :archived))
        or (c.participantUserId = :userId and c.isDeletedByParticipant = false and (:archived is null or c.isArchivedByParticipant = :archived)))
    """)
    Page<Chat> findAllByAppContextAndUser(String appContext, Long userId, Boolean archived, Pageable pageable);

    @Query("""
    select distinct c from Chat c
    join fetch c.messages m
    where (c.creatorUserId = :userId or c.participantUserId = :userId)
        and ((m.senderId = :userId and m.isDeletedBySender = false)
            or (m.senderId != :userId and m.isDeletedByReceiver = false))
    """)
    Optional<Chat> findByIdAndUser(Long id, Long userId);

    @Query("""
    select c from Chat c
    where ((c.creatorUserId = :userId1 and c.participantUserId = :userId2)
        or (c.creatorUserId = :userId2 and c.participantUserId = :userId1))
        and c.appContext = :appContext
    """)
    Chat findBetweenUsersAndAppContext(Long userId1, Long userId2, String appContext);
}
