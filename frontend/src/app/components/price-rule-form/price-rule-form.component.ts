import {Component, ViewChild} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {PricingRuleService} from '../../services/pricing-rule.service';
import {
  LimitType,
  MatchType,
  Position,
  PriceLimit,
  PricingRuleDetails,
  PricingRuleRequest,
  Scope
} from '../../models/price-rule';
import {MatButton, MatButtonModule} from '@angular/material/button';
import {
  MatCell,
  MatCellDef,
  MatColumnDef,
  MatHeaderCell,
  MatHeaderRow,
  MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable
} from '@angular/material/table';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {MatFormField, MatFormFieldModule, MatLabel, MatPrefix} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInput, MatInputModule} from '@angular/material/input';
import {MatPaginator} from '@angular/material/paginator';
import {NgForOf, NgIf} from '@angular/common';
import {MatRadioButton, MatRadioGroup} from '@angular/material/radio';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatDivider} from '@angular/material/divider';
import {MatCard, MatCardContent} from '@angular/material/card';
import {MatCheckbox} from '@angular/material/checkbox';
import {SearchSelectorComponent} from '../search-selector/search-selector.component';
import {
  CategorySearchSelectorComponent
} from '../search-selectors/category-search-selector/category-search-selector.component';
import {
  ProductSearchSelectorComponent
} from '../search-selectors/product-search-selector/product-search-selector.component';
import {MatListOption, MatSelectionList} from '@angular/material/list';
import {ProductService} from '../../services/product/product.service';
import {MatDialog} from '@angular/material/dialog';
import {ConfirmationDialogComponent} from '../confirmation-dialog/confirmation-dialog.component';
import {
  StoreProductSelectorComponent
} from '../search-selectors/store-product-selector/store-product-selector.component';
import {ProductDetailsWithPrices} from '../../models/product-details-with-prices';
import {SnackbarService} from '../../shared/utils/snackbar/snackbar.service';


@Component({
  selector: 'app-price-rule-form',
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
    NgForOf,
    NgIf,
    MatRadioGroup,
    MatRadioButton,
    MatSelect,
    MatOption,
    MatDivider,
    MatCardContent,
    MatCard,
    MatCheckbox,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    SearchSelectorComponent,
    CategorySearchSelectorComponent,
    ProductSearchSelectorComponent,
    MatSelectionList,
    MatListOption,
    StoreProductSelectorComponent
  ],
  templateUrl: './price-rule-form.component.html',
  styleUrl: './price-rule-form.component.scss'
})
export class PriceRuleFormComponent {
  priceRuleForm: FormGroup
  isEditMode: boolean = false;
  priceRuleId: string | null = null;
  scopeEntities: any[] = [];

