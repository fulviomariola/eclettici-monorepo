package it.eclettici.backend.service;

import it.eclettici.backend.entity.Post;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.exception.ResourceNotFoundException;
import it.eclettici.backend.repository.PostRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.NoSuchElementException;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;  // Aggiunta la dipendenza per gli utenti

    // Constructor Injection di entrambi i repository
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Recupera l'autore dal DB, lo assegna al post e persiste la risorsa.
     */
    @Transactional
    public Post createPost(Post post, UUID authorid) {
        User author = userRepository.findById(authorid)
                .orElseThrow(() -> new ResourceNotFoundException("Impossibile creare il post. Utente autore non trovato con ID: " + authorid));

        post.setAuthor(author);  // Assegnazione fondamentale della relazione
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post non trovato con ID: " + id));
    }

    @Transactional
    public void deletePost(UUID id) {
        // 1. Controlliamo se il post esiste. Se non esiste, lanciamo la stessa eccezione
        // che fa svegliare il nostro "primo vigile" (che restituirà un bel 404 su Postman).
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Impossibile eliminare. Post non trovato con ID:" + id);
        }
        // 2. Se esiste, chiediamo a Hibernate di cancellarlo fisicamente dal database.
        postRepository.deleteById(id);
    }

    @Transactional
    public Post updatePost(UUID id, Post postDetails) {
        // 1. Cerco post esistente. Se non c'è scatta la NoSuchElementException
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impossibile aggiornare. Post non trovato con ID: " + id));

        // 2. Aggiorno i campi con i nuovi dati inviati dal client
        existingPost.setTitle(postDetails.getTitle());
        existingPost.setContent(postDetails.getContent());
        existingPost.setIsPremium(postDetails.getIsPremium());

        // 3. Salva modifiche nel db
        return postRepository.save(existingPost);
    }
}