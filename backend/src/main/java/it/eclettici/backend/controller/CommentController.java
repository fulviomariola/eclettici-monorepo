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
    //@PreAuthorize("isAuthenticated()") // Garantisce che solo gli utenti loggati possano accedere
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequestDto requestDto) {
            // @AuthenticationPrincipal UserDetails userDetails) {

        // NUOVO CODICE FLUIDO E STATELESS:
        Comment savedComment = commentService.createComment(postId, requestDto.getAuthorId(), requestDto.getContent());
        // Esecuzione della logica di business sul Service passando i dati protetti
        //Comment savedComment = commentService.createComment(postId, currentUser.getId(), requestDto.getContent());

        // Mappatura pulita sul DTO di risposta
        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setId(savedComment.getId());
        responseDto.setContent(savedComment.getContent());
        responseDto.setCreatedAt(savedComment.getCreatedAt());
        responseDto.setPostId(savedComment.getPost().getId());
        responseDto.setAuthorId(savedComment.getAuthor().getId());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequestDto requestDto) {

        // Invocare servizio per aggiornare il testo
        Comment updateComment = commentService.updateComment(commentId, requestDto.getContent());

        // Mappare entità aggiornata sul DTO di risposta
        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setId(updateComment.getId());
        responseDto.setContent(updateComment.getContent());
        responseDto.setCreatedAt(updateComment.getCreatedAt());
        responseDto.setPostId(updateComment.getPost().getId());
        responseDto.setAuthorId(updateComment.getAuthor().getId());

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        // Invocheremo la logica sul service passando l'ID del commento da rimuovere
        commentService.deleteComment(commentId);

        // Risposta standard REST 204 per le cancellazioni andate a buon fine
        return ResponseEntity.noContent().build();
    }
}