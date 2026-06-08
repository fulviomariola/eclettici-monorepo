package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Comment;
import it.eclettici.backend.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    // Constructor Injection per garantire l'immutabilità della dipendenza
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Endpoint REST per la creazione e l'associazione di un commento a un determinato post.
     * Rotta: POST /api/posts/{postId}/comments
     */
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequestDto requestDto) {

        // 1. Logica di business: salvataggio del commento tramite il servizio
        Comment savedComment = commentService.createComment(postId, requestDto.getAuthorId(), requestDto.getContent());

        // 2. Mappatura dall'Entità ricorsiva al DTO piatto e pulito
        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setId(savedComment.getId());
        responseDto.setContent(savedComment.getContent());
        responseDto.setCreatedAt(savedComment.getCreatedAt());
        responseDto.setPostId(savedComment.getPost().getId());       // Estrae solo l'ID del post
        responseDto.setAuthorId(savedComment.getAuthor().getId());   // Estrae solo l'ID dell'autore

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * DTO in INGRESSO (Ricezione dati da Postman/Frontend)
     */
    public static class CommentRequestDto {
        @NotNull(message = "L'ID dell'autore è obbligatorio")
        private UUID authorId;

        @NotBlank(message = "Il contenuto del commento non può essere vuoto")
        private String content;

        public UUID getAuthorId() { return authorId; }
        public void setAuthorId(UUID authorId) { this.authorId = authorId; }

        public String getContent() { return  content; }
        public void setContent(String content) { this.content = content; }
    }

    /**
     * DTO in USCITA (Risposta pulita per Postman/Frontend)
     * Risolve il bug della ricorsione circolare (3800 righe).
     */
    public static class CommentResponseDto {
        private UUID id;
        private String content;
        private LocalDateTime createdAt;
        private UUID postId;
        private UUID authorId;

        // Getter e Setter
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public UUID getPostId() { return postId; }
        public void setPostId(UUID postId) { this.postId = postId; }

        public UUID getAuthorId() { return authorId; }
        public void setAuthorId(UUID authorId) { this.authorId = authorId; }
    }
}