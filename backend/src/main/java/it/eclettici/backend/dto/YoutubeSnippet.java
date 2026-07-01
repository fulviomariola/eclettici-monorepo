package it.eclettici.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class YoutubeSnippet {
    private String title;
    private String description;
    private Thumbnails thumbnails;

    @JsonProperty("resourceId")
    private ResourceId resourceId;

    @Data
    public static class Thumbnails {
        private ThumbnailDetails medium;
        private ThumbnailDetails high;
        @JsonProperty("maxres")
        private ThumbnailDetails maxRes;

        @Data
        public static class ThumbnailDetails {
            private String url;
            private int width;
            private int height;
        }
    }

    @Data
    public static class ResourceId {
        private String kind;
        @JsonProperty("videoId")
        private String videoId; // Questo è il VERO ID del video di YouTube (es: dQw4w9WgXcQ)
    }
}
