    package com.tranvanluan.backend.service.impl;

    import com.tranvanluan.backend.entity.User;
    import com.tranvanluan.backend.repository.UserRepository;
    import com.tranvanluan.backend.service.UserService;
    import lombok.RequiredArgsConstructor;

    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;

    import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
    import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
    import com.google.api.client.json.jackson2.JacksonFactory;
    import com.google.api.client.http.javanet.NetHttpTransport;

    import java.util.Collections;

    import java.util.List;
    import java.util.NoSuchElementException;
    import com.tranvanluan.backend.entity.AuthProvider;

    @Service
    @RequiredArgsConstructor
    public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;
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
    }
