package it.eclettici.backend.service;

import it.eclettici.backend.dto.CourseSummaryDto;
import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.Video;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final VideoRepository videoRepository;

    public CourseService(CourseRepository courseRepository, VideoRepository videoRepository) {
        this.courseRepository = courseRepository;
        this.videoRepository = videoRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseSummaryDto> getAllCourses() {
        return courseRepository.findAll().stream().map(course -> {
            long totalLessons = videoRepository.countByCourseId(course.getId());

            // Se la thumbnail del corso è vuota, usa quella della prima lezione
            String thumb = course.getThumbnailUrl();
            if (thumb == null || thumb.isBlank()) {
                List<Video> videos = videoRepository.findByCourseIdOrderByIdAsc(course.getId());
                if (!videos.isEmpty()) {
                    thumb = videos.get(0).getThumbnailUrl();
                }
            }

            return new CourseSummaryDto(
                    course.getId(),
                    course.getTitle(),
                    course.getDescription(),
                    thumb,
                    course.getYoutubePlaylistId(),
                    totalLessons,
                    Boolean.TRUE.equals(course.getIsPremium())
            );
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Corso non trovato con ID: " + courseId));
    }

    @Transactional(readOnly = true)
    public List<Video> getVideosByCourse(Long courseId, boolean isStoreOrAdmin) {
        if (isStoreOrAdmin) {
            return videoRepository.findByCourseIdOrderByIdAsc(courseId);
        }
        return videoRepository.findByCourseIdAndIsPremiumFalseOrderByIdAsc(courseId);
    }
}