import {Component, EventEmitter, Input, Output} from '@angular/core';
import {MatFormField, MatFormFieldModule} from '@angular/material/form-field';
import {MatAutocomplete, MatAutocompleteModule, MatOption} from '@angular/material/autocomplete';
import {MatInput, MatInputModule} from '@angular/material/input';
import {debounceTime, map, Observable, of, switchMap} from 'rxjs';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {AsyncPipe, CommonModule} from '@angular/common';

@Component({
  selector: 'app-search-selector',
  standalone: true,
  imports: [
    MatFormField,
    MatAutocomplete,
    MatInput,
    AsyncPipe,
    ReactiveFormsModule,
    MatOption,
    MatFormFieldModule,
    MatInputModule,
    CommonModule,
    MatAutocompleteModule
  ],
  templateUrl: './search-selector.component.html',
  styleUrl: './search-selector.component.scss'
})
export class SearchSelectorComponent {
  @Input() searchEndpoint!: ((searchTerm: string) => Observable<any[]>);
  @Input() displayNameMapper!: ((entity: any) => string);
  @Input() label: string = 'Search';
  @Input() maxResults: number = 10;
  @Output() entitySelected: EventEmitter<any> = new EventEmitter();

  searchControl = new FormControl();
  filteredEntities: Observable<any[]> = of([]);
  entities: any[] = [];

  constructor() {
  }

  ngOnInit() {
    this.filteredEntities = this.searchControl.valueChanges.pipe(
      debounceTime(300), // Wait for 300ms after typing to trigger search
      switchMap((searchTerm) => this.searchEndpoint(searchTerm)), // Call the searchEndpoint directly
      map((entities) => entities.slice(0, this.maxResults)) // Limit the number of results displayed
    );

    this.filteredEntities.subscribe((s) => {
      this.entities = s;
      console.log(s)
    })
  }

  onEntityClick(entity: any) {
    this.entitySelected.emit(entity);
    this.searchControl.setValue('')
  }

}
