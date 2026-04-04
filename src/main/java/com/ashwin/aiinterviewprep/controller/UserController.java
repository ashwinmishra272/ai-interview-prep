package com.ashwin.aiinterviewprep.controller;

import com.ashwin.aiinterviewprep.dto.UserResponse;
import com.ashwin.aiinterviewprep.model.User;
import com.ashwin.aiinterviewprep.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/user/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        return new UserResponse(user.getName(), user.getEmail());
    }
}
