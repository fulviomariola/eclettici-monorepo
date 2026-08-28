package it.eclettici.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseReviewDto {
    private Long id;
    private UUID userId;
    private String authorName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}