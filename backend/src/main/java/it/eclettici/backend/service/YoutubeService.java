package it.eclettici.backend.service;

import it.eclettici.backend.dto.YoutubeItem;
import it.eclettici.backend.dto.YoutubeResponse;
import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.Video;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class YoutubeService {

    private final WebClient webClient;
    private final CourseRepository courseRepository;
    private final VideoRepository videoRepository;

    @Value("${app.youtube.api-key}")
    private String apiKey;

    public YoutubeService(CourseRepository courseRepository, VideoRepository videoRepository) {
        this.webClient = WebClient.create("https://www.googleapis.com/youtube/v3");
        this.courseRepository = courseRepository;
        this.videoRepository = videoRepository;
    }

    /**
     * Sincronizza i video di una specifica playlist di YouTube salvandoli nel DB.
     */
    @Transactional
    public void syncPlaylist(String youtubePlaylistId) {
        System.out.println("--- [DEBUG 1] Inizio syncPlaylist per ID: " + youtubePlaylistId);

        // 1. Cerchiamo il corso corrispondente nel database
        Course course = courseRepository.findByYoutubePlaylistId(youtubePlaylistId)
                .orElseThrow(() -> {
                    System.out.println("--- [DEBUG ERRORE] Corso non trovato a DB per ID: " + youtubePlaylistId);
                    return new RuntimeException("Course not found for playlist ID: " + youtubePlaylistId);
                });

        System.out.println("--- [DEBUG 2] Corso trovato nel DB: " + course.getTitle());

        List<Video> videosToSave = new ArrayList<>();
        String nextPageToken = null;

        do {
            final String currentToken = nextPageToken;

            System.out.println("--- [DEBUG 3] Sto per invocare WebClient. Chiave API presente? " + (apiKey != null ? "SI" : "NO"));

            // 2. Chiamata HTTP asincrona/reattiva bloccata sul thread
            YoutubeResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/playlistItems")
                            .queryParam("part", "snippet")
                            .queryParam("maxResults", 50)
                            .queryParam("playlistId", youtubePlaylistId)
                            .queryParam("key", apiKey)
                            .queryParam("pageToken", currentToken)
                            .build())
                    .retrieve()
                    .bodyToMono(YoutubeResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            System.out.println("--- [DEBUG 4] Risposta ricevuta da Google Cloud!");

            if (response != null && response.getItems() != null) {
                System.out.println("--- [DEBUG 5] Elementi trovati nella pagina corrente: " + response.getItems().size());

                for (YoutubeItem item : response.getItems()) {
                    if (item.getSnippet() == null || item.getSnippet().getResourceId() == null) continue;

                    String youtubeId = item.getSnippet().getResourceId().getVideoId();

                    // 3. Evitiamo duplicati
                    if (!videoRepository.existsByYoutubeId(youtubeId)) {
                        Video video = new Video();
                        video.setYoutubeId(youtubeId);
                        video.setTitolo(item.getSnippet().getTitle());
                        video.setDescrizione(item.getSnippet().getDescription());

                        String thumbUrl = "";
                        if (item.getSnippet().getThumbnails() != null) {
                            thumbUrl = item.getSnippet().getThumbnails().getMaxRes() != null ?
                                    item.getSnippet().getThumbnails().getMaxRes().getUrl() :
                                    item.getSnippet().getThumbnails().getMedium() != null ?
                                            item.getSnippet().getThumbnails().getMedium().getUrl() : "";
                        }
                        video.setThumbnailUrl(thumbUrl);
                        video.setCourse(course);
                        video.setPremium(false);

                        videosToSave.add(video);
                    }
                }
                nextPageToken = response.getNextPageToken();
                System.out.println("--- [DEBUG 6] Fine pagina corrente. Prossimo token: " + nextPageToken);
            } else {
                System.out.println("--- [DEBUG 6-VUOTO] Risposta o elementi nulli da YouTube.");
                nextPageToken = null;
            }

        } while (nextPageToken != null);

        System.out.println("--- [DEBUG 7] Uscito dal ciclo. Video totali da salvare in batch: " + videosToSave.size());

        // 4. Salvataggio massivo
        if (!videosToSave.isEmpty()) {
            videoRepository.saveAll(videosToSave);
            System.out.println("--- [DEBUG 8] saveAll eseguito correttamente!");
        } else {
            System.out.println("--- [DEBUG 8-SKIP] Nessun nuovo video da salvare.");
        }
    }
}