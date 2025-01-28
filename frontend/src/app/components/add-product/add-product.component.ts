import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {MatFormField, MatFormFieldModule} from '@angular/material/form-field';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule, ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import {MatOption, MatSelect, MatSelectModule} from '@angular/material/select';
import {CommonModule, NgClass, NgForOf} from '@angular/common';
import {StoreProductService} from '../../services/store-product/store-product.service';
import {MatInputModule} from '@angular/material/input';
import {DragDirective} from '../../directives/drag.directive';
import {FileHandle} from '../../models/file-handle';
import {CompetitorService} from '../../services/competitor/competitor.service';
import {Product} from '../../models/product';
import {debounceTime, of, switchMap} from 'rxjs';
import {ProductService} from '../../services/product/product.service';
import {MatAutocomplete, MatAutocompleteTrigger} from '@angular/material/autocomplete';
import {ProductCreateDto} from '../../dtos/productCreateDto';
import {StoreProductCreateDto} from '../../dtos/StoreProductCreateDto';
import {CompetitorDetails} from '../../models/competitor-details';
import {SnackbarService} from '../../shared/utils/snackbar/snackbar.service';
import {WebhookService} from '../../services/webhook/webhook.service';
import {Router} from '@angular/router';
import {MatDialog} from "@angular/material/dialog";
import {
  BatchImportProductsDialogComponent
} from './batch-import-products-dialog/batch-import-products-dialog.component';


export const noneValue = '--None--';
const allowedImageTypes = ['image/png', 'image/jpeg'];


@Component({
  selector: 'app-add-product',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatFormField,
    FormsModule,
    MatOption,
    MatSelect,
    NgClass,
    NgForOf,
    DragDirective,
    MatAutocompleteTrigger,
    MatAutocomplete,
    DragDirective,
    ReactiveFormsModule
  ],
  templateUrl: './add-product.component.html',
  styleUrl: './add-product.component.scss'
})
export class AddProductComponent implements OnInit{
  product = {
    name: '',
    gtin: '',
    image: '',
    pricingRule: '',
    category: '',
    competitor: '',
    url: ''
  };

  addProductForm: FormGroup;

  pricingRules = ['Rule 1', 'Rule 2', 'Rule 3'];
  categories: string[] = [];
  competitors: CompetitorDetails[] = [];
  filteredProducts: Product[] = []

  webhookActive = false; // Initially inactive

  imagePreviewUrl: string | null = null;

  productSelected: boolean = false;



