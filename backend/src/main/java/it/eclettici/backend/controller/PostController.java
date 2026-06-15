package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Post;
import it.eclettici.backend.service.PostService;
import jakarta.validation.Valid;
import it.eclettici.backend.dto.PostResponseDto;
import it.eclettici.backend.dto.PostRequestDto;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    /**
     * CREAZIONE POST: Riceve il RequestDto pubblico e restituisce il ResponseDto piatto.
     */
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(@Valid @RequestBody PostRequestDto requestDto) {
        Post post = new Post();
        post.setTitle(requestDto.getTitle());
        post.setContent(requestDto.getContent());

        // Invocazione del servizio con l'entità leggera e l'ID dell'autore separato
        Post savedPost = postService.createPost(post, requestDto.getAuthorId());

        return new ResponseEntity<>(convertToDto(savedPost), HttpStatus.CREATED);
    }

    /**
     * LETTURA DI TUTTI I POST: Trasforma l'intera lista di entità in una lista di DTO puliti.
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

        return dto;
    }
}



















