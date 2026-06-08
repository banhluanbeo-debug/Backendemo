package com.tranvanluan.backend.controller;

import com.tranvanluan.backend.dto.auth.GoogleLoginRequest;
import com.tranvanluan.backend.dto.auth.LoginRequest;
import com.tranvanluan.backend.dto.auth.RegisterRequest;
import com.tranvanluan.backend.entity.User;
import com.tranvanluan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

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

    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<?> sendPasswordResetOtp(@RequestBody Map<String, String> request) {
        try {
            userService.sendPasswordResetOtp(request.get("email"));
            return ResponseEntity.ok(Map.of("message", "Mã OTP đã được gửi."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPasswordWithOtp(@RequestBody Map<String, String> request) {
        try {
            userService.resetPasswordWithOtp(
                    request.get("email"),
                    request.get("otp"),
                    request.get("newPassword")
            );
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ===== Phone-based password reset =====

    @PostMapping("/forgot-password/verify-phone")
    public ResponseEntity<?> verifyPhoneForReset(@RequestBody Map<String, String> request) {
        try {
            String maskedPhone = userService.verifyPhoneForReset(request.get("email"));
            return ResponseEntity.ok(Map.of("maskedPhone", maskedPhone));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password/reset-by-phone")
    public ResponseEntity<?> resetPasswordByPhone(@RequestBody Map<String, String> request) {
        try {
            userService.resetPasswordWithPhone(
                    request.get("email"),
                    request.get("phone"),
                    request.get("newPassword")
            );
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}