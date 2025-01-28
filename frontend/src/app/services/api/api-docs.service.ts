import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Globals} from '../../global/globals';
import {Observable} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiDocsService {
  private baseUri: string;

  constructor(private http: HttpClient, private globals: Globals) {
    this.baseUri = this.globals.backendUri;
  }

  public fetchApiDocs(): Observable<any> {
    return this.http.get<any>(`${this.baseUri}api-docs`);
  }
}
