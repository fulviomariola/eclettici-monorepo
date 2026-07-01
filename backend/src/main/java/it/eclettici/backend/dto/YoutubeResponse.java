package it.eclettici.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class YoutubeResponse {
    private List<YoutubeItem> items;
    private String nextPageToken;
    private PageInfo pageInfo;

    @Data
    public static class PageInfo {
        private int totalResults;
        private int resultsPerPage;
    }
}