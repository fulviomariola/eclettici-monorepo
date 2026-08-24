package it.eclettici.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.eclettici.backend.entity.Course;
import it.eclettici.backend.entity.Video;
import it.eclettici.backend.repository.CourseRepository;
import it.eclettici.backend.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeImportService {

    @Value("${app.youtube.api-key:}")
    private String apiKey;

    private final CourseRepository courseRepository;
    private final VideoRepository videoRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public YouTubeImportService(CourseRepository courseRepository,
                                VideoRepository videoRepository) {
        this.courseRepository = courseRepository;
        this.videoRepository = videoRepository;
        this.restClient = RestClient.builder().baseUrl("https://www.googleapis.com/youtube/v3").build();
    }

    @Transactional
    public Course syncPlaylist(String rawInput) {
        // Estrae il vero ID playlist se l'utente incolla un URL intero
        String playlistId = estraiPlaylistId(rawInput);

        // 1. Recupera o crea il Corso
        Course course = courseRepository.findByYoutubePlaylistId(playlistId)
                .orElseGet(() -> {
                    Course newCourse = new Course();
                    newCourse.setYoutubePlaylistId(playlistId);
                    newCourse.setStatus("PUBLISHED");
                    newCourse.setIsPremium(false);
                    return newCourse;
                });

        // 2. Recupera metadati da YouTube (Verifica che la playlist esista)
        syncPlaylistMetadata(course, playlistId);
        course = courseRepository.save(course);

        // 3. Sincronizza i video
        syncPlaylistVideos(course, playlistId);

        return course;
    }

    private String estraiPlaylistId(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("L'ID o URL della playlist non può essere vuoto.");
        }
        input = input.trim();

        // Se contiene il parametro list= (es. https://www.youtube.com/playlist?list=PL...)
        if (input.contains("list=")) {
            Pattern pattern = Pattern.compile("[?&]list=([^&]+)");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        // Se l'utente ha inserito l'ID diretto (es. PLFv9W5SOpvJE)
        return input;
    }

    private void syncPlaylistMetadata(Course course, String playlistId) {
        try {
            String url = String.format("/playlists?part=snippet&id=%s&key=%s", playlistId, apiKey);
            String response = restClient.get().uri(url).retrieve().body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("items");

            if (!items.isArray() || items.isEmpty()) {
                throw new IllegalArgumentException("Nessuna playlist trovata su YouTube con ID: " + playlistId + ". Assicurati che sia una PLAYLIST pubblica e non il link a un singolo video.");
            }

            JsonNode snippet = items.get(0).path("snippet");

            course.setTitle(snippet.path("title").asText("Corso Senza Titolo"));
            course.setDescription(snippet.path("description").asText(""));

            JsonNode thumbs = snippet.path("thumbnails");
            String thumbUrl = thumbs.path("maxres").path("url").asText(
                    thumbs.path("high").path("url").asText(
                            thumbs.path("medium").path("url").asText("")
                    )
            );
            course.setThumbnailUrl(thumbUrl);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la chiamata alle API YouTube: " + e.getMessage(), e);
        }
    }

    private void syncPlaylistVideos(Course course, String playlistId) {
        String nextPageToken = null;
        List<Video> videosToSave = new ArrayList<>();

        try {
            do {
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append(String.format("/playlistItems?part=snippet&maxResults=50&playlistId=%s&key=%s", playlistId, apiKey));
                if (nextPageToken != null && !nextPageToken.isBlank()) {
                    urlBuilder.append("&pageToken=").append(nextPageToken);
                }

                String response = restClient.get().uri(urlBuilder.toString()).retrieve().body(String.class);
                JsonNode root = objectMapper.readTree(response);

                JsonNode items = root.path("items");
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        JsonNode snippet = item.path("snippet");
                        String youtubeId = snippet.path("resourceId").path("videoId").asText();

                        if (youtubeId == null || youtubeId.isBlank() || "Private video".equals(snippet.path("title").asText())) {
                            continue;
                        }

                        Video video = videoRepository.findByYoutubeId(youtubeId)
                                .orElseGet(() -> {
                                    Video v = new Video();
                                    v.setYoutubeId(youtubeId);
                                    v.setPremium(false);
                                    return v;
                                });

                        video.setTitolo(snippet.path("title").asText());
                        video.setDescrizione(snippet.path("description").asText());

                        JsonNode thumbs = snippet.path("thumbnails");
                        String thumb = thumbs.path("high").path("url").asText(
                                thumbs.path("medium").path("url").asText("https://img.youtube.com/vi/" + youtubeId + "/hqdefault.jpg")
                        );
                        video.setThumbnailUrl(thumb);
                        video.setCourse(course);

                        videosToSave.add(video);
                    }
                }

                nextPageToken = root.path("nextPageToken").asText(null);
            } while (nextPageToken != null && !nextPageToken.isBlank());

            videoRepository.saveAll(videosToSave);

        } catch (Exception e) {
            throw new RuntimeException("Errore sincronizzazione video: " + e.getMessage(), e);
        }
    }
}