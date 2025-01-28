import { Component } from '@angular/core';
import {MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef} from "@angular/material/dialog";
import {MatIcon} from '@angular/material/icon';
import {DragDirective} from '../../../directives/drag.directive';
import {NgForOf, NgIf} from '@angular/common';
import {FileHandle} from '../../../models/file-handle';
import {MatButton} from '@angular/material/button';
import {ExcelToJsonConverter} from '../../../shared/utils/excel-to-json-converter/excel-to-json.util';
import {ProductBatchDto} from '../../../dtos/ProductBatchDto';
import {SnackbarService} from '../../../shared/utils/snackbar/snackbar.service';
import {ProductService} from '../../../services/product/product.service';

const ALLOWED_FILE_TYPES: string[] = [
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
  'application/vnd.ms-excel' // .xls
];

@Component({
  selector: 'app-batch-import-products-dialog',
  standalone: true,
  imports: [
    MatIcon,
    DragDirective,
    NgIf,
    MatButton,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent,
    NgForOf
  ],
  templateUrl: './batch-import-products-dialog.component.html',
  styleUrl: './batch-import-products-dialog.component.scss'
})
export class BatchImportProductsDialogComponent {
  fileName: string | null = null;
  file: File | null = null;
  errorMessage: string[] = []; //'Invalid file type. Please upload a valid Excel file (.xlsx, .xls).';
  validationErrors: string[] = [];
  parsedData: ProductBatchDto[] = [];

  constructor(private dialogRef: MatDialogRef<BatchImportProductsDialogComponent>,
              private snackbarService: SnackbarService,
              private productService: ProductService) {
  }

  onClose(): void {
    console.log("CLOSING");
    this.dialogRef.close();
  }

  downloadSampleExcel() {
    const sampleExcelUrl = '/CogniPriceSampleFile.xlsx';
    window.open(sampleExcelUrl, '_blank');
  }

  onFileDropped(fileHandle: FileHandle): void {
    this.handleFile(fileHandle.file);
  }

  onFileUpload(event: Event): void {
    console.log("File Upload");
    const input = event.target as HTMLInputElement;
    if (input?.files?.[0]) {
      this.handleFile(input.files[0]);
    }
  }

  removeFile(): void {
    this.file = null;
    this.fileName = null;
    this.errorMessage = [];
    this.validationErrors = [];
    console.log("File removed");
  }

  private async handleFile(file: File): Promise<void> {
    if (this.validateFileType(file)) {
      this.file = file;
      this.fileName = file.name;
      try {
        const {data} = await ExcelToJsonConverter.convert(file);
        console.log(data);
        this.parsedData = this.fromJsonArrayToProductBatchDtoList(data);
        this.validationErrors = this.validateData(this.parsedData);
        console.log(this.parsedData);
        console.log(this.validationErrors);
      } catch (error) {
        console.log(error);
        this.validationErrors = [error instanceof Error ? error.message : String(error)];
        // this.validationErrors = [error];
      }
    } else {
      this.snackbarService.error('Invalid file type. Only PNG and JPEG are allowed.');
      return;
    }
  }

  importFile(): void {
    console.log("Importing file");
    if (this.validationErrors.length > 0) {
      console.error('Fix validation errors before importing');
      return;
    }
    console.log('Valid data ready for import: ', this.parsedData);
    if (!this.parsedData.length) {
      console.log('No data to import');
      this.snackbarService.error('No data to import');
      return;
    }

    this.productService.createBatchOfProducts(this.parsedData).subscribe({
      next: (response) => {
        console.log("Response from server: ", response);
        this.snackbarService.success('Products imported successfully!');
        this.onClose();
      },
      error: (err) => {
        console.error('Error import products', err);
        console.log(err);
        this.snackbarService.error(err.error);
      }
    })
  }

  private validateData(data: ProductBatchDto[]): string[] {
    const errors: string[] = [];
    data.forEach((dto, index) => {
      const dtoErrors = dto.validate();
      if (dtoErrors.length) {
        errors.push(`Row ${index + 1}: ${dtoErrors.join(', ')}`);
      }
    });
    this.errorMessage = errors.slice(0, 3);
    return errors;
  }

  private fromJsonArrayToProductBatchDtoList(data: any[]): ProductBatchDto[] {
    return data.map((row) => {
      return new ProductBatchDto({
        name: row.Name,
        gtin: row.GTIN?.toString(), // Ensure GTIN is a string
        productCategory: row['Product Category'],
        productImageUrl: row['Product Image URL'],
        competitor: row['Competitor'],
        productUrl: row['Competitor Product URL'],
      });
    });
  }

  private validateFileType(file: File): boolean {
    const isValid = ALLOWED_FILE_TYPES.includes(file.type);
    console.log("Valid: ", isValid);
    return isValid;
  }

}
