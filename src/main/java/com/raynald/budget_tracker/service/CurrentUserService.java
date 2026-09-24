package com.raynald.budget_tracker.service;

import com.raynald.budget_tracker.entity.User;
import com.raynald.budget_tracker.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Temporary: returns a demo user until login is added in the security step.
 * Only this class will change then; everything else stays the same.
 */

@Service
public class CurrentUserService {

    private static final String DEMO_EMAIL = "demo@budget-tracker.local";

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createDemoUserIfMissing() {
        if (userRepository.findByEmail(DEMO_EMAIL).isEmpty()) {
            userRepository.save(new User(DEMO_EMAIL, "not-a-real-password", "Demo User"));
        }
    }

    public User getCurrentUser() {
        return userRepository.findByEmail(DEMO_EMAIL)
                .orElseThrow(() -> new IllegalStateException("Demo user is missing"));
    }
}