export interface RepositoryDto {
  name: string;
  description: string;
  html_url: string;      // URL ufficiale del repository su GitHub
  language: string;      // Linguaggio principale (es. Java, TypeScript)
  stargazers_count: number;
}
