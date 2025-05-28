package com.api.chatapi.infrastructure.persistence.repositories;

import com.api.chatapi.domain.enums.AppContext;
import com.api.chatapi.domain.models.UserBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    @Query("select ub from UserBlock ub where ub.blockerUserId = ?1 and ub.appContext = ?2")
    List<UserBlock> findAllByBlockerUser(Long blockerUserId, String appContext);

    @Query("select ub from UserBlock ub where ub.appContext = ?1 and ub.blockerUserId = ?2 and ub.blockedUserId = ?3")
    Optional<UserBlock> findByAppContextAndBlockerUserAndBlockedUser(String appContext, Long blockerUserId, Long blockedUserId);
}
