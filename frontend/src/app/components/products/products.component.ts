import {Component, OnInit} from '@angular/core';
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
import {CurrencyPipe, DatePipe, NgForOf, NgIf, SlicePipe} from '@angular/common';
import {debounceTime, distinctUntilChanged, Subject} from 'rxjs';
import {ProductService} from '../../services/product/product.service';
import {Product} from '../../models/product';
import {MatProgressSpinner} from "@angular/material/progress-spinner";
import {ActivatedRoute, Router} from '@angular/router';
import {FormsModule} from '@angular/forms'; // Import MatButtonModule

@Component({
  selector: 'app-products',
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
    MatProgressSpinner,
    FormsModule,
    SlicePipe,
  ],
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss']
})
export class ProductsComponent implements OnInit {
  private searchSubject: Subject<string> = new Subject<string>();
  products: Product[] = [];
  pageSize: number = 16;
  currentPage: number = 0;
  totalPages: number = 0;
  totalElements: number = 0;
  searchQuery: string = '';
  selectedCategory: string = '';
  categories: string[] = [];
  isLoading: boolean = true;

  // Needed if coming from competitor list page
  competitorId: number | null = null;

  constructor(private productService: ProductService,
              private router: Router,
              private route: ActivatedRoute) {
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

    this.route.queryParams.subscribe((params) => {
      this.competitorId = params['competitorId'] ? params['competitorId'] : null;
      this.loadProducts(this.currentPage, this.pageSize, this.searchQuery, this.selectedCategory);
    })

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

  }

  loadProducts(page: number, size: number, name: string, category: string): void {
    if (this.competitorId) {
      console.log("Should have id: " + this.competitorId);
      this.productService.getFilteredProductsByCompetitorId(this.competitorId, page, size, name, category).subscribe((data) => {
        console.log(data)
        this.products = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.currentPage = data.number;
        this.isLoading = false;
      })
    } else {
      this.productService.getFilteredProducts(page, size, name, category).subscribe((
        data) => {
        console.log(data)
        this.products = data.content;
        this.totalPages = data.totalPages;
        this.totalElements = data.totalElements;
        this.currentPage = data.number;
        this.isLoading = false;
      });
    }
  }

  onSearch(event: any): void {
    this.searchSubject.next(event.target.value);
  }

  onCategoryChange(category: string): void {
    this.selectedCategory = category;
    this.currentPage = 0;
    localStorage.setItem('selectedCategory', category);
    this.loadProducts(this.currentPage, this.pageSize, this.searchQuery, this.selectedCategory);
  }

  goToPreviousPage(): void {
    if (this.currentPage > 0) {
      this.scrollToTop();
      this.loadProducts(this.currentPage - 1, this.pageSize, this.searchQuery, this.selectedCategory);
    }
  }

  goToNextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.scrollToTop();
      this.loadProducts(this.currentPage + 1, this.pageSize, this.searchQuery, this.selectedCategory);
    }
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.scrollToTop();
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

  getImage(product: Product): string {
    if (product.imageBase64) {
      return 'data:image/jpeg;base64,' + product.imageBase64;
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
}
