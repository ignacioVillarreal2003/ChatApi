package com.api.chatapi.application.services;

import com.api.chatapi.application.mappers.UserBlockResponseMapper;
import com.api.chatapi.config.authentication.AuthenticatedUserProvider;
import com.api.chatapi.domain.dtos.userBlock.CreateUserBlockRequest;
import com.api.chatapi.domain.dtos.userBlock.UserBlockResponse;
import com.api.chatapi.domain.enums.AppContext;
import com.api.chatapi.domain.models.UserBlock;
import com.api.chatapi.infrastructure.persistence.repositories.UserBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserBlockResponseMapper userBlockResponseMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public List<UserBlockResponse> getUserBlocks(String appContext) {
        return userBlockRepository.findAllByBlockerUser(authenticatedUserProvider.getUserId(), appContext)
                .stream()
                .map(userBlockResponseMapper)
                .collect(Collectors.toList());
    }

    public UserBlockResponse createUserBlock(CreateUserBlockRequest userBlockRequest) {
        UserBlock existingUserBlock = userBlockRepository
                .findByAppContextAndBlockerUserAndBlockedUser(userBlockRequest.appContext().toString(), authenticatedUserProvider.getUserId(), userBlockRequest.blockedUserId())
                .orElse(null);

        if (existingUserBlock != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        if (userBlockRequest.blockedUserId().equals(authenticatedUserProvider.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        UserBlock newUserBlock = UserBlock.builder()
                .blockerUserId(authenticatedUserProvider.getUserId())
                .blockedUserId(userBlockRequest.blockedUserId())
                .appContext(userBlockRequest.appContext())
                .build();

        UserBlock savedUserBlock = userBlockRepository.save(newUserBlock);
        return userBlockResponseMapper.apply(savedUserBlock);
    }

    public void deleteUserBlock(Long id) {
        UserBlock existingUserBlock = userBlockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!existingUserBlock.getBlockerUserId().equals(authenticatedUserProvider.getUserId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        userBlockRepository.delete(existingUserBlock);
    }

    public void validateUsersNotBlocked(Long userId, AppContext appContext) {
        Long currentUserId = authenticatedUserProvider.getUserId();

        UserBlock userBlock1 = userBlockRepository
                .findByAppContextAndBlockerUserAndBlockedUser(appContext.toString(), currentUserId, userId)
                .orElse(null);
        UserBlock userBlock2 = userBlockRepository
                .findByAppContextAndBlockerUserAndBlockedUser(appContext.toString(), userId, currentUserId)
                .orElse(null);

        if (userBlock1 != null && userBlock1.getBlockerUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        if (userBlock2 != null && userBlock2.getBlockedUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
    }
}
