package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Comment;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.service.CommentService;
import it.eclettici.backend.repository.UserRepository;
import jakarta.validation.Valid;
import it.eclettici.backend.dto.CommentRequestDto;
import it.eclettici.backend.dto.CommentResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository; // Iniettato per estrarre l'utente autenticato

    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint per la creazione di un commento.
     * Qualsiasi utente autenticato (STUDENT, STORE, ADMIN) può commentare.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Garantisce che solo gli utenti loggati possano accedere
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Recuperiamo l'utente reale dall'email presente nel contesto di sicurezza HTTP Basic
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente autenticato non trovato nel sistema"));

        // Esecuzione della logica di business sul Service passando i dati protetti
        Comment savedComment = commentService.createComment(postId, currentUser.getId(), requestDto.getContent());

        // Mappatura pulita sul DTO di risposta
        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setId(savedComment.getId());
        responseDto.setContent(savedComment.getContent());
        responseDto.setCreatedAt(savedComment.getCreatedAt());
        responseDto.setPostId(savedComment.getPost().getId());
        responseDto.setAuthorId(savedComment.getAuthor().getId());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }
}