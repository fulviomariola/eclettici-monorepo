package it.eclettici.backend.controller;

import it.eclettici.backend.service.YoutubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// @PreAuthorize("hasRole('STORE')") // Blocca l'accesso a livello di classe: solo gli utenti STORE possono entrare
@RestController
@RequestMapping("/api/admin/courses")
@CrossOrigin(origins = "http://localhost:4200") // <-- 1. ABILITA IL CORS PER QUESTO CONTROLLER
public class AdminCourseController {

    private final YoutubeService youtubeService;

    public AdminCourseController(YoutubeService youtubeService) {
        this.youtubeService = youtubeService;
    }

    /**
     * Endpoint per avviare la sincronizzazione di una playlist.
     * URL: POST /api/admin/courses/sync?playlistId=PLFv9W5SOpvJE
     */
    @PostMapping("/sync")
    @PreAuthorize("hasRole('STORE')")
    public ResponseEntity<?> syncYoutubePlaylist(@RequestParam String playlistId) {
        System.out.println("--- [CONTROLLER DEBUG 1] Ricevuta richiesta POST per playlistId: " + playlistId);
        try {
            System.out.println("--- [CONTROLLER DEBUG 2] Sto per invocare youtubeService.syncPlaylist...");
            youtubeService.syncPlaylist(playlistId);

            System.out.println("--- [CONTROLLER DEBUG 3] Sincronizzazione terminata con successo nel service.");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Sincronizzazione completata con successo per la playlist: " + playlistId
            ));
        } catch (RuntimeException e) {
            // Se il corso non viene trovato o l'API di YouTube fallisce
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            // Errore generico di sistema
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Errore interno durante la sincronizzazione: " + e.getMessage()
            ));
        }
    }
}