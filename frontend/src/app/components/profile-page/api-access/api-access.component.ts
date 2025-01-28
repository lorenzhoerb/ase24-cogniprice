import {Component, OnInit} from '@angular/core';
import {MatGridList, MatGridTile} from '@angular/material/grid-list';
import {ApiDocsComponent} from './api-docs/api-docs.component';
import {NgIf, formatDate} from '@angular/common';
import {MatButton, MatMiniFabButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {MatDialog} from '@angular/material/dialog';
import {CopyApiKeyModalComponent} from './copy-api-key-modal/copy-api-key-modal.component';
import {ApiKeyInfo} from '../../../models/ApiKeyInfo';
import {UserDetailsService} from '../../../services/user-details/user-details.service';
import {SnackbarService} from '../../../shared/utils/snackbar/snackbar.service';

@Component({
  selector: 'app-api-access',
  standalone: true,
  imports: [
    MatGridList,
    MatGridTile,
    ApiDocsComponent,
    NgIf,
    MatButton,
    MatMiniFabButton,
    MatIcon
  ],
  templateUrl: './api-access.component.html',
  styleUrl: './api-access.component.scss'
})
export class ApiAccessComponent implements OnInit {
  activeTab: 'apiKey' | 'documentation' = 'documentation';
  apiKeyInfo: ApiKeyInfo | null = null;

  constructor(private dialog: MatDialog,
              private userDetailsService: UserDetailsService,
              private snackBarService: SnackbarService
  ) {
  }

  ngOnInit(): void {
    this.loadApiKeyInfo();
  }

  switchTab(tab: 'apiKey' | 'documentation'): void {
    this.activeTab = tab;
  }

  loadApiKeyInfo(): void {
    this.userDetailsService.getApiKeyInfo().subscribe({
      next: (info) => {
        this.apiKeyInfo = info;
      },
      error: (err) => {
        this.apiKeyInfo = null;
        // console.log('Failed to load API key info: ', err);
      }
    });
  }

  generateApiKey(): void {
    this.userDetailsService.generateApiKey().subscribe({
      next: (apiKey) => {
        this.snackBarService.success('API Key generated successfully.')
        apiKey = 'apiKey ' + apiKey;
        this.dialog.open(CopyApiKeyModalComponent, {
          data: apiKey,
          width: '400px'
        });
        this.loadApiKeyInfo();
      },
      error: (err) => {
        this.snackBarService.error('API Key generation failed. Try again later.')
        console.log('Failed to generate API key: ', err);
      }
    });
  }

  formatDate(dateString: string): string {
    if (dateString === '')
      return 'No date yet.'
    return formatDate(dateString, 'dd/MM/yyyy, HH:mm', 'en');
  }
}
