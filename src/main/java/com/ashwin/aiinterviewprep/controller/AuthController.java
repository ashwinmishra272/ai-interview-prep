package com.ashwin.aiinterviewprep.controller;


import com.ashwin.aiinterviewprep.dto.AuthRequest;
import com.ashwin.aiinterviewprep.dto.AuthResponse;
import com.ashwin.aiinterviewprep.model.User;
import com.ashwin.aiinterviewprep.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        return userService.signup(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return userService.login(request);
    }
}

