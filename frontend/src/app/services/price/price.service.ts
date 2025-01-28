import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Globals} from '../../global/globals';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PriceService {

  private baseUri: string;

  constructor(private http: HttpClient, private globals: Globals) {
    this.baseUri = this.globals.backendUri + 'productPrices'
  }

  /**
   * Fetches the price history for a given product ID.
   * @param productId The product ID for which to fetch the price history.
   * @returns An Observable of the price history data.
   */
  getPriceHistoryByProductId(productId: number): Observable<any> {
    const url = `${this.baseUri}/${productId}`;
    return this.http.get<any>(url);
  }
}
