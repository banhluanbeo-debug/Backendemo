    package com.tranvanluan.backend.service.impl;

    import com.tranvanluan.backend.entity.PasswordResetOtp;
    import com.tranvanluan.backend.entity.User;
    import com.tranvanluan.backend.repository.PasswordResetOtpRepository;
    import com.tranvanluan.backend.repository.UserRepository;
    import com.tranvanluan.backend.service.UserService;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;

    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
    import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
    import com.google.api.client.json.jackson2.JacksonFactory;
    import com.google.api.client.http.javanet.NetHttpTransport;

    import java.util.Collections;

    import java.util.List;
    import java.util.NoSuchElementException;
    import java.util.Optional;
    import java.util.Random;
    import java.time.LocalDateTime;

    import com.tranvanluan.backend.entity.AuthProvider;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
        private final PasswordResetOtpRepository otpRepository;
        private final PasswordEncoder passwordEncoder;

        @Override
        public List<User> getAll() {
            return userRepository.findAll();
        }

        @Override
        public User getById(Long id) {
            return userRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("User not found with id " + id));
        }

        @Override
        public User create(User user) {
            user.setProvider(AuthProvider.LOCAL);
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return userRepository.save(user);
        }

        @Override
        public User update(Long id, User user) {
            User existing = getById(id);
            existing.setName(user.getName());
            existing.setEmail(user.getEmail());
            existing.setPhone(user.getPhone());
            existing.setPassword(user.getPassword());
            existing.setRole(user.getRole());
            existing.setStatus(user.getStatus());
            existing.setAvatar(user.getAvatar());
            existing.setDob(user.getDob());
            return userRepository.save(existing);
        }

        @Override
        public void delete(Long id) {
            userRepository.deleteById(id);
        }

        @Override
        public User register(User user) {
            user.setProvider(AuthProvider.LOCAL);

            // hash password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            return userRepository.save(user);
        }

        @Override
        public User login(String email, String password) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new RuntimeException("Tài khoản này được đăng ký bằng Google và chưa có mật khẩu. Vui lòng đăng nhập bằng Google.");
            }
            
            if (user.getStatus() != null && !user.getStatus()) {
                throw new RuntimeException("Tài khoản đã bị khoá");
            }

            // Tự động sửa lỗi mật khẩu cũ chưa được mã hoá (Lazy Migration)
            if (!user.getPassword().startsWith("$2a$")) {
                if (user.getPassword().equals(password)) {
                    user.setPassword(passwordEncoder.encode(password));
                    userRepository.save(user); // Cập nhật lại mật khẩu chuẩn BCrypt vào DB
                } else {
                    throw new RuntimeException("Wrong password");
                }
            } else if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Wrong password");
            }

            return user;
        }

        @Override
        public User loginWithGoogle(String email, String name, String providerId) {
            return userRepository.findByEmail(email)
                    .map(user -> {
                        if (user.getProvider() == AuthProvider.LOCAL) {
                            user.setProvider(AuthProvider.GOOGLE);
                            user.setProviderId(providerId);
                            return userRepository.save(user);
                        }
                        return user;
                    })
                    .orElseGet(() -> {
                        User newUser = User.builder()
                                .email(email)
                                .name(name)
                                .provider(AuthProvider.GOOGLE)
                                .providerId(providerId)
                                .build();
                        return userRepository.save(newUser);
                    });
        }
        
        public User loginWithGoogleToken(String token) {
            try {
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        JacksonFactory.getDefaultInstance())
.setAudience(Collections.singletonList(
    "1014240576045-mk1tmkrhqvdb9n4faldviq0odnng603e.apps.googleusercontent.com"
))                        .build();

                GoogleIdToken idToken = verifier.verify(token);

                if (idToken == null) {
                    throw new RuntimeException("Token không hợp lệ");
                }

                var payload = idToken.getPayload();

                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String providerId = payload.getSubject();

                return loginWithGoogle(email, name, providerId);

            } catch (Exception e) {
                throw new RuntimeException("Google login failed: " + e.getMessage());            }
        }

        @Override
        @Transactional
        public void sendPasswordResetOtp(String email) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Email chưa được đăng ký trong hệ thống."));

            if (user.getProvider() == AuthProvider.GOOGLE) {
                throw new RuntimeException("Tài khoản này được đăng nhập bằng Google. Vui lòng sử dụng đăng nhập Google.");
            }

            // Generate 6-digit OTP
            String otp = String.format("%06d", new Random().nextInt(1000000));
            
            // Delete old OTP if exists
            otpRepository.deleteByEmail(email);

            // Save new OTP
            PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                    .email(email)
                    .otp(otp)
                    .expiryTime(LocalDateTime.now().plusMinutes(5))
                    .build();
            otpRepository.save(resetOtp);

            // Print to console for now
            log.info("==============================================");
            log.info("MÃ OTP KHÔI PHỤC MẬT KHẨU CHO EMAIL {}: {}", email, otp);
            log.info("Mã này có hiệu lực trong 5 phút.");
            log.info("==============================================");
        }

        @Override
        @Transactional
        public void resetPasswordWithOtp(String email, String otp, String newPassword) {
            PasswordResetOtp resetOtp = otpRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu khôi phục mật khẩu cho email này."));

            if (resetOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
                otpRepository.deleteByEmail(email);
                throw new RuntimeException("Mã OTP đã hết hạn, vui lòng yêu cầu mã mới.");
            }

            if (!resetOtp.getOtp().equals(otp)) {
                throw new RuntimeException("Mã OTP không chính xác.");
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            otpRepository.deleteByEmail(email);
        }
    }
