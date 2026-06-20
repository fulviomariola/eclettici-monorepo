package it.eclettici.backend.service;

import it.eclettici.backend.dto.PostResponseDto;
import it.eclettici.backend.entity.Post;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.enums.Role;
import it.eclettici.backend.exception.ResourceNotFoundException;
import it.eclettici.backend.repository.PostRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;

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

    @Transactional
    public Post updatePost(UUID postId, PostResponseDto updateDto, UUID currentUserId) {
        // 1. Cercare  post nel DB
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post non trovato con id: " + postId));

        // 2. CONTROLLO DI SICUREZZA: l'utente loggato è autore del post?
        if(!post.getAuthor().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Non hai i permessi per modificare questo post!");
        }

        // 3. Aggiorno i campi consentiti
        post.setTitle(updateDto.getTitle());
        post.setContent(updateDto.getContent());
        post.setIsPrivate(updateDto.getIsPrivate());  // Lo STORE può decidere se rendere privato oppure no un post che era pubblico

        return postRepository.save(post);
    }

    @Transactional
    public List<Post> getAllPosts(UUID currentUserId, Role userRole) {
        // 1. Recuperiamo tutti i post ordinati dal più recente (la tua riga attuale)
        List<Post> allPosts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        // 2. Se l'utente è ADMIN, vede TUTTO senza filtri
        if (userRole == Role.ADMIN) {
            return allPosts;
        }

        // 3. Se è STORE o STUDENT, applichiamo il filtro di riservatezza
        return allPosts.stream()
                .filter(post ->
                        !post.getIsPrivate() ||
                        post.getAuthor().getId().equals(currentUserId)
                )
                .collect(Collectors.toList());
    }

    public Post getPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post non trovato con ID: " + id));
    }

    @Transactional
    public void deletePost(UUID postId) {
        // 1. Controlliamo se il post esiste. Se non esiste, lanciamo la stessa eccezione
        // che fa svegliare il nostro "primo vigile" (che restituirà un bel 404 su Postman).
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post non trovato con id: " + postId);
        }
        // 2. Se esiste, chiediamo a Hibernate di cancellarlo fisicamente dal database.
        postRepository.deleteById(postId);
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