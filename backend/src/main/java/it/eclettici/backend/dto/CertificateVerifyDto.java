package it.eclettici.backend.dto;

import java.time.LocalDateTime;

public record CertificateVerifyDto(
        Long attemptId,
        String studentName,
        String courseTitle,
        int score,
        boolean passed,
        LocalDateTime issuedAt
) {}