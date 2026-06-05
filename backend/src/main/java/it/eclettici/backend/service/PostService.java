package it.eclettici.backend.service;

import it.eclettici.backend.entity.Post;
import it.eclettici.backend.repository.PostRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.NoSuchElementException;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    // Aggiungere questo metodo all'interno di PostService
    public Post createPost(Post post) {
        // il metodo save() è fornito automaticamente da Spring Data JPA
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(UUID id) {
        // findById cerca nel DB. orElseThrow() lancia un'eccezione se non lo trova.
        return postRepository.findById(id).orElseThrow();
    }

    public void deletePost(UUID id) {
        // 1. Controlliamo se il post esiste. Se non esiste, lanciamo la stessa eccezione
        // che fa svegliare il nostro "primo vigile" (che restituirà un bel 404 su Postman).
        if (!postRepository.existsById(id)) {
            throw new NoSuchElementException();
        }

        // 2. Se esiste, chiediamo a Hibernate di cancellarlo fisicamente dal database.
        postRepository.deleteById(id);
    }

    public Post updatePost(UUID id, Post postDetails) {
        // 1. Cerco post esistente. Se non c'è scatta la NoSuchElementException
        Post existingPost = postRepository.findById(id).orElseThrow();

        // 2. Aggiorno i campi con i nuovi dati inviati dal client
        existingPost.setTitle(postDetails.getTitle());
        existingPost.setContent(postDetails.getContent());

        // 3. Salva modifiche nel db
        return postRepository.save(existingPost);
    }
}