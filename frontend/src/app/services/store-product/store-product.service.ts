import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {catchError, map, Observable, of} from 'rxjs';
import {StoreProductDetailsDTO, StoreProductWithPriceDTO} from '../../models/store-product-with-price';
import {Globals} from '../../global/globals';
import {PaginatedResponse} from '../../models/paginated-response';
import {StoreProductCreateDto} from '../../dtos/StoreProductCreateDto';
import {ProductDetailsWithPrices} from '../../models/product-details-with-prices';

@Injectable({
  providedIn: 'root',
})
export class StoreProductService {
  private baseUri: string;

  constructor(private http: HttpClient, private globals: Globals) {
    this.baseUri = this.globals.backendUri + 'storeProducts'
  }

  getAllProductsOfUser(page: number, size: number, name: string, category: string): Observable<PaginatedResponse<ProductDetailsWithPrices>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('name', name)
      .set('category', category);
    return this.http.get<PaginatedResponse<ProductDetailsWithPrices>>(this.baseUri + '/view', {params});
  }

  createStoreProduct(storeProductDto: StoreProductCreateDto): Observable<void> {
    return this.http.post(this.baseUri +'/create', storeProductDto).pipe(map(() => undefined));
  }



  // Changed to product service for now
  getProductByProductIdAndCompetitorId(productId: number, competitorId: number): Observable<StoreProductDetailsDTO> {
    return this.http.get<StoreProductDetailsDTO>(this.baseUri + '/' + productId + '/' + competitorId);
  }

  removeStoreProduct(productId: number, competitorId: number): Observable<void> {
    const storeProductId = {productId, competitorId};
    return this.http.delete<void>(this.baseUri + '/remove', {body: storeProductId});
  }

  removeProductFromWatchList(productId: number): Observable<void> {
    return this.http.delete<void>(this.baseUri + '/removeAll', {body: productId});
  }
}
