package it.eclettici.backend.controller;

import it.eclettici.backend.dto.CourseSummaryDto;
import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.Video;
import it.eclettici.backend.service.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // 1. Catalogo Corsi (Vetrina iniziale per la griglia)
    @GetMapping
    public ResponseEntity<List<CourseSummaryDto>> getCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    // 2. Info del singolo corso
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseDetail(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    // 3. Lezioni del corso selezionato per l'aula virtuale
    @GetMapping("/{id}/videos")
    public ResponseEntity<List<Video>> getCourseVideos(
            @PathVariable Long id,
            Authentication authentication) {

        boolean isStoreOrAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STORE") || a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(courseService.getVideosByCourse(id, isStoreOrAdmin));
    }
}