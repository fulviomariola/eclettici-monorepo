import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PostResponseDto } from '../../../services/post';
import { VideoDto } from '../../../models/video';
import { ScrollFadeDirective } from '../../../directives/scroll-fade.directive';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, ScrollFadeDirective],
  templateUrl: './admin-panel.component.html'
})
export class AdminPanelComponent {
  @Input({ required: true }) postsList: PostResponseDto[] = [];
  @Input({ required: true }) videosList: VideoDto[] = [];
  @Input() showOnlyPrivate: boolean = false;

  @Output() filterToggled = new EventEmitter<boolean>();

  get privatePostsCount(): number {
    return this.postsList.filter(p => p.isPrivate).length;
  }

  togglePrivateFilter(): void {
    this.showOnlyPrivate = !this.showOnlyPrivate;
    this.filterToggled.emit(this.showOnlyPrivate);
  }
}
