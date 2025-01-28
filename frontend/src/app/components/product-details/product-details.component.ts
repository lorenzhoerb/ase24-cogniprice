import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {StoreProductService} from '../../services/store-product/store-product.service';
import {StoreProductDetailsDTO, StoreProductWithPriceDTO} from '../../models/store-product-with-price';
import {MatButton} from '@angular/material/button';
import {MatGridList, MatGridTile} from '@angular/material/grid-list';
import {MatIcon} from '@angular/material/icon';
import {ProductDetailsCardComponent} from './product-details-card/product-details-card.component';
import {PriceGraphComponent} from './price-graph/price-graph.component';
import {ProductDetailsWithPrices} from "../../models/product-details-with-prices";
import {ProductService} from '../../services/product/product.service';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-product-details',
  standalone: true,
  imports: [
    MatButton,
    MatGridList,
    MatGridTile,
    MatIcon,
    ProductDetailsCardComponent,
    PriceGraphComponent,
    NgIf
  ],
  templateUrl: './product-details.component.html',
  styleUrl: './product-details.component.scss'
})
export class ProductDetailsComponent implements OnInit {
  productId: number | null = null;
  product: ProductDetailsWithPrices | null = null;
  isLoading: boolean = true;

  constructor(private route: ActivatedRoute, private productService: ProductService) {
  }

  ngOnInit() {
    console.log("INIT");
    this.route.paramMap.subscribe((params) => {
      this.productId = params.get('productId') ? parseInt(params.get('productId')!, 10) : null;
      console.log("productID: ", this.productId);

      if (this.productId) {
        this.getProduct(this.productId);
      }
    })
  }

  getProduct(productId: number) {
    console.log("Getting " + productId);
    this.productService.getProductDetailsByProductId(productId).subscribe({
      next: (product) => {
        console.log("Fetch successful");
        this.product = this.ensurePriceConsistency(product);
        console.log("Product: ", this.product);
        this.isLoading = false;
      },
      error: (err) => {
        console.log("Error while fetching Product: " + err)
      }
    })
  }

  getImage(): string {
    if (this.product?.productImageBase64) {
      return 'data:image/jpeg;base64,' + this.product?.productImageBase64;
    } else if (this.product?.productImageUrl) {
      return this.product.productImageUrl;
    } else {
     return '/not-found.png';
    }
  }

  // Check if only has 1 price
  private ensurePriceConsistency(product: ProductDetailsWithPrices) {
    if (product.lowestPrice && !product.highestPrice) {
      product.highestPrice = product.lowestPrice;
    }
    if (product.highestPrice && !product.lowestPrice) {
      product.lowestPrice = product.highestPrice;
    }
    return product;
  }


}
