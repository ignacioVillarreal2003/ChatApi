package com.api.chatapi.config;

import com.api.chatapi.config.authentication.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<Long> {

    private AuthenticatedUserProvider authenticatedUserProvider;

    @Override
    public Optional<Long> getCurrentAuditor() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return Optional.empty();
        }
        return Optional.of(authenticatedUserProvider.getUserId());
    }
}
