package it.eclettici.backend.dto;

import lombok.Data;

@Data
public class YoutubeItem {
    private String id; // L'ID dell'item della playlist (diverso dall'ID del video)
    private YoutubeSnippet snippet;
}