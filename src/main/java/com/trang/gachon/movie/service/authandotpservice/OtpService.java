package com.trang.gachon.movie.service.authandotpservice;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
/**
 * Sinh OTP 6 số, lưu tạm trong memory, gửi qua email.
 * Production nên dùng Redis thay vì HashMap.
 */
public class OtpService {
    private final JavaMailSender mailSender;

    @Value("${app.otp.expire-minutes:1}")
    private int expireMinutes;

    // key = email | value = [otp, expireTime]
    private final Map<String, String[]> otpStore = new HashMap<>();

    /**
     * Sinh OTP 6 số, lưu vào store, gửi email.
     */
    public void generateAndSend(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(expireMinutes);

        // Lưu tạm: [otp, expireTime]
        otpStore.put(email, new String[]{otp, expireAt.toString()});

        // Gửi email
        sendEmail(email, otp);

        log.info("OTP sent to {}: {} (expires at {})", email, otp, expireAt);
    }

    /**
     * Xác thực OTP: đúng mã và chưa hết hạn.
     */
    public boolean verify(String email, String inputOtp) {
        String[] stored = otpStore.get(email);
        if (stored == null) return false;

        String savedOtp    = stored[0];
        LocalDateTime expireAt = LocalDateTime.parse(stored[1]);

        if (LocalDateTime.now().isAfter(expireAt)) {
            otpStore.remove(email);
            return false;   // hết hạn
        }

        if (!savedOtp.equals(inputOtp)) {
            return false;   // sai mã
        }

        otpStore.remove(email); // dùng xong xóa đi
        return true;
    }

    private void sendEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Movie Theater - Mã xác thực OTP");
        message.setText(
                "Xin chào!\n\n" +
                        "Mã OTP của bạn là: " + otp + "\n\n" +
                        "Mã có hiệu lực trong " + expireMinutes + " phút.\n" +
                        "Vui lòng không chia sẻ mã này với bất kỳ ai.\n\n" +
                        "Movie Theater Team"
        );
        mailSender.send(message);
    }
}

