package com.digitalcyberseva.backend.service;

import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.exception.UnauthorizedException;
import com.digitalcyberseva.backend.repository.UserRepository;
import com.digitalcyberseva.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        return getCurrentUser(false);
    }

    public User getCurrentUserAllowDeleted() {
        return getCurrentUser(true);
    }

    private User getCurrentUser(boolean allowDeleted) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UnauthorizedException("Authentication required");
        }

        User user = userRepository.findById(principal.getUser().getId())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!allowDeleted && Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new UnauthorizedException("Your account is scheduled for deletion");
        }

        return user;
    }
}
