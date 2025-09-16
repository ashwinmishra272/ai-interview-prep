package com.ashwin.aiinterviewprep.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/user/me")
    public Object getCurrentUser(Authentication authentication) {
        return authentication.getPrincipal();
    }
}
