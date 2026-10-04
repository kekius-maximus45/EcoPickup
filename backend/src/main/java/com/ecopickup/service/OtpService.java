package com.ecopickup.service;

import com.ecopickup.dto.AuthDtos.LoginChallengeResponse;
import com.ecopickup.model.User;
import com.ecopickup.repository.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private static final int MAX_ATTEMPTS = 5;
    private final Map<String, Challenge> challenges = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final UserRepository users;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final long expirySeconds;
    private final String mailHost;
    private final String mailFrom;

    public OtpService(UserRepository users,
                      ObjectProvider<JavaMailSender> mailSender,
                      @Value("${ecopickup.otp.expiry-seconds:300}") long expirySeconds,
                      @Value("${spring.mail.host:}") String mailHost,
                      @Value("${ecopickup.mail.from:no-reply@ecopickup.local}") String mailFrom) {
        this.users = users;
        this.mailSender = mailSender;
        this.expirySeconds = expirySeconds;
        this.mailHost = mailHost;
        this.mailFrom = mailFrom;
    }

    public synchronized LoginChallengeResponse createChallenge(User user) {
        challenges.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(Instant.now()));
        String challengeId = UUID.randomUUID().toString();
        String code = String.format("%06d", random.nextInt(1_000_000));
        challenges.put(challengeId, new Challenge(user.getId(), digest(challengeId, code), Instant.now().plusSeconds(expirySeconds), 0));

        if (mailHost.isBlank() || mailSender.getIfAvailable() == null) {
            challenges.remove(challengeId);
            throw new IllegalStateException("Email verification is not configured. Set the SMTP environment variables.");
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(user.getEmail());
            message.setSubject("Your EcoPickup verification code");
            message.setText("Your EcoPickup verification code is " + code + ". It expires in 5 minutes. Do not share this code.");
            mailSender.getObject().send(message);
        } catch (RuntimeException ex) {
            challenges.remove(challengeId);
            throw new IllegalStateException("The verification email could not be sent. Please try again.");
        }

        return new LoginChallengeResponse(challengeId, maskEmail(user.getEmail()), expirySeconds, "EMAIL");
    }

    public synchronized User verify(String challengeId, String code) {
        Challenge challenge = challenges.get(challengeId);
        if (challenge == null) throw new IllegalArgumentException("This verification request is invalid or has already been used");
        if (challenge.expiresAt().isBefore(Instant.now())) {
            challenges.remove(challengeId);
            throw new IllegalArgumentException("The verification code has expired. Please log in again");
        }
        if (!MessageDigest.isEqual(challenge.codeDigest().getBytes(StandardCharsets.UTF_8), digest(challengeId, code).getBytes(StandardCharsets.UTF_8))) {
            int attempts = challenge.attempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                challenges.remove(challengeId);
                throw new IllegalArgumentException("Too many incorrect attempts. Please log in again");
            }
            challenges.put(challengeId, new Challenge(challenge.userId(), challenge.codeDigest(), challenge.expiresAt(), attempts));
            throw new IllegalArgumentException("Incorrect verification code. " + (MAX_ATTEMPTS - attempts) + " attempts remaining");
        }
        challenges.remove(challengeId);
        return users.findById(challenge.userId()).orElseThrow(() -> new NoSuchElementException("Account not found"));
    }

    private String digest(String challengeId, String code) {
        try {
            byte[] value = MessageDigest.getInstance("SHA-256").digest((challengeId + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(value);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Verification service is unavailable");
        }
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return "***" + email.substring(Math.max(at, 0));
        return email.charAt(0) + "***" + email.substring(at);
    }

    private record Challenge(Long userId, String codeDigest, Instant expiresAt, int attempts) {}
}
