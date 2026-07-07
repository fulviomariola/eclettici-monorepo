export interface VideoDto {
  videoId?: number;           // ID del record sul tuo database PostgreSQL
  youtubeId: string;    // L'ID del video di YouTube (es. dQw4w9WgXcQ)
  titolo: string;
  descrizione: string;
  thumbnailUrl: string;
  durata?: string;
  premium: boolean;   // Colonna fondamentale per la profilazione della Fase 4
}
