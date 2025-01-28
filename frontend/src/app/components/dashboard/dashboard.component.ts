import {Component, OnInit} from '@angular/core';
import {StoreProductService} from '../../services/store-product/store-product.service';
import {MatPaginator} from '@angular/material/paginator';
import {MatOption, MatSelect} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field'; // Import MatFormFieldModule
import {MatInputModule} from '@angular/material/input'; // Import MatInputModule
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';

import {
  MatCard,
  MatCardActions,
  MatCardContent,
  MatCardHeader,
  MatCardImage,
  MatCardTitle,
} from '@angular/material/card';
import {CurrencyPipe, DatePipe, NgForOf, NgIf} from '@angular/common';
import {debounceTime, distinctUntilChanged, filter, Subject} from 'rxjs';
import {Router, RouterLink} from '@angular/router';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {ProductService} from '../../services/product/product.service';
import {ProductCategorie} from '../../models/product-categorie';
import {ProductDetailsWithPrices} from '../../models/product-details-with-prices';
import {FormsModule} from '@angular/forms';
import {SnackbarService} from '../../shared/utils/snackbar/snackbar.service';
import {WebhookService} from '../../services/webhook/webhook.service'; // Import MatButtonModule

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    MatPaginator,
    MatFormFieldModule, // Add MatFormFieldModule
    MatInputModule, // Add MatInputModule
    MatButtonModule, // Import MatButtonModule for buttons like 'Add Product'
    MatSelect,
    MatCardActions,
    MatCardContent,
    MatCard,
    MatOption,
    NgIf,
    NgForOf,
    MatCardImage,
    CurrencyPipe,
    DatePipe,
    MatCardHeader,
    MatCardTitle,
    MatIconModule,
    RouterLink,
    MatProgressSpinner,
    FormsModule,
  ],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  private searchSubject: Subject<string> = new Subject<string>();
  products: ProductDetailsWithPrices[] = [];
  pageSize: number = 10;
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;
  searchQuery: string = '';
  selectedCategory: string = '';
  categories: string[] = [];
  userHasProducts: boolean = true;
  isLoading = true;

  constructor(private storeProductService: StoreProductService,
              private productService: ProductService,
              private router: Router,
              private snackbarService: SnackbarService,
              private webhookService: WebhookService) {
  }

  ngOnInit(): void {

    const savedQuery = localStorage.getItem('searchQuery');
    const savedCategory = localStorage.getItem('selectedCategory');

    if (savedQuery) {
      this.searchQuery = savedQuery;
    }
    if (savedCategory) {
      this.selectedCategory = savedCategory;
    }

    this.loadProducts(this.currentPage, this.pageSize, this.searchQuery, this.selectedCategory);

    this.productService.getAllProductCategories().subscribe({
      next: (data) => {
        this.categories = data.map(category => category.category); // Extract category names
        this.categories.sort();
      },
      error: (err) => {
        console.error('Error fetching product categories:', err);
      }
    });

    this.searchSubject.pipe(
      debounceTime(300), // Wait 300ms after the user stops typing
      distinctUntilChanged(), // Only emit when the search query changes
    ).subscribe(query => {
      this.searchQuery = query.toLowerCase();
      localStorage.setItem('searchQuery', this.searchQuery);
      this.currentPage = 0;
      this.loadProducts(this.currentPage, this.pageSize, this.searchQuery, this.selectedCategory);
    });

    if (this.products.length == 0) {
      this.userHasProducts = false;
    }
  }

  loadProducts(page: number, size: number, name: string, category: string): void {
    this.storeProductService.getAllProductsOfUser(page, size, name, category).subscribe((data) => {
      this.products = data.content;
      this.totalPages = data.totalPages;
      this.totalElements = data.totalElements;
      this.currentPage = data.number;
      this.isLoading = false;
    });
  }
  hasPrices(product: ProductDetailsWithPrices) : boolean {
    return product.lowestPrice != null || product.highestPrice != null;
  }


  removeProductFromWatchlist(productId: number) {
    this.storeProductService.removeProductFromWatchList(productId).subscribe(
      () => {
        console.log("success")
        this.products = this.products.filter(product => product.productId !== productId);
      }
    )
  }


  onSearch(event: any): void {
    this.searchSubject.next(event.target.value);
  }

  onCategoryChange(category: string): void {
    this.selectedCategory = category;
    this.currentPage = 0;
    localStorage.setItem('selectedCategory', category);
    this.loadProducts(0, this.pageSize, this.searchQuery, this.selectedCategory);
  }

  goToPreviousPage(): void {
    if (this.currentPage > 0) {
      this.scrollToTop()
      this.loadProducts(this.currentPage - 1, this.pageSize, this.searchQuery, this.selectedCategory);

    }
  }

  goToNextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.scrollToTop()
      this.loadProducts(this.currentPage + 1, this.pageSize, this.searchQuery, this.selectedCategory);
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.scrollToTop()
      this.loadProducts(page, this.pageSize, this.searchQuery, this.selectedCategory);
    }
  }

  onPageInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const page = parseInt(input.value, 10) - 1; // Convert to zero-based index
    if (!isNaN(page)) {
      this.goToPage(page);
    }
  }

  scrollToTop() {
    window.scrollTo({
      top: 0, // Scroll to the top of the page
      behavior: 'smooth' // Smooth scrolling
    });
  }

  viewDetails(productId: number): void {
    console.log('viewDetails clicked: ' + productId);
    this.router.navigate(['/', productId, 'product', 'details']);
  }

  getImage(product: ProductDetailsWithPrices): string {
    if (product.productImageBase64) {
      return 'data:image/jpeg;base64,' + product.productImageBase64;
    } else if (product.productImageUrl) {
      return product.productImageUrl;
    } else {
      return '/not-found.png';
    }
  }

  addProduct() {
    console.log('add clicked!')
  }

  createComp() {
    console.log('add clicked!')

  }

  createProd() {
    console.log('add clicked!')

  }

  addToWatchlist() {
    console.log('add clicked!')

  }


  addWebhook(productId: number) {
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
}
