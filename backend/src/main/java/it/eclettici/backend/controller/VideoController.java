package it.eclettici.backend.controller;

import it.eclettici.backend.dto.VideoDto;
import it.eclettici.backend.entity.Video;
import it.eclettici.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*")
public class VideoController {

    private final VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Recupera solo i video pubblici (Accesso Libero)
     */
    @GetMapping("/pubblici")
    public ResponseEntity<List<VideoDto>> getVideosPubblici() {
        List<VideoDto> dtos = videoService.getVideosPubblici().stream()
                .map(v -> new VideoDto(
                        v.getId(), // Prende l'id dall'entità e lo mappa su videoId del DTO
                        v.getTitolo(),
                        v.getDescrizione(),
                        v.getYoutubeId(),
                        v.getThumbnailUrl(),
                        v.getPremium() != null && v.getPremium() // Evita NullPointerException se il boolean è nullo nel DB
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Recupera i video premium - Accesso consentito SOLO a STUDENT o STORE autenticati
     */
    @GetMapping("/premium")
    @PreAuthorize("hasAnyAuthority('STUDENT','STORE')")
    public ResponseEntity<List<VideoDto>> getPremiumVideos() {
        List<VideoDto> dtos = videoService.getVideosAll().stream()
                .map(v -> new VideoDto(
                        v.getId(), // Prende l'id dall'entità e lo mappa su videoId del DTO
                        v.getTitolo(),
                        v.getDescrizione(),
                        v.getYoutubeId(),
                        v.getThumbnailUrl(),
                        v.getPremium() != null && v.getPremium()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Permette l'inserimento manuale di un singolo video
     */
    @PostMapping
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<Video> salvaVideo(@RequestBody Video video) {
        if (video.getYoutubeId() == null || video.getTitolo() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(videoService.salvaVideo(video));
    }
}