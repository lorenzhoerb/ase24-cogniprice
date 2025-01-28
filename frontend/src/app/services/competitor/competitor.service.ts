import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Globals} from '../../global/globals';
import {Observable} from 'rxjs';
import {CompetitorDetails} from '../../models/competitor-details';
import {ProductCategorie} from '../../models/product-categorie';

@Injectable({
  providedIn: 'root'
})
export class CompetitorService {
  private baseUri: string;
  constructor(private http: HttpClient, private globals: Globals) {
    this.baseUri = this.globals.backendUri + 'competitors'
  }

  createCompetitor(competitor: any): Observable<CompetitorDetails> {
    return this.http.post<CompetitorDetails>(this.baseUri, competitor);
  }

  getAllCompetitors(): Observable<CompetitorDetails[]> {
    return this.http.get<CompetitorDetails[]>(this.baseUri);
  }
}
