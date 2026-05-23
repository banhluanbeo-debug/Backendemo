package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.dto.auth.GoogleLoginRequest;
import com.tranvanluan.backend.dto.auth.LoginRequest;
import com.tranvanluan.backend.dto.auth.RegisterRequest;
import com.tranvanluan.backend.entity.User;
import com.tranvanluan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .phone(request.getPhone())
                .build();

        return userService.register(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequest request) {
        return userService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/google")
    public User loginWithGoogle(@RequestBody GoogleLoginRequest request) {
        return userService.loginWithGoogleToken(request.getToken());
    }
}