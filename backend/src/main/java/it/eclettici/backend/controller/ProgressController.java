package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Progress;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.service.ProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    @Autowired
    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * Endpoint per ottenere lo stato di completamento di un singolo video per l'utente autenticato.
     * URL: GET http://localhost:8082/api/progress/video/{videoId}
     */
    @GetMapping("/video/{videoId}")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'STORE')")
    public ResponseEntity<?> getStatoProgressoVideo(@PathVariable Long videoId, Authentication authentication) {
        User principale = (User) authentication.getPrincipal();
        UUID userId = principale.getId();

        // Chiamata al servizio per verificare lo stato
        boolean isCompleted = progressService.checkVideoCompletato(userId, videoId);

        return ResponseEntity.ok(Map.of("isCompleted", isCompleted));
    }

    /**
     * Endpoint per salvare o modificare il progresso di un video.
     * URL: POST http://localhost:8082/api/progress/video/{videoId}?completato=true
     */
    @PostMapping("/video/{videoId}")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'STORE')")
    public ResponseEntity<?> aggiornaProgresso(
            @PathVariable Long videoId,
            @RequestParam Boolean completato,
            Authentication authentication) {

        // Estraiamo l'ID dell'utente autenticato (assicurati che il tuo CustomUserDetails ritorni l'oggetto User o il suo ID)
        // Se nel tuo JwtAuthenticationFilter metti l'ID come username o nel Principal, adattalo di conseguenza.
        // Assumiamo qui di poter estrarre l'ID dell'utente dal Principal o dal tuo oggetto User.
        User principale = (User) authentication.getPrincipal();
        UUID userId = principale.getId();

        Progress progresso = progressService.aggiornaProgresso(userId, videoId, completato);

        return ResponseEntity.ok(Map.of(
                "videoId", videoId,
                "isCompleted", progresso.getCompleted(),
                "message", "Progresso aggiornato con successo"
        ));
    }

    /**
     * Endpoint per ottenere la percentuale di avanzamento totale dell'utente.
     * URL: GET http://localhost:8082/api/progress/percentuale
     */
    @GetMapping("/percentuale")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'STORE')")
    public ResponseEntity<?> getPercentualeAvanzamento(Authentication authentication) {
        User principale = (User) authentication.getPrincipal();
        UUID userId = principale.getId();

        int percentuale = progressService.calcolaPercentualeAvanzamento(userId);

        return ResponseEntity.ok(Map.of("percentuale", percentuale));
    }
}