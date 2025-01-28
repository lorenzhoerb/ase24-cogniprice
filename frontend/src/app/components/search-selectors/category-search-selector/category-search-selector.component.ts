import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SearchSelectorComponent} from '../../search-selector/search-selector.component';
import {ProductService} from '../../../services/product/product.service';
import {ProductCategorie} from '../../../models/product-categorie';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-category-search-selector',
  standalone: true,
  imports: [
    SearchSelectorComponent
  ],
  templateUrl: './category-search-selector.component.html',
  styleUrl: './category-search-selector.component.scss'
})
export class CategorySearchSelectorComponent {

  @Input() maxResults: number = 10;
  @Input() label: string = 'Categories';
  @Output() entitySelected: EventEmitter<any> = new EventEmitter();


  constructor(public productService: ProductService) {
  }

  displayNameMapper(category: ProductCategorie): string {
    return category.category;
  }

  searchCategories(searchTerm: string): Observable<any[]> {
    return this.productService.searchCategories(searchTerm);
  }
}
