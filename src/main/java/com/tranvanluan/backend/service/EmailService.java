package com.tranvanluan.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        if (mailSender == null) {
            log.warn("JavaMailSender chưa được cấu hình. Bỏ qua việc gửi email thật đến {}", toEmail);
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("LunCinemas <no-reply@luncinemas.com>");
            message.setTo(toEmail);
            message.setSubject("LUNCINEMAS - Mã OTP Khôi Phục Mật Khẩu");
            message.setText("Chào bạn,\n\n" +
                    "Bạn đã yêu cầu khôi phục mật khẩu tài khoản LunCinemas. " +
                    "Dưới đây là mã OTP của bạn:\n\n" +
                    "Mã OTP: " + otp + "\n\n" +
                    "Mã này có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.\n\n" +
                    "Trân trọng,\nĐội ngũ LunCinemas.");

            mailSender.send(message);
            log.info("Đã gửi email chứa OTP đến {}", toEmail);
        } catch (Exception e) {
            log.error("Lỗi khi gửi email đến {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Không thể gửi email lúc này, vui lòng thử lại sau.");
        }
    }
}
