package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Course;
import it.eclettici.backend.service.YouTubeImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/videos")
@CrossOrigin(origins = "*")
public class VideoAdminController {

    private final YouTubeImportService youtubeImportService;

    // Iniezione delle dipendenze di Spring
    public VideoAdminController(YouTubeImportService youtubeImportService) {
        this.youtubeImportService = youtubeImportService;
    }

    @PostMapping("/import-playlist")
    public ResponseEntity<?> importaPlaylist(
            @RequestParam String playlistId,
            @RequestParam(defaultValue = "false") boolean isPremium) {

        // Chiama il service, che fa il lavoro e restituisce l'entità salvata
        Course course = youtubeImportService.syncPlaylist(playlistId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Playlist sincronizzata con successo per il corso: " + course.getTitle(),
                "playlistId", playlistId,
                "courseId", course.getId()
        ));
    }
}