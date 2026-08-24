package it.eclettici.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "youtube_playlist_id", unique = true, nullable = true)
    private String youtubePlaylistId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_premium", nullable = false)
    private Boolean isPremium = false;

    @Column(nullable = false)
    private String status = "PUBLISHED";
}