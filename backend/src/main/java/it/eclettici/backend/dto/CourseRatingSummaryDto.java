package it.eclettici.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseRatingSummaryDto {
    private double averageRating;
    private long totalReviews;
    private List<CourseReviewDto> reviews;
}