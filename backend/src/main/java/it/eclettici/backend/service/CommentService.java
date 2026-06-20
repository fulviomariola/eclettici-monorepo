package it.eclettici.backend.service;

import it.eclettici.backend.entity.Comment;
import it.eclettici.backend.entity.Post;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.repository.CommentRepository;
import it.eclettici.backend.repository.PostRepository;
import it.eclettici.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // Costruttore per la Dependency Injection automatica di Spring
    public CommentService(CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /**
     * Crea e persiste un nuovo commento associato a un post e a un autore specifico.
     */
    @Transactional
    public Comment createComment(UUID postId, UUID authorId, String content) {
        // 1. Recupera il Post di riferimento
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post non trovato con ID: " + postId));

        // 2. Recupera l'Utente che sta commentando
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato con ID: " + authorId));

        // 3. Istanziazione e configurazione del Commento
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(content);

        // 4. Persistenza a database
        return commentRepository.save(comment);
    }

    /**
     * Modificare i commento associato a un post.
     */
    public Comment updateComment(UUID commentId, String newContent) {
        // 1. Recuperiao commento esistente
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Commento non trovato con ID: " + commentId));

        // 2. Aggiornare il testo
        comment.setContent(newContent);

        // 3. Salvare/restiruire entità aggiornata
        return commentRepository.save(comment);
    }

    /**
     * Elimina commento associato a un post.
     */
    @Transactional
    public void deleteComment(UUID commentId) {
        // 1. Verifichiamo se il commento esiste davvero nel sistema
        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("Commento non trovato con ID: " + commentId);
        }
        // 2. Cancellazione effettiva dal database
        commentRepository.deleteById(commentId);
    }
}