  @ViewChild('scopeEntitiesList') scopeEntitiesList!: MatSelectionList;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    public router: Router,
    private pricingRuleService: PricingRuleService,
    private productService: ProductService,
    private dialog: MatDialog,
    private snackBarService: SnackbarService
  ) {
    this.priceRuleForm = this.setupFormGroup()
  }

  ngOnInit(): void {
    this.checkEditMode();
    this.setupPositionTypeWatcher();
    this.setupLimitWatcher();

    // Apply the generalized function to relevant fields
    this.ensurePositiveValue('position.custom.value');
    this.ensurePositiveValue('limits.minLimit');
    this.ensurePositiveValue('limits.maxLimit');
  }

  private setupPositionTypeWatcher(): void {
    const positionTypeControl = this.priceRuleForm.get('position.type');
    const customControls = this.priceRuleForm.get('position.custom') as FormGroup;
    const exactControls = this.priceRuleForm.get('position.exact') as FormGroup;

    positionTypeControl?.valueChanges.subscribe((value: string) => {
      if (value === 'CUSTOM') {
        customControls.enable();
        exactControls.disable();
      } else if (value === 'EXACT') {
        exactControls.enable();
        customControls.disable();
      }
    });

    // Initialize the state based on the current value
    const initialType = positionTypeControl?.value;
    if (initialType === 'CUSTOM') {
      customControls.enable();
      exactControls.disable();
    } else if (initialType === 'EXACT') {
      exactControls.enable();
      customControls.disable();
    }
  }

  private setupLimitWatcher(): void {
    // Get form controls for hasMinLimit, minLimit, hasMaxLimit, and maxLimit
    const hasMinLimitControl = this.priceRuleForm.get('limits.hasMinLimit');
    const minLimitControl = this.priceRuleForm.get('limits.minLimit') as FormControl;
    const hasMaxLimitControl = this.priceRuleForm.get('limits.hasMaxLimit');
    const maxLimitControl = this.priceRuleForm.get('limits.maxLimit') as FormControl;

    // Initialize the minLimit and maxLimit controls based on the current values of hasMinLimit and hasMaxLimit
    this.toggleLimitControl(hasMinLimitControl?.value, minLimitControl);
    this.toggleLimitControl(hasMaxLimitControl?.value, maxLimitControl);

    // Subscribe to changes in hasMinLimit and hasMaxLimit controls
    hasMinLimitControl?.valueChanges.subscribe((hasMinLimit: boolean) => {
      this.toggleLimitControl(hasMinLimit, minLimitControl);
    });

    hasMaxLimitControl?.valueChanges.subscribe((hasMaxLimit: boolean) => {
      this.toggleLimitControl(hasMaxLimit, maxLimitControl);
    });
  }

  private toggleLimitControl(enable: boolean, limitControl: FormControl): void {
    if (enable) {
      limitControl.enable();
    } else {
      limitControl.disable();
    }
  }

  private checkEditMode(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        // We're in edit mode, load the price rule
        this.isEditMode = true;
        this.priceRuleId = id;
        this.fetchPriceRule(id);
      } else {
        // We're in create mode
        this.isEditMode = false;
        this.priceRuleId = null;
      }
    });
  }

  private setupFormGroup(): FormGroup {
    return this.fb.group({
      id: [null],
      name: ['', [Validators.required, Validators.minLength(3)]],
      position: this.fb.group({
        type: ['', Validators.required],
        exact: this.fb.group({
          reference: ['', Validators.required],
        }),
        custom: this.fb.group({
          matchType: ['', Validators.required],
          reference: ['', Validators.required],
          value: [null, Validators.required],
          unit: ['', Validators.required]
        })
      }),
      scope: ['', Validators.required],
      appliedCategories: this.fb.array([]),
      appliedProductIds: this.fb.array([]),
      limits: this.fb.group({
        hasMaxLimit: [false],
        maxLimit: ['', Validators.required],
        hasMinLimit: [false],
        minLimit: ['', Validators.required],
      }),
      isActive: ['ACTIVE', Validators.required],
    });
  }

  private fetchPriceRule(id: string): void {
    this.pricingRuleService.getPricingRuleById(id).subscribe({
      next: (pricingRule: PricingRuleDetails) => this.updateForm(pricingRule),
      error: (error) => {
        console.error('Error fetching pricing rule:', error);
        if (error.status === 404) {
          this.snackBarService.error('Pricing rule not found')
        } else if (error.status === 403) {
          this.snackBarService.error('You are not allowed to edit this price rule')
        } else {
          this.snackBarService.error('Some error occurred')
        }
        this.router.navigate(['/dynamic-pricing'])
      }
    });
  }

  updateForm(pricingRule: PricingRuleDetails): void {
    let isExactPosition = pricingRule.position.matchType === 'EQUALS';

    this.priceRuleForm.patchValue({
      id: pricingRule.id,
      name: pricingRule.name,
      position: {
        type: isExactPosition ? 'EXACT' : 'CUSTOM',
        exact: {
          reference: isExactPosition ? pricingRule.position.reference : '',
        },
        custom: {
          matchType: !isExactPosition ? pricingRule.position.matchType : '',
          reference: !isExactPosition ? pricingRule.position.reference : '',
          value: !isExactPosition ? pricingRule.position.value : '',
          unit: !isExactPosition ? pricingRule.position.unit : ''
        }
      },
      scope: pricingRule.scope,
      appliedCategories: pricingRule.appliedCategories,
      appliedProductIds: pricingRule.appliedProductIds,
      limits: {
        hasMinLimit: pricingRule.minLimit !== null,
        minLimit: pricingRule.minLimit?.limitValue || null,
        hasMaxLimit: pricingRule.maxLimit !== null,
        maxLimit: pricingRule.maxLimit?.limitValue || null,
      },
      isActive: pricingRule.active ? 'ACTIVE' : 'DISABLED',
    });

    this.fetchScopeEntities(pricingRule.scope, pricingRule.appliedCategories, pricingRule.appliedProductIds);
  }

  onSubmit(): void {
    // form must be valid
    if (!this.priceRuleForm.valid) {
      this.priceRuleForm.markAllAsTouched();
      return;
    }

    // At least one scope entity must be selected
    if (this.scopeEntities.length <= 0) {
      this.snackBarService.error("At least one scope entity must be added")
      return;
    }

    // update entity if in edit mode
    if (this.isEditMode && this.priceRuleId) {
      this.pricingRuleService.updatePricingRule(this.priceRuleId, this.getPricingRuleRequest()).subscribe({
        next: pricingRuleData => {
          this.updateForm(pricingRuleData)
          this.snackBarService.success("Successfully updated pricing rule")
        },
        error: err => {
          this.snackBarService.error(`Error while updating pricing rule: ${err.error}`)
          console.error(err)
        }
      });
    } else {
      this.pricingRuleService.createPricingRule(this.getPricingRuleRequest()).subscribe({
        next: ({id}) => {
          this.router.navigate([`dynamic-pricing/rules/${id}`]);
          this.snackBarService.success("Saved Pricing Rule");
        },
        error: (error) => {
          this.snackBarService.error(`Error in saving Pricing Rule: ${error.error}`)
        }
      })
    }
  }

  private ensurePositiveValue(controlPath: string): void {
    const control = this.priceRuleForm.get(controlPath);

    if (!control) {
      console.warn(`Control '${controlPath}' does not exist in the form.`);
      return;
    }

    control.valueChanges.subscribe((value) => {
      if (value !== null && value < 0) {
        control.setValue(Math.abs(value), {emitEvent: false});
      }
    });
  }


  onScopeEntitySelected(entity: any) {
    this.scopeEntities.push(entity);
  }

  removeScopeEntities(selected: MatListOption[]) {
    const selectedEntities = selected.map((option) => option.value);
    this.scopeEntities = this.scopeEntities.filter(
      (entity) => !selected.some((option) => option.value === entity)
    );
  }

  clearScopeEntities(): void {
    this.scopeEntities = [];
  }

  onDiscard(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Discard',
        message: 'Are you sure you want to discard all changes?',
      },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'confirm') {
        this.router.navigate(['/dynamic-pricing'], {queryParams: {}});
        this.snackBarService.success('Discarded changes')
      }
    })
  }

  onDelete(): void {
    const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
      data: {
        title: 'Confirm Delete',
        message: 'Are you sure you want to delete this pricing rule?',
      },
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result === 'confirm' && this.priceRuleId) {
        this.pricingRuleService.deletePriceRuleById(this.priceRuleId)
          .subscribe({
            next: value => {
              this.router.navigate(['/dynamic-pricing'], {queryParams: {}});
              this.snackBarService.success('Deleted Pricing Rule successfully.')
            },
            error: err => {
              console.error(err)
              this.snackBarService.error("Error deleting pricing rule")
            }
          })
      }
    });
  }

  private fetchScopeEntities(scope: String, appliedCategories: string[], appliedProductIds: number[]) {
    if (scope === 'CATEGORY' && appliedCategories.length > 0) {
      // Fetch categories
      this.productService.getCategoriesByIds(appliedCategories).subscribe({
        next: (categories) => {
          this.scopeEntities = categories;
        },
        error: (error: any) => {
          console.error('Error fetching categories:', error);
        }
      });
    } else if (scope === 'PRODUCT' && appliedProductIds.length > 0) {
      // Fetch products
      this.productService.getProductsByIds(appliedProductIds).subscribe({
        next: (products: ProductDetailsWithPrices[]) => {
          this.scopeEntities = products;
        },
        error: (error: any) => {
          console.error('Error fetching products:', error);
        }
      });
    }

  }

  private getPricingRuleRequest(): PricingRuleRequest {
    const pricingRuleData = this.priceRuleForm.value;

    // Get scope ids depending on selected scope
    const appliedToIds = pricingRuleData.scope === Scope.PRODUCT ?
      this.scopeEntities.map(product => product.productId)
      : this.scopeEntities.map(category => category.category);

    let position: Position;

    // assign position
    if (pricingRuleData.position.type === 'EXACT') {
      position = {
        matchType: MatchType.EQUALS,
        reference: pricingRuleData.position.exact.reference,
      }
    } else {
      position = {
        matchType: pricingRuleData.position.custom.matchType,
        reference: pricingRuleData.position.custom.reference,
        unit: pricingRuleData.position.custom.unit,
        value: pricingRuleData.position.custom.value
      }
    }

    return {
      name: pricingRuleData.name,
      position: position,
      scope: pricingRuleData.scope,
      appliedToIds: appliedToIds,
      isActive: pricingRuleData.isActive === 'ACTIVE',
      minLimit: pricingRuleData.limits.hasMinLimit ?
        {limitType: LimitType.FIXED_AMOUNT, limitValue: pricingRuleData.limits.minLimit} as PriceLimit
        : null,
      maxLimit: pricingRuleData.limits.hasMaxLimit ?
        {limitType: LimitType.FIXED_AMOUNT, limitValue: pricingRuleData.limits.maxLimit} as PriceLimit
        : null,
    }
  }
}
