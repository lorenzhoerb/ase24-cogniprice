export interface Product {
  id: number; // Use 'number' for a long integer type
  name: string;
  gtin: string;
  imageBase64: string;
  productImageUrl: string;
  category: string;
  productUrl? : string | null;
}
