import {Component, EventEmitter, Input, Output} from '@angular/core';
import {SearchSelectorComponent} from '../../search-selector/search-selector.component';
import {map, Observable} from 'rxjs';
import {StoreProductService} from '../../../services/store-product/store-product.service';
import {ProductDetailsWithPrices} from '../../../models/product-details-with-prices';

@Component({
  selector: 'app-store-product-selector',
  standalone: true,
  imports: [
    SearchSelectorComponent
  ],
  templateUrl: './store-product-selector.component.html',
  styleUrl: './store-product-selector.component.scss'
})
export class StoreProductSelectorComponent {

  @Input() maxResults: number = 10;
  @Input() label: string = 'Products';
  @Output() entitySelected: EventEmitter<any> = new EventEmitter();

  constructor(public storeProductService: StoreProductService) {
  }

  displayNameMapper(product: ProductDetailsWithPrices): string {
    return product.productName;
  }

  searchProducts(searchTerm: string): Observable<any[]> {
    return this.storeProductService
      .getAllProductsOfUser(0, this.maxResults, searchTerm, 'a')
      .pipe(map(pageable => pageable.content))
  }
}
