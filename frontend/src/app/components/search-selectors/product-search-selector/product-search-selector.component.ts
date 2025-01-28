import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ProductService} from '../../../services/product/product.service';
import {Observable} from 'rxjs';
import {Product} from '../../../models/product';
import {SearchSelectorComponent} from '../../search-selector/search-selector.component';

@Component({
  selector: 'app-product-search-selector',
  standalone: true,
  imports: [
    SearchSelectorComponent
  ],
  templateUrl: './product-search-selector.component.html',
  styleUrl: './product-search-selector.component.scss'
})
export class ProductSearchSelectorComponent {
  @Input() maxResults: number = 10;
  @Input() label: string = 'Categories';
  @Output() entitySelected: EventEmitter<any> = new EventEmitter();

  constructor(public productService: ProductService) {
  }

  displayNameMapper(product: Product): string {
    return product.name;
  }

  searchProducts(searchTerm: string): Observable<any[]> {
    return this.productService.getAutocompleteProducts(searchTerm);
  }
}
