package com.ashwin.aiinterviewprep.controller;


import com.ashwin.aiinterviewprep.dto.AuthRequest;
import com.ashwin.aiinterviewprep.dto.AuthResponse;
import com.ashwin.aiinterviewprep.dto.SignupRequest;
import com.ashwin.aiinterviewprep.model.User;
import com.ashwin.aiinterviewprep.security.JwtService;
import com.ashwin.aiinterviewprep.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public User signup(@RequestBody SignupRequest request) {
        System.out.println("Signup request received: " + request.getEmail());
        User saved = userService.signup(request);
        System.out.println("User saved: " + saved.getEmail());
        return saved;
    }


    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        String token = jwtService.generateToken(request.getEmail());
        return new AuthResponse(token);
    }

}