  constructor(private productService: ProductService,
              private competitorService: CompetitorService,
              private fb: FormBuilder,
              private cdr: ChangeDetectorRef,
              private snackbarService: SnackbarService,
              private storeProductService: StoreProductService,
              private webhookService: WebhookService,
              private router: Router,
              private dialog: MatDialog) {
    this.addProductForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      gtin: ['', [Validators.required,
        Validators.minLength(8),
        Validators.maxLength(13),
        Validators.pattern(/^\d+$/)
          ]],
      category: [null, Validators.required],
      image: [null, [fileTypeValidator(allowedImageTypes)]],
      url: ['', [Validators.maxLength(255)
      ]],
      competitor: [null],
      pricingRule: [null]
    },
      { validators: competitorUrlValidator } // Attach the custom validator
    );

    let product = (this.router.getCurrentNavigation()?.extras.state as { product: any })?.product;
    if(product) {
      this.setProduct(product);
    }
  }

  getErrorMessage(controlName: string): string | null {
    const control = this.addProductForm.get(controlName);
    if (control?.hasError('required')) {
      return `${controlName.charAt(0).toUpperCase() + controlName.slice(1)} is required.`;
    }
    if (control?.hasError('maxlength')) {
      const maxLength = control?.getError('maxlength')?.requiredLength;
      return `${controlName.charAt(0).toUpperCase() + controlName.slice(1)} cannot exceed ${maxLength} characters.`;
    }
    return null;
  }

  ngOnInit(): void {
    this.productService.getAllProductCategories().subscribe({
      next: (data) => {
        this.categories = data.map(category => category.category); // Extract category names
        this.categories.sort();
      },
      error: (err) => {
        console.error('Error fetching product categories:', err);
      }
    });

    this.competitorService.getAllCompetitors().subscribe({
      next: (data: CompetitorDetails[]) => {
        this.competitors = [
          { id: -1, name: noneValue, hostname: "" }, // Prepend the `noneValue`
          ...data // Spread the actual competitors
        ];
      },
      error: (err) => {
        console.error('Error fetching competitors:', err);
      }
    });

    this.addProductForm.get('name')?.valueChanges
      .pipe(
        debounceTime(300),  // Delay the request until typing stops
        switchMap((value: string) => {
          if (value && value.length >= 3) {
            // Trigger search if the input has at least 3 characters
            return this.productService.getAutocompleteProducts(value);  // Assuming you pass the value as a query
          }
          return of([]);
        })
      )
      .subscribe((results) => {
        this.filteredProducts = results;  // Update the filtered products
      });

    // Listen to changes in the competitor field
    this.addProductForm.get('competitor')?.valueChanges.subscribe((competitorId) => {
      const selectedCompetitor = this.competitors.find(c => c.id === competitorId);
      if (selectedCompetitor && selectedCompetitor.hostname) {
        // Update the URL field with the competitor's hostname
        this.addProductForm.patchValue({
          url: selectedCompetitor.hostname
        });
      } else {
        // Clear the URL field if no valid competitor is selected
        this.addProductForm.patchValue({
          url: ''
        });
      }
    });
  }

  onProductSelected(event: any): void {
    const selectedProduct = this.filteredProducts.find(product => product.name === event.option.value);
    console.log(selectedProduct?.imageBase64)

    const imageLoaded = selectedProduct?.imageBase64 ? 'data:image/png;base64,' + selectedProduct?.imageBase64 : null;
    if (selectedProduct) {
      this.addProductForm.patchValue({
        name: selectedProduct.name,
        gtin: selectedProduct.gtin,
        category: selectedProduct.category,
        image: null
      });
    }
    // Disable the relevant fields

    this.addProductForm.get('name')?.disable();
    this.addProductForm.get('gtin')?.disable();
    this.addProductForm.get('category')?.disable();
    this.addProductForm.get('image')?.disable();


    // Set the preview URL for the image
    this.imagePreviewUrl = imageLoaded;

    // Mark product as selected
    this.productSelected = true;
  }

  setProduct(product: any) {
    console.log(product);
    if (product) {
      this.addProductForm.patchValue({
        name: product.productName,
        gtin: product.gtin,
        category: product.category,
        image: null
      });
    }
    // Disable the relevant fields

    this.addProductForm.get('name')?.disable();
    this.addProductForm.get('gtin')?.disable();
    this.addProductForm.get('category')?.disable();
    this.addProductForm.get('image')?.disable();

    const imageLoaded = product?.productImageBase64 ? 'data:image/png;base64,' + product?.productImageBase64 : null;

    // Set the preview URL for the image
    this.imagePreviewUrl = imageLoaded;

    // Mark product as selected
    this.productSelected = true;
  }

  resetPreselectedFields(): void {
    // Enable the relevant fields
    this.addProductForm.get('name')?.enable();
    this.addProductForm.get('gtin')?.enable();
    this.addProductForm.get('category')?.enable();
    this.addProductForm.get('image')?.enable();

    // Reset the form fields
    this.addProductForm.patchValue({
      name: '',
      gtin: '',
      category: null,
      image: null,
    });

    // Clear the image preview and productSelected flag
    this.imagePreviewUrl = null;
    this.productSelected = false;
  }

  onImageUpload(event: any): void {
    const file = event.target.files[0];

    if (file) {
      const control = this.addProductForm.get('image');

      // Temporarily set the file to trigger validation
      control?.setValue(file);

      // Check if the file type is invalid
      if (control?.hasError('invalidFileType')) {
        this.snackbarService.error('Invalid file type. Only PNG and JPEG are allowed.');
        control?.setValue(null); // Reset the field
        this.imagePreviewUrl = null; // Clear the preview
        return;
      }

      // If valid, update the preview URL
      this.imagePreviewUrl = URL.createObjectURL(file);
      this.cdr.detectChanges();

      console.log('Valid file uploaded:', file); // Debugging log
    }
  }


  onSubmit(): void {
    console.log('Form values before submission:', this.addProductForm.value);

    this.addProductForm.get('name')?.enable();
    this.addProductForm.get('gtin')?.enable();
    this.addProductForm.get('category')?.enable();
    this.addProductForm.get('image')?.enable();

    console.log("formData" +this.addProductForm.value.toString())
    console.log(this.addProductForm.valid)

    if (this.addProductForm.valid) {
      const formData = this.addProductForm.value;

      const productDto = new ProductCreateDto(
        formData.name,
        formData.gtin,
        formData.category
      );

      let storeProductDto: StoreProductCreateDto | null = null;
      if (formData.competitor && formData.url) {
        storeProductDto = new StoreProductCreateDto(
          0, // Placeholder for productId
          formData.competitor,
          formData.url
        );
      }

      const image: File | null = formData.image || null;

      // Sequential execution
      this.productService.createProduct(productDto, image).subscribe({
        next: ({ productId, status }) => {
          if (status === 200) {
            this.snackbarService.success('Product already exists.');
          } else if (status === 201) {
            this.snackbarService.success('Product created successfully.');
          }

          if (storeProductDto) {
            storeProductDto.productId = productId;

            // Step 2: Create Store Product
            this.storeProductService.createStoreProduct(storeProductDto).subscribe({
              next: () => {
                this.snackbarService.success('Store Product linked successfully.');


              },
              error: (err) => {
                console.log(err)
                this.snackbarService.error(err.error);
              }
            });

          }

          if (this.webhookActive) {
            // Step 3: Add to Webhook
            this.webhookService.addProductToWebhook(productId).subscribe({
              next: () => {
                this.snackbarService.success('Product added to webhook successfully.');

              },
              error: (err) => {
                console.log(err)
                this.snackbarService.error(err.error);
              }
            });
          }

          //TODO pricing rule

          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.snackbarService.error(err.error);
        }
      });
    } else {
      console.log('Form is invalid:', this.addProductForm.errors);
      if(this.productSelected){
        this.addProductForm.get('name')?.disable();
        this.addProductForm.get('gtin')?.disable();
        this.addProductForm.get('category')?.disable();
        this.addProductForm.get('image')?.disable();
      }
      this.addProductForm.markAllAsTouched();
      this.cdr.detectChanges(); // Force change detection to update UI
    }
  }


  toggleWebhookStatus(): void {
    this.webhookActive = !this.webhookActive; // Toggle the button state
    console.log('Webhook status:', this.webhookActive ? 'Active' : 'Inactive');
    if (this.webhookActive) {
      // Call your webhook creation logic here
      console.log('Webhook created for product:', this.product);
    }
  }

  fileDropped(fileHandle: FileHandle): void {
    const file = fileHandle.file;

    if (file) {
      const control = this.addProductForm.get('image');

      // Temporarily set the file to trigger validation
      control?.setValue(file);

      // Check if the file type is invalid
      if (control?.hasError('invalidFileType')) {
        this.snackbarService.error('Invalid file type. Only PNG and JPEG are allowed.');
        control?.setValue(null); // Reset the field
        this.imagePreviewUrl = null; // Clear the preview
        return;
      }

      // If valid, update the preview URL
      this.imagePreviewUrl = URL.createObjectURL(file);
      this.cdr.detectChanges();

      console.log('Valid file uploaded:', file); // Debugging log
    }

  }


  removeImage(): void {
    this.addProductForm.patchValue({
      image: null, // Clear the form control value
    });
    if (this.imagePreviewUrl != null) {
      URL.revokeObjectURL(this.imagePreviewUrl);
      this.imagePreviewUrl = null;
    }

    console.log('Image removed:', this.addProductForm.get('image')?.value); // Debugging log
  }

  openBatchImportDialogue(): void {
    const dialogueRef = this.dialog.open(BatchImportProductsDialogComponent, {
      panelClass: 'batch-dialog-panel'
    });

    // dialogueRef.afterClosed().subscribe()
  }
}

