package com.jvproject.club_management_system.integration.email;


import com.jvproject.club_management_system.exception.InvalidOtpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class OtpService {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN =
            Duration.ofSeconds(60);

    private static final int MAX_ATTEMPTS = 5;

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(
            StringRedisTemplate redisTemplate,
            EmailService emailService) {
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    public void sendRegistrationOtp(String email, String name) {
        String normalizedEmail = normalizeEmail(email);

        String otpKey = otpKey(normalizedEmail);
        String attemptsKey = attemptsKey(normalizedEmail);
        String cooldownKey = cooldownKey(normalizedEmail);

        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(cooldownKey, "1", RESEND_COOLDOWN);

        if (!Boolean.TRUE.equals(acquired)) {
            throw new IllegalStateException(
                    "Please wait before requesting another OTP."
            );
        }

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        String otpHash = passwordEncoder.encode(otp);

        redisTemplate.opsForValue().set(
                otpKey,
                otpHash,
                OTP_TTL
        );

        redisTemplate.delete(attemptsKey);

        try {
            emailService.sendOtpVerificationEmail(
                    normalizedEmail,
                    name,
                    otp
            );
        } catch (RuntimeException exception) {
            redisTemplate.delete(otpKey);
            redisTemplate.delete(cooldownKey);
            throw exception;
        }
    }

    public boolean verifyRegistrationOtp(String email, String submittedOtp) {
        String normalizedEmail = normalizeEmail(email);

        if (submittedOtp == null ||
                !submittedOtp.matches("\\d{6}")) {
            log.warn("subttemt otp");
            return false;
        }

        String otpKey = otpKey(normalizedEmail);
        String attemptsKey = attemptsKey(normalizedEmail);

        String storedHash = redisTemplate.opsForValue().get(otpKey);

        if (storedHash == null) {

            return false;
        }

        Long attempts = redisTemplate.opsForValue()
                .increment(attemptsKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey, OTP_TTL);
        }

        if (attempts == null || attempts > MAX_ATTEMPTS) {
            redisTemplate.delete(otpKey);


            return false;
        }


        boolean matches = passwordEncoder.matches(
                submittedOtp,
                storedHash
        );


        if (!matches) {
            if (attempts >= MAX_ATTEMPTS) {
                redisTemplate.delete(otpKey);
                redisTemplate.delete(attemptsKey);
            }

            return false;
        }

        redisTemplate.delete(otpKey);
        redisTemplate.delete(attemptsKey);


        return true;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    public void resendRegistrationOtp(String email, String fullName) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        String otpKey = "otp:register:" + normalizedEmail;
        String attemptsKey = "otp:register:attempts:" + normalizedEmail;
        String cooldownKey = "otp:register:cooldown:" + normalizedEmail;

        Boolean cooldownAcquired = redisTemplate.opsForValue()
                .setIfAbsent(
                        cooldownKey,
                        "1",
                        Duration.ofSeconds(60)
                );

        if (!Boolean.TRUE.equals(cooldownAcquired)) {
            throw new InvalidOtpException(
                    "Please wait before requesting another verification code."
            );
        }

        String otp = "%06d".formatted(
                secureRandom.nextInt(1_000_000)
        );

        try {
            redisTemplate.opsForValue().set(
                    otpKey,
                    passwordEncoder.encode(otp),
                    Duration.ofMinutes(5)
            );

            redisTemplate.delete(attemptsKey);

            emailService.sendOtpVerificationEmail(
                    normalizedEmail,
                    fullName,
                    otp
            );
        } catch (RuntimeException exception) {
            redisTemplate.delete(List.of(otpKey, attemptsKey, cooldownKey));
            throw exception;
        }
    }

    private String otpKey(String email) {
        return "otp:register:" + email;
    }

    private String attemptsKey(String email) {
        return "otp:register:attempts:" + email;
    }

    private String cooldownKey(String email) {
        return "otp:register:cooldown:" + email;
    }


}
