package com.tranvanluan.backend.service;

import com.tranvanluan.backend.entity.User;
import java.util.List;

public interface UserService {
    List<User> getAll();

    User getById(Long id);

    User create(User user);

    User update(Long id, User user);

    void delete(Long id);

    User register(User user);

    User login(String email, String password);

    User loginWithGoogle(String email, String name, String providerId);

    User loginWithGoogleToken(String token);

    void sendPasswordResetOtp(String email);

    void resetPasswordWithOtp(String email, String otp, String newPassword);
}
