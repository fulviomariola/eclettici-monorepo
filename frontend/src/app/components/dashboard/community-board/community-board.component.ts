import { Component, OnInit, Input, OnChanges, SimpleChanges, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostService, PostResponseDto, PostRequestDto } from '../../../services/post';
import { CommentService, CommentRequestDto } from '../../../services/comment';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';

@Component({
  selector: 'app-community-board',
  standalone: true,
  imports: [CommonModule, FormsModule, ScrollFadeDirective],
  templateUrl: './community-board.component.html'
})
export class CommunityBoardComponent implements OnInit, OnChanges {
  @Input({ required: true }) currentUserId!: string;
  @Input({ required: true }) userRole!: string;
  @Input() showOnlyPrivate: boolean = false;

  private postService = inject(PostService);
  private commentService = inject(CommentService);
  private cdr = inject(ChangeDetectorRef);

  postsList: PostResponseDto[] = [];
  editingPostId: string | null = null;
  editPostData: PostRequestDto = { title: '', content: '', isPrivate: false };

  editingCommentId: string | null = null;
  editCommentData: { content: string } = { content: '' };
  commentInputs: { [postId: string]: string } = {};

  successMessage: string | null = null;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.loadPosts();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['currentUserId'] || changes['userRole']) {
      if (this.currentUserId && this.userRole) {
        this.loadPosts();
      }
    }
  }

  loadPosts(): void {
    if (!this.currentUserId || !this.userRole) return;
    this.postService.getAllPosts(this.currentUserId, this.userRole).subscribe({
      next: (data) => {
        this.postsList = data;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Errore nel caricamento dei post', err);
        this.errorMessage = 'Impossibile caricare i post del blog.';
        this.cdr.detectChanges();
      }
    });
  }

  get filterPosts(): PostResponseDto[] {
    if (this.showOnlyPrivate) {
      return this.postsList.filter(p => p.isPrivate);
    }
    return this.postsList;
  }

  onStartEdit(post: PostResponseDto): void {
    this.editingPostId = post.id;
    this.editPostData = {
      title: post.title,
      content: post.content,
      isPrivate: post.isPrivate
    };
  }

  onCancelEdit(): void {
    this.editingPostId = null;
    this.editPostData = { title: '', content: '', isPrivate: false };
  }

  onSaveEdit(postId: string): void {
    const payload = {
      id: postId,
      title: this.editPostData.title,
      content: this.editPostData.content,
      isPrivate: this.editPostData.isPrivate || false,
      authorId: this.currentUserId,
      userRole: this.userRole
    };

    this.postService.updatePost(postId, payload).subscribe({
      next: () => {
        this.successMessage = 'Post aggiornato con successo';
        this.editingPostId = null;
        this.loadPosts();
        this.cdr.detectChanges();

        setTimeout(() => {
          this.successMessage = null;
          this.cdr.detectChanges();
        }, 5000);
      },
      error: (err) => {
        console.error('Errore durante la modifica:', err);
        this.errorMessage = 'Impossibile modificare il post. Verificare i permessi.';
        this.cdr.detectChanges();
      }
    });
  }

  onDeletePost(postId: string): void {
    if (confirm('Sei sicuro di voler eliminare questo post e tutti i suoi commenti?')) {
      this.postService.deletePost(postId).subscribe({
        next: () => {
          this.loadPosts();
        },
        error: (err) => {
          console.log('Errore durante l\'eliminazione del post:', err);
          this.errorMessage = 'Impossibile eliminare il post in questo momento.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onStartEditComment(comment: any): void {
    this.editingCommentId = comment.id;
    this.editCommentData = { content: comment.content };
  }

  onCancelEditComment(): void {
    this.editingCommentId = null;
    this.editCommentData = { content: '' };
  }

  onSaveEditComment(postId: string, commentId: string): void {
    const testoPulito = this.editCommentData.content?.trim();
    if (!testoPulito) return;

    const payload: CommentRequestDto = {
      content: testoPulito,
      authorId: this.currentUserId
    };

    this.commentService.updateComment(postId, commentId, payload).subscribe({
      next: () => {
        this.editingCommentId = null;
        this.loadPosts();
      },
      error: (err) => {
        console.error('Errore durante la modifica del commento', err);
        this.errorMessage = 'Impossibile modificare il commento in questo momento.';
        this.cdr.detectChanges();
      }
    });
  }

  onAddComment(postId: string): void {
    const content = this.commentInputs[postId];
    const testoPulito = content?.trim();

    if (!testoPulito) return;

    this.commentService.createComment(postId, {
      content: testoPulito,
      authorId: this.currentUserId
    }).subscribe({
      next: () => {
        this.commentInputs[postId] = '';
        this.loadPosts();
      },
      error: (err) => {
        console.error('Errore durante l\'invio del commento', err);
        this.errorMessage = 'Impossibile pubblicare il commento in questo momento.';
        this.cdr.detectChanges();
      }
    });
  }

  onDeleteComment(postId: string, commentId: string): void {
    this.commentService.deleteComment(postId, commentId).subscribe({
      next: () => {
        this.loadPosts();
      },
      error: (err) => {
        console.log('Errore durante l\'eliminazione del commento', err);
        this.errorMessage = 'Impossibile eliminare il commento in questo momento.';
        this.cdr.detectChanges();
      }
    });
  }
}
