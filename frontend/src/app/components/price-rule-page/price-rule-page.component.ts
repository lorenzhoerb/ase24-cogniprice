import { Component } from '@angular/core';
import {FormsModule} from '@angular/forms';
import {MatButton} from '@angular/material/button';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow, MatRowDef, MatTable
} from '@angular/material/table';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {MatFormField, MatLabel, MatPrefix} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {MatPaginator} from '@angular/material/paginator';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-price-rule-page',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatCell,
    MatCellDef,
    MatChip,
    MatChipSet,
    MatColumnDef,
    MatFormField,
    MatHeaderCell,
    MatHeaderRow,
    MatHeaderRowDef,
    MatIcon,
    MatInput,
    MatLabel,
    MatPaginator,
    MatPrefix,
    MatRow,
    MatRowDef,
    MatTable,
    NgIf
  ],
  templateUrl: './price-rule-page.component.html',
  styleUrl: './price-rule-page.component.scss'
})
export class PriceRulePageComponent {

}
