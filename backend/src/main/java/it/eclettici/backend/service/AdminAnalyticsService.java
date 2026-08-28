package it.eclettici.backend.service;

import it.eclettici.backend.dto.AdminAnalyticsDto;
import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.CourseReview;
import it.eclettici.backend.entity.QuizAttempt;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.CourseReviewRepository;
import it.eclettici.backend.repository.QuizAttemptRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseReviewRepository courseReviewRepository;

    public AdminAnalyticsService(UserRepository userRepository,
                                 CourseRepository courseRepository,
                                 QuizAttemptRepository quizAttemptRepository,
                                 CourseReviewRepository courseReviewRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseReviewRepository = courseReviewRepository;
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsDto getPlatformAnalytics() {
        long totalUsers = userRepository.count();
        List<Course> courses = courseRepository.findAll();
        List<QuizAttempt> allAttempts = quizAttemptRepository.findAll();
        List<CourseReview> allReviews = courseReviewRepository.findAll();

        long totalAttempts = allAttempts.size();
        long totalCertificates = allAttempts.stream().filter(QuizAttempt::isPassed).count();

        double overallAvgRating = allReviews.stream()
                .mapToInt(CourseReview::getRating)
                .average()
                .orElse(0.0);
        overallAvgRating = Math.round(overallAvgRating * 10.0) / 10.0;

        List<AdminAnalyticsDto.CourseMetricDto> metrics = new ArrayList<>();

        for (Course course : courses) {
            List<QuizAttempt> courseAttempts = allAttempts.stream()
                    .filter(a -> a.getQuiz().getCourse().getId().equals(course.getId()))
                    .toList();

            long attemptsCount = courseAttempts.size();
            long passedCount = courseAttempts.stream().filter(QuizAttempt::isPassed).count();
            double passRate = attemptsCount > 0
                    ? Math.round(((double) passedCount / attemptsCount) * 100.0)
                    : 0.0;

            List<CourseReview> courseReviews = allReviews.stream()
                    .filter(r -> r.getCourse().getId().equals(course.getId()))
                    .toList();

            double avgCourseRating = courseReviews.stream()
                    .mapToInt(CourseReview::getRating)
                    .average()
                    .orElse(0.0);
            avgCourseRating = Math.round(avgCourseRating * 10.0) / 10.0;

            metrics.add(new AdminAnalyticsDto.CourseMetricDto(
                    course.getId(),
                    course.getTitle(),
                    course.getIsPremium() != null && course.getIsPremium(),
                    attemptsCount,
                    passedCount,
                    passRate,
                    avgCourseRating,
                    courseReviews.size()
            ));
        }

        return new AdminAnalyticsDto(
                totalUsers,
                courses.size(),
                totalAttempts,
                totalCertificates,
                overallAvgRating,
                metrics
        );
    }
}