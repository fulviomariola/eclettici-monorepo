package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Video;
import it.eclettici.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin(origins = "*") // Permette ad Angular di connettersi senza blocchi CORS
public class VideoController {

    private final VideoService videoService;

    @Autowired
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/pubblici")
    public ResponseEntity<List<Video>> getVideosPubblici() {
        return ResponseEntity.ok(videoService.getVideosPubblici());
    }

    @GetMapping("/premium")
    public ResponseEntity<List<Video>> getVideosAll() {
        return ResponseEntity.ok(videoService.getVideosAll());
    }

    @PostMapping
    public ResponseEntity<Video> salvaVideo(@RequestBody Video video) {
        return ResponseEntity.ok(videoService.salvaVideo(video));
    }
}