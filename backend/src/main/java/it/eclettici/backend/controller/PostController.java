package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Post;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.enums.Role;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.service.PostService;
import jakarta.validation.Valid;
import it.eclettici.backend.dto.PostResponseDto;
import it.eclettici.backend.dto.PostRequestDto;
import it.eclettici.backend.dto.CommentResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;

    public PostController(PostService postService, UserRepository userRepository) {
        this.postService = postService;
        this.userRepository = userRepository;
    }

    /**
     * CREAZIONE POST: Solo ADMIN e STORE possono creare post.
     * Recupera l'utente autenticato da Spring Security tramite @AuthenticationPrincipal.
     */
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(
            @Valid @RequestBody PostRequestDto requestDto) {

        // 1. Creiamo l'oggetto Post (Entità) e gli diamo i dati del DTO
        Post postEntity = new Post();
        postEntity.setTitle(requestDto.getTitle());
        postEntity.setContent(requestDto.getContent());
        postEntity.setIsPrivate(requestDto.getIsPrivate() != null ? requestDto.getIsPrivate() : false);

        // 2. Ora passiamo i DUE parametri corretti che il Service si aspetta (l'entità + l'UUID)
        Post savedPost = postService.createPost(postEntity, requestDto.getAuthorId());

        // 3. Mappiamo il risultato sul DTO di risposta per Angular
        PostResponseDto responseDto = new PostResponseDto();
        responseDto.setId(savedPost.getId());
        responseDto.setTitle(savedPost.getTitle());
        responseDto.setContent(savedPost.getContent());
        responseDto.setIsPrivate(savedPost.getIsPrivate());
        responseDto.setAuthorId(savedPost.getAuthor().getId());
        responseDto.setAuthorEmail(savedPost.getAuthor().getEmail());

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    /**
     * LETTURA DI TUTTI I POST: Qualsiasi utente autenticato (STUDENT, STORE, ADMIN) può leggerli.
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<PostResponseDto>> getAllPosts(
            @RequestParam UUID currentUserId,
            @RequestParam Role userRole) {
        // 1. Passiamo i parametri richiesti al Service per filtrare i dati a monte
        List<Post> posts = postService.getAllPosts(currentUserId, userRole);

        // 2. Trasformiamo la lista di entità in una lista di DTO protetti
        List<PostResponseDto> dtos = posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody PostResponseDto updateDto) {

        // 1. Convertiamo la stringa del ruolo che arriva da Angular nel nostro Enum Java
        // Se il metodo si chiama in modo diverso nel tuo DTO (es. getRole()), usa quello.
        Role roleEnum = Role.valueOf(updateDto.getUserRole());

        // 2. Passiamo al servizio i 4 parametri ora richiesti:
        // ID del post, il DTO con i dati, l'ID dell'utente corrente e l'Enum del ruolo
        Post updatedPost = postService.updatePost(id, updateDto, updateDto.getAuthorId(), roleEnum);

        // 3. Prepariamo l'oggetto di risposta per Angular
        PostResponseDto responseDto = new PostResponseDto();
        responseDto.setId(updatedPost.getId());
        responseDto.setTitle(updatedPost.getTitle());
        responseDto.setContent(updatedPost.getContent());
        responseDto.setIsPrivate(updateDto.getIsPrivate());
        responseDto.setAuthorId(updatedPost.getAuthor().getId());
        responseDto.setAuthorEmail(updatedPost.getAuthor().getEmail());

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Metodo di utilità interno per mappare l'Entità complessa sul DTO piatto.
     * Isola l'ID dell'autore interrompendo ogni potenziale ricorsione con i commenti.
     */
    private PostResponseDto convertToDto(Post post) {
        PostResponseDto dto = new PostResponseDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setPremium(post.getIsPremium());
        dto.setIsPrivate(post.getIsPrivate());

        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
            dto.setAuthorEmail(post.getAuthor().getEmail());
        }

        // MAPPATURA DEI COMMENTI: Trasformiamo ogni entità Comment in CommentResponseDto
       // if (post.getComments() != null) {
        if (post.getComments() != null && !post.getComments().isEmpty()) {
            List<CommentResponseDto> commentDtos = post.getComments().stream()
                    .map(comment -> {
                        CommentResponseDto cDto = new CommentResponseDto();
                        cDto.setId(comment.getId());
                        cDto.setContent(comment.getContent());
                        cDto.setCreatedAt(comment.getCreatedAt());
                        cDto.setPostId(comment.getPost().getId());
                        cDto.setAuthorId(comment.getAuthor().getId());

                        return  cDto;
                    })
                    .collect(Collectors.toList());
            dto.setComments(commentDtos);
        } else {
            dto.setComments(new ArrayList<>());
        }

        return dto;
    }
}



