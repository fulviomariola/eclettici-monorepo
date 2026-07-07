package it.eclettici.backend.dto;

public class VideoDto {
    private Long videoId; // <--- 1. Aggiunto l'ID (Bigint -> Long)
    private String titolo;
    private String descrizione;
    private String youtubeId;
    private String thumbnailUrl;
    private boolean premium;

    // Costruttore vuoto
    public VideoDto() {}

    // 2. Costruttore aggiornato per includere il videoId
    public VideoDto(Long videoId, String titolo, String descrizione, String youtubeId, String thumbnailUrl, boolean premium) {
        this.videoId = videoId;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.youtubeId = youtubeId;
        this.thumbnailUrl = thumbnailUrl;
        this.premium = premium;
    }

    // --- 3. Getter e Setter per il videoId ---
    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    // Resto dei Getter e Setter invariati
    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public String getYoutubeId() { return youtubeId; }
    public void setYoutubeId(String youtubeId) { this.youtubeId = youtubeId; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public boolean isPremium() { return premium; }
    public void setPremium(boolean premium) { this.premium = premium; }
}