package it.eclettici.backend.dto;

import java.time.LocalDateTime;

public record UserCertificateDto(
        Long attemptId,
        Long courseId,
        String courseTitle,
        int score,
        LocalDateTime issuedAt
) {}