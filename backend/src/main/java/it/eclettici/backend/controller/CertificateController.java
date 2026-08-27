package it.eclettici.backend.controller;

import it.eclettici.backend.dto.CertificateVerifyDto;
import it.eclettici.backend.dto.UserCertificateDto;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.service.CertificateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
@CrossOrigin(origins = "*")
public class CertificateController {

    private final CertificateService certificateService;
    private final UserRepository userRepository;

    public CertificateController(CertificateService certificateService, UserRepository userRepository) {
        this.certificateService = certificateService;
        this.userRepository = userRepository;
    }

    /**
     * Download del file PDF dell'attestato per il corso completato.
     */
    @GetMapping("/course/{courseId}/download")
    @PreAuthorize("hasAnyRole('STUDENT', 'STORE', 'ADMIN')")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long courseId, Authentication authentication) {
        UUID userId = extractUserId(authentication);
        byte[] pdfBytes = certificateService.generateCertificatePdf(courseId, userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Certificato_Corso_" + courseId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    /**
     * Recupera la lista di tutti gli attestati conseguiti dall'utente loggato per la Dashboard.
     */
    @GetMapping("/my-certificates")
    @PreAuthorize("hasAnyRole('STUDENT', 'STORE', 'ADMIN')")
    public ResponseEntity<List<UserCertificateDto>> getMyCertificates(Authentication authentication) {
        UUID userId = extractUserId(authentication);
        return ResponseEntity.ok(certificateService.getUserCertificates(userId));
    }

    /**
     * Endpoint pubblico per verificare l'autenticità del certificato tramite scansione QR o ID tentativo.
     */
    @GetMapping("/verify/{attemptId}")
    public ResponseEntity<CertificateVerifyDto> verifyCertificate(@PathVariable Long attemptId) {
        return ResponseEntity.ok(certificateService.verifyCertificate(attemptId));
    }

    private UUID extractUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Utente non trovato")).getId();
    }
}