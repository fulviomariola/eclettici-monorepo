package it.eclettici.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectReviewRequestDto {
    private String status; // APPROVED oppure REJECTED
    private String feedback;
}