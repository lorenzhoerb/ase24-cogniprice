import {Component} from '@angular/core';
import {MatCardModule} from '@angular/material/card';
import {MatFormField, MatFormFieldModule, MatLabel} from '@angular/material/form-field';
import {MatCheckbox} from '@angular/material/checkbox';
import {MatTableDataSource, MatTableModule} from '@angular/material/table';
import {MatPaginator} from '@angular/material/paginator';
import {MatInputModule} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {MatChip, MatChipSet} from '@angular/material/chips';
import {CommonModule} from '@angular/common';
import {BehaviorSubject, combineLatest, debounceTime, distinctUntilChanged, map, startWith, switchMap} from 'rxjs';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {MatButtonModule} from '@angular/material/button';
import {PricingRuleService} from '../../services/pricing-rule.service';
import {SimplePricingRule} from '../../models/price-rule';
import {PaginatedResponse} from '../../models/paginated-response';
import {toPricingRuleSummary} from '../../utils/pricing-rule-utils';
import {Router} from '@angular/router';

@Component({
  selector: 'app-dynamic-pricing-page',
  standalone: true,
  imports: [
    MatCardModule,
    MatFormField,
    MatCheckbox,
    MatLabel,
    MatTableModule,
    MatFormFieldModule,
    MatPaginator,
    MatInputModule,
    MatIcon,
    MatChipSet,
    MatChip,
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule
  ],
  templateUrl: './dynamic-pricing-page.component.html',
  styleUrl: './dynamic-pricing-page.component.scss'
})
export class DynamicPricingPageComponent {
  // Pagination
  pageLength: number = 0
  currentPage: number = 0;
  pageSize: number = 10

  displayedColumns: string[] = ['name', 'summary', 'status'];

  // Reactive form controls and filters
  searchControl = new FormControl('');
  statusFilter$ = new BehaviorSubject<string | null>(null);
  paginationState$ = new BehaviorSubject<{ page: number, size: number }>({page: 0, size: 10});
  dataSource = new MatTableDataSource<SimplePricingRule>();


  constructor(private pricingRuleService: PricingRuleService, private router: Router) {
  }

  ngOnInit(): void {
    // Combine search and status filters into a single stream
    combineLatest([
      this.searchControl.valueChanges.pipe(
        startWith(''),
        debounceTime(300),
        distinctUntilChanged()
      ),
      this.statusFilter$,
      this.paginationState$
    ])
      .pipe(
        switchMap(([searchQuery, statusFilter, {page, size}]) =>
          this.pricingRuleService.getAllPricingRules(
            page,
            size,
            statusFilter === null ? undefined : statusFilter === 'ACTIVE',
            searchQuery as string
          )
        ),
        map((response: PaginatedResponse<SimplePricingRule>) => {
          console.log(response)
          this.pageLength = response.totalElements;
          this.currentPage = response.number;
          return response.content;
        })
      )
      .subscribe((data) => {
        this.dataSource.data = data;
      });
  }

  /**
   * Toggles the status filter.
   * If the selected filter is already active, it resets the filter.
   */
  toggleFilter(status: string): void {
    const currentFilter = this.statusFilter$.value;
    this.statusFilter$.next(currentFilter === status ? null : status);
  }

  /**
   * Handles pagination events from the paginator.
   */
  handlePageEvent(event: { pageIndex: number; pageSize: number }): void {
    this.paginationState$.next({page: event.pageIndex, size: event.pageSize});
  }

  addPricingRule() {
    this.router.navigate(['/dynamic-pricing/rules/create']);
  }

  navigateToRule(id: string): void {
    this.router.navigate([`/dynamic-pricing/rules/${id}`]);
  }

  protected readonly toPricingRuleSummary = toPricingRuleSummary;
}
