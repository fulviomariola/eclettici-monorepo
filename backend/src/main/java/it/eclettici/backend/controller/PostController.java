package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Post;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.service.PostService;
import jakarta.validation.Valid;
import it.eclettici.backend.dto.PostResponseDto;
import it.eclettici.backend.dto.PostRequestDto;
import it.eclettici.backend.dto.CommentResponseDto;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE')")  // Blindiamo l'accesso a livello di metodo
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestBody PostRequestDto requestDto,
                                                      @AuthenticationPrincipal UserDetails userDetails) {  // <--- Intercetta la sessione attiva
        Post post = new Post();
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());

        // Recuperiamo l'UUID dell'autore dal database usando l'email estratta dall'autenticazione HTTP Basic
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utente non trovato nel sistema"));

        // Passiamo l'oggetto post e l'ID dell'utente recuperato in sicurezza sul backend
        Post savedPost = postService.createPost(post, currentUser.getId());

        return new ResponseEntity<>(convertToDto(savedPost), HttpStatus.CREATED);
    }

    /**
     * LETTURA DI TUTTI I POST: Qualsiasi utente autenticato (STUDENT, STORE, ADMIN) può leggerli.
     */
    @GetMapping
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        List<PostResponseDto> responseDtos = posts.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDtos);
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

        if (post.getAuthor() != null) {
            dto.setAuthorId(post.getAuthor().getId());
        }

        // MAPPATURA DEI COMMENTI: Trasformiamo ogni entità Comment in CommentResponseDto
        if (post.getComments() != null) {
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
        }

        return dto;
    }
}



