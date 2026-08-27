package it.eclettici.backend.service;

import it.eclettici.backend.entity.Progress;
import it.eclettici.backend.entity.User;
import it.eclettici.backend.entity.Video;
import it.eclettici.backend.repository.ProgressRepository;
import it.eclettici.backend.repository.UserRepository;
import it.eclettici.backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    @Autowired
    public ProgressService(ProgressRepository progressRepository,
                           UserRepository userRepository,
                           VideoRepository videoRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    /**
     * Verifica se un determinato video risulta completato da un utente.
     */
    @Transactional(readOnly = true)
    public boolean checkVideoCompletato(UUID userId, Long videoId) {
        return progressRepository.findByUserIdAndVideoId(userId, videoId)
                .map(Progress::getCompleted)
                .orElse(false);
    }

    /**
     * Recupera la lista degli ID video completati da un utente per uno specifico corso.
     */
    @Transactional(readOnly = true)
    public List<String> getCompletedLessonsByCourse(UUID userId, Long courseId) {
        return progressRepository.findCompletedVideoIdsByUserIdAndCourseId(userId, courseId);
    }

    /**
     * Salva o aggiorna lo stato di completamento di un video per un utente.
     */
    @Transactional
    public Progress aggiornaProgresso(UUID userId, Long videoId, Boolean completato) {
        return progressRepository.findByUserIdAndVideoId(userId, videoId)
                .map(progress -> {
                    progress.setCompleted(completato);
                    return progressRepository.save(progress);
                })
                .orElseGet(() -> {
                    Progress nuovoProgresso = new Progress();

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("Utente non trovato"));
                    Video video = videoRepository.findById(videoId)
                            .orElseThrow(() -> new RuntimeException("Video non trovato"));

                    nuovoProgresso.setUser(user);
                    nuovoProgresso.setVideo(video);
                    nuovoProgresso.setCompleted(completato);

                    return progressRepository.save(nuovoProgresso);
                });
    }

    /**
     * Calcola la percentuale di completamento dei video totali per un utente.
     */
    @Transactional(readOnly = true)
    public int calcolaPercentualeAvanzamento(UUID userId) {
        long totalVideo = videoRepository.count();
        if (totalVideo == 0) {
            return 0;
        }

        List<Progress> progressiUtente = progressRepository.findByUserId(userId);

        long videoCompletati = progressiUtente.stream()
                .filter(Progress::getCompleted)
                .count();

        return (int) ((videoCompletati * 100) / totalVideo);
    }
}