package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Comment;
import it.eclettici.backend.service.CommentService;
import jakarta.validation.Valid;
import it.eclettici.backend.dto.CommentRequestDto;
import it.eclettici.backend.dto.CommentResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}