// import {Component, ElementRef, inject, OnDestroy, OnInit} from '@angular/core';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ContactFormComponent } from '../contact-form/contact-form';
import {ScrollFadeDirective} from '../../directives/scroll-fade.directive';    // Controlla il percorso esatto

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, ContactFormComponent, ScrollFadeDirective],  // Importiamo il form dei contatti per usarlo nel template
  templateUrl: './home.html'
})

export class HomeComponent {

}
