import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Globals} from '../global/globals';
import {Observable} from 'rxjs';
import {PaginatedResponse} from '../models/paginated-response';
import {PricingRuleDetails, PricingRuleRequest, SimplePricingRule} from '../models/price-rule';

@Injectable({
  providedIn: 'root'
})
export class PricingRuleService {

  private baseUri: string;

  constructor(private http: HttpClient, private globals: Globals) {
    this.baseUri = globals.backendUri + 'pricing-rules';
  }

  /**
   * Fetches all pricing rules with optional filters for status, page, size, and name.
   *
   * @param page Page number for pagination
   * @param size Number of items per page
   * @param isActive Boolean to filter by status: true = ACTIVE, false = DISABLED, null = no status filter
   * @param name Search query for filtering by name
   * @returns An observable of the API response
   */
  getAllPricingRules(page?: number,
                     size?: number,
                     isActive?: boolean,
                     name?: string
  ): Observable<PaginatedResponse<SimplePricingRule>> {
    console.debug(`getAllPricingRules(page: ${page}, size: ${size}, isActive: ${isActive}, name: ${name})`);
    let params = new HttpParams();


    if (isActive !== undefined) params = params.set('status', isActive ? 'ACTIVE' : 'DISABLED');
    if (page !== undefined) params = params.set('page', page.toString());
    if (size !== undefined) params = params.set('size', size.toString());
    if (name) params = params.set('name', name);

    return this.http.get<PaginatedResponse<SimplePricingRule>>(`${this.baseUri}`, {params});
  }

  /**
   * Fetches a specific pricing rule by its ID.
   *
   * @param id The unique identifier of the pricing rule
   * @returns An observable of the pricing rule details
   */
  getPricingRuleById(id: string): Observable<PricingRuleDetails> {
    const url = `${this.baseUri}/${id}`;  // Assuming the API endpoint is /api/pricing-rules/{id}
    return this.http.get<PricingRuleDetails>(url);
  }

  createPricingRule(pricingRuleRequest: PricingRuleRequest): Observable<PricingRuleDetails> {
    return this.http.post<PricingRuleDetails>(this.baseUri, pricingRuleRequest);
  }

  updatePricingRule(pricingRuleId: string, pricingRuleRequest: PricingRuleRequest): Observable<PricingRuleDetails> {
    return this.http.put<PricingRuleDetails>(`${this.baseUri}/${pricingRuleId}`, pricingRuleRequest)
  }

  /**
   * Deletes a price rule by its ID.
   *
   * @param id The uique identifier of the price rule
   */
  deletePriceRuleById(id: string): Observable<void> {
    const url = `${this.baseUri}/${id}`;
    return this.http.delete<void>(url);
  }
}
