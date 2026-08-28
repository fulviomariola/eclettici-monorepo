package it.eclettici.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsDto {
    private long totalUsers;
    private long totalCourses;
    private long totalQuizAttempts;
    private long totalCertificatesIssued;
    private double overallAverageRating;
    private List<CourseMetricDto> courseMetrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseMetricDto {
        private Long courseId;
        private String courseTitle;
        private boolean isPremium;
        private long totalAttempts;
        private long passedAttempts;
        private double passRate;
        private double averageRating;
        private long reviewsCount;
    }
}