export const competitorUrlValidator: ValidatorFn = (formGroup: AbstractControl): ValidationErrors | null => {
  const urlControl = formGroup.get('url');
  const competitorControl = formGroup.get('competitor');

  const url = urlControl?.value;
  const competitor = competitorControl?.value;

  const currentErrors = urlControl?.errors || {};

  console.log('Validator triggered:', { url, competitor });

  if ((url && (!competitor || competitor == -1)) || (competitor && !url && competitor != -1)) {
    console.log('competitorUrlError triggered'); // Debug
    formGroup.get('competitor')?.setErrors({ competitorUrlError: true });
    formGroup.get('url')?.setErrors({ competitorUrlError: true });
    return { competitorUrlError: true }; // Error if one field is filled but not the other
  }else{
    //formGroup.get('competitor')?.setErrors(null);
    //formGroup.get('url')?.setErrors(null);
    //console.log("errors" + currentErrors);
    const { competitorUrlError, ...remainingErrors } = currentErrors;
    urlControl?.setErrors(Object.keys(remainingErrors).length ? remainingErrors : null);
    competitorControl?.setErrors(null);
  }

  return null; // No error if both are empty or both are filled
};

function fileTypeValidator(allowedTypes: string[]): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const file = control.value as File;
    if (file && !allowedTypes.includes(file.type)) {
      return { invalidFileType: true };
    }
    return null;
  };
}
