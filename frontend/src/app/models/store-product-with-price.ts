export interface StoreProductId {
  productId: number;
  competitorId: number;
}

export interface PriceDetails {
  price: number;
  competitorName: string;
  currency: string;
  priceTime: string;
}

export interface StoreProductWithPriceDTO {
  productId: number;
  productName: string;
  category: string;
  thumbnailBase64: string | null;
  lowestPrice: PriceDetails;
  highestPrice: PriceDetails;
}

export interface StoreProductDetailsDTO {
  productId: number;
  productName: string;
  category: string;
  productUrl: string;
  thumbnailBase64: string | null;
  lowestPrice: PriceDetails;
  highestPrice: PriceDetails;
}
