import {Component, Input} from '@angular/core';
import {StoreProductWithPriceDTO} from '../../../models/store-product-with-price';
import {MatCard, MatCardActions, MatCardContent} from '@angular/material/card';
import {MatIcon} from '@angular/material/icon';
import {CurrencyPipe, NgIf} from '@angular/common';
import {MatGridList, MatGridTile} from '@angular/material/grid-list';
import {MatFabButton} from '@angular/material/button';
import {MatTooltip} from '@angular/material/tooltip';
import {StoreProductService} from '../../../services/store-product/store-product.service';
import {SnackbarService} from "../../../shared/utils/snackbar/snackbar.service";

@Component({
  selector: 'app-product-details-card',
  standalone: true,
  imports: [
    MatCard,
    MatCardContent,
    MatCardActions,
    MatIcon,
    CurrencyPipe,
    MatGridList,
    MatGridTile,
    MatFabButton,
    MatTooltip,
    NgIf
  ],
  templateUrl: './product-details-card.component.html',
  styleUrl: './product-details-card.component.scss'
})
export class ProductDetailsCardComponent {

  @Input() product: any; // StoreProductWithPriceDTO | null;

  public onWatchlist: boolean = true;

  constructor(private storeProductService: StoreProductService,
              private snackbarService: SnackbarService) {
  }

  get hasPrices() : boolean {
    return !!(this.product && (this.product.lowestPrice || this.product.highestPrice));
  }

  editPriceStrategy() {
    console.log("Edit product..." + this.product);
  }

  deleteFromWatchlist() {
    if (!this.product) {
      console.error('No product provided');
      return;
    }
    const {productId, competitorId} = this.product.id;
    this.storeProductService.removeStoreProduct(productId, competitorId).subscribe({
      next: () => {
        this.onWatchlist = false;
        console.log("REMOVED");
        this.snackbarService.success('Product removed from watchlist');
      },
      error: (error) => {
        let errorMessage;
        if (typeof error.error === 'object') {
          errorMessage = error.error.error;
        } else {
          errorMessage = error.error;
        }
        console.log('Error deleting account: ', error);
        this.snackbarService.error('Could not remove from watchlist ' + errorMessage);
    }
    })
  }

}
