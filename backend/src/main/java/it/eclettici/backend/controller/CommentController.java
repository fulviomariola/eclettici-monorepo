package it.eclettici.backend.controller;

import it.eclettici.backend.dto.CommentRequestDto;
import it.eclettici.backend.dto.CommentResponseDto;
import it.eclettici.backend.entity.Comment;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comments") // 1. Rotta di base generica e pulita
@CrossOrigin(origins = "*")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * ================= CONTESTO VIDEOLEZIONI =================
     */

    /**
     * Recupera tutti i commenti di una specifica videolezione.
     * URL: GET http://localhost:8082/api/comments/video/{videoId}
     */
    @GetMapping("/video/{videoId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentiPerVideo(@PathVariable Long videoId) {
        List<CommentResponseDto> commenti = commentService.getCommentiPerVideo(videoId);
        return ResponseEntity.ok(commenti);
    }


    /**
     * ================= CONTESTO DASHBOARD (POST) =================
     */

    /**
     * Recupera tutti i commenti di un determinato Post della Dashboard.
     * URL: GET http://localhost:8082/api/comments/post/{postId}
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentiPerPost(@PathVariable UUID postId) {
        List<CommentResponseDto> commenti = commentService.getCommentiPerPost(postId);
        return ResponseEntity.ok(commenti);
    }


    /**
     * ================= AZIONI COMUNI (CREA, MODIFICA, CANCELLA) =================
     */

    /**
     * Endpoint unico per la creazione di un commento (smistato internamente dal Service).
     * URL: POST http://localhost:8082/api/comments
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('STUDENT', 'STORE')")
    public ResponseEntity<CommentResponseDto> createComment(
            @Valid @RequestBody CommentRequestDto requestDto,
            Authentication authentication) {

        // Estrazione stateless dell'utente autenticato dal JWT
        User principale = (User) authentication.getPrincipal();
        UUID authenticatedUserId = principale.getId();

        CommentResponseDto responseDto = commentService.salvaCommento(requestDto, authenticatedUserId);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * Modifica un commento esistente.
     * URL: PUT http://localhost:8082/api/comments/{commentId}
     */
    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'STORE')")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentRequestDto requestDto) {

        Comment updateComment = commentService.updateComment(commentId, requestDto.getContent());

        CommentResponseDto responseDto = new CommentResponseDto();
        responseDto.setId(updateComment.getId());
        responseDto.setContent(updateComment.getContent());
        responseDto.setCreatedAt(updateComment.getCreatedAt());
        if (updateComment.getPost() != null) responseDto.setPostId(updateComment.getPost().getId());
        if (updateComment.getVideo() != null) responseDto.setVideoId(updateComment.getVideo().getId());
        responseDto.setAuthorId(updateComment.getAuthor().getId());

        return ResponseEntity.ok(responseDto);
    }

    /**
     * Elimina un commento.
     * URL: DELETE http://localhost:8082/api/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'STORE')")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}