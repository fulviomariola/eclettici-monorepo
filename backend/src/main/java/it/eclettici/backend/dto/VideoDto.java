package it.eclettici.backend.dto;

public class VideoDto {
    private String titolo;
    private String descrizione;
    private String youtubeId;
    private String thumbnailUrl;
    private boolean premium;

    // Costruttori
    public VideoDto() {}

    public VideoDto(String titolo, String descrizione, String youtubeId, String thumbnailUrl, boolean premium) {
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.youtubeId = youtubeId;
        this.thumbnailUrl = thumbnailUrl;
        this.premium = premium;
    }

    // Getter e Setter
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