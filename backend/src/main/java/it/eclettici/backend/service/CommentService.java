package it.eclettici.backend.service;

import it.eclettici.backend.dto.CommentResponseDto;
import it.eclettici.backend.dto.CommentRequestDto;
import it.eclettici.backend.entity.Comment;
import it.eclettici.backend.entity.Post;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.entity.Video;
import it.eclettici.backend.repository.CommentRepository;
import it.eclettici.backend.repository.PostRepository;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final VideoRepository videoRepository;

    // Costruttore per la Dependency Injection automatica di Spring
    @Autowired
    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          PostRepository postRepository,
                          VideoRepository videoRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.videoRepository = videoRepository;
    }

    /**
     * Recupera i commenti di un video e li trasforma in DTO.
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentiPerVideo(Long videoId) {
        return commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Recupera i commenti di un post e li trasforma in DTO.
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentiPerPost(UUID postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Salva un nuovo commento associandolo dinamicamente a un Post o a un Video.
     */
    @Transactional
    public CommentResponseDto salvaCommento(CommentRequestDto requestDto, UUID authenticatedUserId) {
        // Recuperiamo l'autore direttamente dal contesto di sicurezza validato
        User autore = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new RuntimeException("Utente autenticato non trovato"));

        Comment commento = new Comment();
        commento.setAuthor(autore);
        commento.setContent(requestDto.getContent());

        // LOGICA DI SMISTAMENTO: Verifica la presenza di videoId o postId
        if (requestDto.getVideoId() != null) {
            Video video = videoRepository.findById(requestDto.getVideoId())
                    .orElseThrow(() -> new RuntimeException("Video non trovato con ID: " + requestDto.getVideoId()));
            commento.setVideo(video);
        } else if (requestDto.getPostId() != null) {
            Post post = postRepository.findById(requestDto.getPostId())
                    .orElseThrow(() -> new RuntimeException("Post non trovato con ID: " + requestDto.getPostId()));
            commento.setPost(post);
        } else {
            throw new IllegalArgumentException("Il commento deve essere associato a un Video o a un Post");
        }

        Comment commentoSalvato = commentRepository.save(commento);
        return mapToResponseDto(commentoSalvato);
    }

    /**
     * Metodo helper privato per mappare l'entità nel DTO di uscita.
     */
    private CommentResponseDto mapToResponseDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setAuthorId(comment.getAuthor().getId());

        // Recupero dinamico dei dati dell'autore dal database
        dto.setAuthorName(comment.getAuthor().getNome() + " " + comment.getAuthor().getCognome());
        dto.setAuthorRole(comment.getAuthor().getRole().toString()); // Converte l'enum Role in stringa (STORE, STUDENT)

        // Mappatura condizionale delle chiavi esterne per evitare valori inconsistenti
        if (comment.getPost() != null) {
            dto.setPostId(comment.getPost().getId());
        }
        if (comment.getVideo() != null) {
            dto.setVideoId(comment.getVideo().getId());
        }
        return dto;
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