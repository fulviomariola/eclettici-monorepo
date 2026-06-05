package it.eclettici.backend.controller;

import it.eclettici.backend.entity.Post;
import it.eclettici.backend.service.PostService;
import java.util.UUID;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    //public Post createPost(@RequestBody Post post) {
    public Post createPost(@Valid @RequestBody Post post) {
        return postService.createPost(post);
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/{id}")
    public Post getPostById(@PathVariable UUID id) {
        return postService.getPostById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);

        // Rispondiamo con un codice 204 (No Content).
        // Significa: "Richiesta eseguita con successo, ma non ho niente da mostrarti in risposta".
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public Post updatePost(@PathVariable UUID id, @Valid @RequestBody Post postDetails) { // Rimozine --> , PostService postService) {
        return postService.updatePost(id, postDetails);
    }
}