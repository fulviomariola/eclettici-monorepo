export interface CommentRequestDto {
  content: string;
  authorId?: string; // Opzionale: il backend lo estrae già in sicurezza dal JWT
  postId?: string;   // Opzionale: valorizzato se commentiamo un post della Dashboard (UUID)
  videoId?: number;  // Opzionale: valorizzato se commentiamo una videolezione (Long)
}

export interface CommentResponseDto {
  id: string;        // UUID del commento generato dal DB
  content: string;
  createdAt: string; // Data ISO in formato stringa
  authorId: string;  // UUID dell'autore
  authorName: string;
  authorRole: string;
  postId?: string;   // Opzionale: presente se legato a un post
  videoId?: number;  // Opzionale: presente se legato a un video
}
