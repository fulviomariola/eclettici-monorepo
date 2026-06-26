package it.eclettici.backend.service;

import it.eclettici.backend.entity.Video;
import it.eclettici.backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VideoService {

    private final VideoRepository videoRepository;

    @Autowired
    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    /**
     * Ritorna solo i video gratuiti (isPremium = false) per utenti anonimi
     */
    public List<Video> getVideosPubblici() {
        return videoRepository.findByIsPremiumFalse();
    }

    /**
     * Ritorna tutti i video del catalogo (gratis + premium) per gli utenti autenticati
     */
    public List<Video> getVideosAll() {
        return videoRepository.findAll();
    }

    /**
     * Salva o aggiorna un video nel database
     */
    public Video salvaVideo(Video video) {
        // Evitiamo duplicati: se il video esiste già con lo stesso youtubeId, aggiorna il record esistente
        Optional<Video> existingVideo = videoRepository.findByYoutubeId(video.getYoutubeId());
        if (existingVideo.isPresent()) {
            Video v = existingVideo.get();
            v.setTitolo(video.getTitolo());
            v.setDescrizione(video.getDescrizione());
            v.setThumbnailUrl(video.getThumbnailUrl());
            v.setPremium(video.isPremium());
            return videoRepository.save(v);
        }
        return videoRepository.save(video);
    }
}