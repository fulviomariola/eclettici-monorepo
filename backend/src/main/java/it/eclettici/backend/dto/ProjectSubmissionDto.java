package it.eclettici.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSubmissionDto {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private UUID userId;
    private String studentName;
    private String studentEmail;
    private String repoUrl;
    private String notes;
    private String status;
    private String adminFeedback;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
}