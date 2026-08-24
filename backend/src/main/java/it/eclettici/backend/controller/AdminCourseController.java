package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Course;
import it.eclettici.backend.service.YouTubeImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/courses")
@CrossOrigin(origins = "*")
public class AdminCourseController {

    private final YouTubeImportService importService;

    public AdminCourseController(YouTubeImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/sync")
    @PreAuthorize("hasAnyRole('STORE', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> syncCourseFromPlaylist(@RequestParam String playlistId) {
        Course course = importService.syncPlaylist(playlistId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Corso '" + course.getTitle() + "' sincronizzato con successo.",
                "courseId", course.getId()
        ));
    }
}