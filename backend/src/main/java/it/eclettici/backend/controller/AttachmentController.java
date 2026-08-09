package it.eclettici.backend.controller;

import it.eclettici.backend.dto.AttachmentResponseDto;
import it.eclettici.backend.entity.Attachment;
import it.eclettici.backend.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/debug-auth")
    public ResponseEntity<?> debugAuth() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return ResponseEntity.ok(java.util.Map.of("message", "Nessun utente autenticato"));
        }

        return ResponseEntity.ok(java.util.Map.of(
                "username", auth.getName(),
                "authorities", auth.getAuthorities().stream()
                        .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                        .toList()
        ));
    }



    /**
     * Upload di un nuovo allegato legato a un video specifico.
     * Solo lo STORE (Amministratore) può caricare file.
     */
    @PreAuthorize("hasRole('STORE')")
    @PostMapping("/videos/{videoId}/attachments")
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @PathVariable Long videoId,
            @RequestParam("file") MultipartFile file) {
        try {
            AttachmentResponseDto response = attachmentService.saveAttachment(videoId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recupera la lista dei metadati degli allegati per un video specifico.
     * Accessibile a tutti (anonimi e registrati).
     */
    @GetMapping("/videos/{videoId}/attachments")
    public ResponseEntity<List<AttachmentResponseDto>> getAttachments(@PathVariable Long videoId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByVideo(videoId));
    }

    /**
     * Download sicuro di un file fisico tramite il suo UUID.
     * Accessibile a chiunque sia abilitato alla visualizzazione delle lezioni.
     */
    @GetMapping("/attachments/{id}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable UUID id) {
        try {
            Attachment attachment = attachmentService.getPhysicalAttachment(id);
            Path path = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(attachment.getFileType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminazione di un allegato.
     * Solo lo STORE (Amministratore) può cancellare i file.
     */
    @PreAuthorize("hasRole('STORE')")
    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID id) {
        try {
            attachmentService.deleteAttachment(id);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}