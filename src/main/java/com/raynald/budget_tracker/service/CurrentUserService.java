package com.raynald.budget_tracker.service;

import com.raynald.budget_tracker.entity.User;
import com.raynald.budget_tracker.exception.UnauthorizedException;
import com.raynald.budget_tracker.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/** Finds the logged-in user from the JWT that Spring Security has already verified. */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new UnauthorizedException("Not logged in");
        }
        Long userId = Long.valueOf(jwt.getSubject());
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("This account no longer exists"));
    }
}