import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {catchError, from, map, mergeMap, Observable, of, toArray} from 'rxjs';

import {Globals} from '../../global/globals';
import {ProductCategorie} from '../../models/product-categorie';
import {Product} from '../../models/product';
import {ProductCreateDto} from '../../dtos/productCreateDto';
import {StoreProductCreateDto} from '../../dtos/StoreProductCreateDto';
import {PaginatedResponse} from '../../models/paginated-response';
import {ProductDetailsWithPrices} from '../../models/product-details-with-prices';
import {tap} from 'rxjs/operators';
import {ProductBatchDto} from '../../dtos/ProductBatchDto';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private baseUri: string;

  constructor(private http: HttpClient, private globals: Globals) {
    this.baseUri = this.globals.backendUri + 'products'
  }

  getAllProductsOfUser(): Observable<Product[]> {
    return this.http.get<Product[]>(this.baseUri + '/view');
  }

  getAllProductCategories(): Observable<ProductCategorie[]> {
    const cat = this.getCategories();
    if (cat != null) {
      return of(cat);
    }
    return this.http.get<ProductCategorie[]>(this.globals.backendUri + 'product-categories').pipe(
      tap(response => this.setCategories(response))
    );
  }

  getAutocompleteProducts(query: string): Observable<Product[]> {
    const params = new HttpParams().set('q', query);  // Add query parameter

    return this.http.get<Product[]>(this.baseUri + '/autocomplete', {params})
  }

  addProduct(productData: any): Observable<any> {
    return this.http.post<any>(this.baseUri, productData);
  }

  getProductDetailsByProductId(productId: number): Observable<ProductDetailsWithPrices> {
    return this.http.get<ProductDetailsWithPrices>(this.baseUri + '/details/' + productId);
  }


  createProduct(productCreateDto: ProductCreateDto, image: File | null): Observable<{
    productId: number;
    status: number
  }> {
    const formData = new FormData();

    const plainProductCreateDto = {
      name: productCreateDto.name,
      gtin: productCreateDto.gtin,
      productCategoryId: productCreateDto.productCategoryId,
    };

    formData.append('productCreateDto', new Blob([JSON.stringify(plainProductCreateDto)], {type: 'application/json'}));
    if (image) {
      formData.append('image', image);
    }
    return this.http.post(`${this.baseUri}/create`, formData, {observe: 'response', responseType: 'text'}).pipe(
      map((response) => {
        const productId = parseInt(response.body || '', 10);
        return {productId, status: response.status}; // Return productId and status
      })
    );
  }

  createBatchOfProducts(producs: ProductBatchDto[]): Observable<any> {
    return this.http.post(`${this.baseUri}/batch-create`, producs, { responseType: 'text' });
  }

  getFilteredProducts(page: number, size: number, query: string, category: string): Observable<PaginatedResponse<Product>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('query', query)
      .set('category', category);
    return this.http.get<PaginatedResponse<Product>>(this.baseUri + '/search', {params});
  }

  getFilteredProductsByCompetitorId(competitorId: number, page: number, size: number, query: string, category: string): Observable<PaginatedResponse<Product>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('query', query)
      .set('category', category)
      .set('competitorId', competitorId.toString());
    return this.http.get<PaginatedResponse<Product>>(this.baseUri + '/search', {params});
  }

  private setCategories(categories: ProductCategorie[]) {
    localStorage.setItem('categories', JSON.stringify(categories));
  }

  public getCategories(): ProductCategorie[] | null {
    const categoriesString = localStorage.getItem('categories');
    return categoriesString ? JSON.parse(categoriesString) : null;
  }

  // This method transforms the categories into an Observable for use with the entityEndpoint
  public searchCategories(searchTerm: string): Observable<ProductCategorie[]> {
    const categories = this.getAllProductCategories()
    return categories.pipe(
      map((categories) => {
        if (!categories) {
          return []; // Return an empty array if categories are null
        }
        // Filter categories based on searchTerm (case insensitive)
        return categories.filter(category =>
          category.category.toLowerCase().includes(searchTerm.toLowerCase())
        );
      })
    );
  }

  getCategoriesByIds(appliedCategories: string[]): Observable<ProductCategorie[]> {
    return this.getAllProductCategories().pipe(
      map(categories => {
        // Filter categories by the provided IDs
        return categories.filter(category => appliedCategories.includes(category.category));
      })
    );
  }

  getProductsByIds(appliedProductIds: number[]): Observable<ProductDetailsWithPrices[]> {
    if (!appliedProductIds || appliedProductIds.length === 0) {
      return of([]); // Return an empty array if no product IDs are provided
    }

    return from(appliedProductIds).pipe(
      mergeMap(id =>
        this.getProductDetailsByProductId(id).pipe(
          catchError(() => of(null)) // Handle errors for each product individually
        )
      ),
      toArray(), // Collect results into a single array
      map(results => results.filter((result): result is ProductDetailsWithPrices => result !== null)) // Filter out null results
    );
  }
}
