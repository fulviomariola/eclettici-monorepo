package it.eclettici.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO in ingresso (Payload da Postman/Frontend)
 */
public class PostRequestDto {

    @NotBlank(message = "Il titolo del post è obbligatorio")
    private String title;

    @NotBlank(message = "Il contenuto del post è obbligatorio")
    private String content;

  //  @NotNull(message = "L'ID dell'autore è obbligatorio")
  //  private UUID authorId;

    // Metodi Getter e Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

 //   public UUID getAuthorId() { return authorId; }
  //  public void setAuthorId(UUID authorId) { this.authorId = authorId; }
}