import {PriceDetails} from "./store-product-with-price";

export interface ProductDetailsWithPrices {
    productId: number;
    productName: string;
    gtin: string;
    category: string;
    productImageBase64: string | null;
    // thumbnailBase64: string | null;
    productImageUrl: string;
    lowestPrice: PriceDetails;
    highestPrice: PriceDetails;
}
