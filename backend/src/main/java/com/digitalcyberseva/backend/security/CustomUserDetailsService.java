package com.digitalcyberseva.backend.security;

import com.digitalcyberseva.backend.entity.DeletionStatus;
import com.digitalcyberseva.backend.entity.User;
import com.digitalcyberseva.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByMobile(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return toActivePrincipal(user);
    }

    public UserPrincipal loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new UserPrincipal(user);
    }

    private UserPrincipal toActivePrincipal(User user) {
        if (Boolean.TRUE.equals(user.getIsDeleted()) || user.getDeletionStatus() == DeletionStatus.APPROVED) {
            throw new DisabledException("Account is scheduled for deletion");
        }
        return new UserPrincipal(user);
    }
}
