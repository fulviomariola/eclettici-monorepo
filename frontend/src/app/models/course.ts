export interface CourseSummaryDto {
  id: number;
  title: string;
  description: string;
  thumbnailUrl: string;
  youtubePlaylistId: string;
  totalLessons: number;
  isPremium: boolean;
}
