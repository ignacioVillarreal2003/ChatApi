package com.api.chatapi.infrastructure.persistence.repositories;

import com.api.chatapi.domain.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Modifying
    @Query("""
        update Message m
        set m.isDeletedBySender = true
        where m.chat.id = :chatId and m.senderId = :userId
    """)
    void deletedBySender(Long chatId, Long userId);

    @Modifying
    @Query("""
        update Message m
        set m.isDeletedByReceiver = true
        where m.chat.id = :chatId and m.senderId <> :userId
    """)
    void deletedByReceiver(Long chatId, Long userId);
}
