import {Component, OnInit} from '@angular/core';
import {MatGridList, MatGridTile} from '@angular/material/grid-list';
import {CompetitorDetails} from '../../models/competitor-details';
import {CompetitorService} from '../../services/competitor/competitor.service';
import {MatList, MatListItem} from '@angular/material/list';
import {NgForOf, NgIf} from '@angular/common';
import {MatCard, MatCardContent} from '@angular/material/card';
import {Router} from '@angular/router';
import {MatButton} from '@angular/material/button';
import {MatFormField, MatLabel, MatPrefix} from '@angular/material/form-field';
import {MatIcon} from '@angular/material/icon';
import {MatInput} from '@angular/material/input';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-competitors',
  standalone: true,
  imports: [
    MatGridList,
    MatGridTile,
    MatListItem,
    MatList,
    NgForOf,
    NgIf,
    MatCard,
    MatCardContent,
    MatButton,
    MatFormField,
    MatIcon,
    MatInput,
    MatLabel,
    MatPrefix,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './competitors.component.html',
  styleUrl: './competitors.component.scss'
})
export class CompetitorsComponent implements OnInit {
  competitors: CompetitorDetails[] = [];
  filteredCompetitors: CompetitorDetails[] = [];
  isLoading: boolean = true;
  searchQuery: string = '';

  constructor(private competitorService: CompetitorService, private router: Router) {
  }

  ngOnInit() {
    this.fetchCompetitors();
  }

  fetchCompetitors(): void {
    this.competitorService.getAllCompetitors().subscribe((data) => {
      this.competitors = data;
      this.filteredCompetitors = [...data];
      this.isLoading = false;
    })
  }

  viewProducts(competitorId: number) : void {
    this.router.navigate(['/products'], { queryParams: { competitorId }});
  }

  navigateToCreateCompetitor(): void {
    this.router.navigate(['/create-competitor']);
  }

  onSearch(event: any): void {
    const query = event.target.value.toLowerCase();
    this.filteredCompetitors = this.competitors.filter(competitor =>
      competitor.name.toLowerCase().includes(query) ||
      competitor.hostname.toLowerCase().includes(query)
    );
    console.log(this.filteredCompetitors);
  }
}
