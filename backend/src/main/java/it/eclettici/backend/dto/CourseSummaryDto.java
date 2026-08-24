package it.eclettici.backend.dto;

public record CourseSummaryDto(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        String youtubePlaylistId,
        long totalLessons,
        boolean isPremium
) {}