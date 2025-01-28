import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Globals} from '../../global/globals';
import {map, Observable} from 'rxjs';
import {WebhookDto} from '../../dtos/webhookDto';

@Injectable({
  providedIn: 'root'
})
export class WebhookService {
  private baseUri: string;
  constructor(private http: HttpClient, private globals: Globals) {
    this.baseUri = this.globals.backendUri + 'webhook'
  }

  getWebhook(): Observable<WebhookDto> {
    console.log("Fetching webhook");
    return this.http.get<WebhookDto>(this.baseUri);
  }

  createWebhook(webhookData: { url: string; secret?: string }): Observable<string> {
    const webhookDto = new WebhookDto(webhookData.url, webhookData.secret || '');

    // Send the WebhookDto object in the POST request
    return this.http.post<string>(this.baseUri + '/create', webhookDto, { responseType: 'text' as 'json' });
    //return this.http.post<void>(this.baseUri + '/create', webhookDto);
  }

  updateWebhook(url: string): Observable<void> {
    const webhookDto = new WebhookDto(url, '');
    return this.http.put<void>(this.baseUri + '/update-url', webhookDto, { responseType: 'text' as 'json' });
  }

  deleteWebhook(): Observable<void> {
    return this.http.delete<void>(this.baseUri);
  }

  addProductToWebhook(productId: number): Observable<string> {
    return this.http.post<string>(`${this.baseUri}/add-product/${productId}`, null, { responseType: 'text' as 'json' });
  }

  triggerWebhookForUser(): Observable<string> {
    return this.http.post<string>(`${this.baseUri}/send-data`, null, { responseType: 'text' as 'json' });
  }


}
