import { Component } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {NgClass, NgIf} from '@angular/common';
import {MatFormField, MatHint, MatLabel} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {CompetitorService} from '../../services/competitor/competitor.service';
import {SnackbarService} from '../../shared/utils/snackbar/snackbar.service';

@Component({
  selector: 'app-create-competitor',
  templateUrl: './create-competitor.component.html',
  styleUrls: ['./create-competitor.component.scss'],
  imports: [
    NgIf,
    ReactiveFormsModule,
    MatHint,
    MatLabel,
    MatFormField,
    MatInput,
    NgClass
  ],
  standalone: true
})
export class CreateCompetitorComponent {
  competitorForm: FormGroup;

  constructor(private fb: FormBuilder,
              private competitorService: CompetitorService,
              private snackBarService: SnackbarService) {
    this.competitorForm = this.fb.group({
      name: [
        '',
        [Validators.required, Validators.minLength(1), Validators.maxLength(255)],
      ],
      hostname: [
        '',
        [
          Validators.required,
          Validators.pattern(
            '^(https?:\\/\\/)?([a-zA-Z0-9-]{1,63}\\.)+[a-zA-Z0-9-]{2,}$'
          ), // RFC 1123 hostname regex
          Validators.maxLength(25),
        ],
      ],
    });
  }

  onSubmit() {
    if (this.competitorForm.valid) {
      const competitorData = this.competitorForm.value;

      // Call the service to create the competitor
      this.competitorService.createCompetitor(competitorData).subscribe({
        next: () => {
          this.snackBarService.success('Competitor created successfully');
        },
        error: error => {
          // Error: Handle different error scenarios
          if (error.status === 400) {
            this.snackBarService.error('Invalid input. Please check your form data.');
          } else if (error.status === 409) {
            this.snackBarService.error('Competitor with the same hostname already exists.');
          } else {
            this.snackBarService.error('An unexpected error occurred. Please try again.');
          }
          console.error('Error creating competitor:', error);
        }
      });
    }
  }
}
