export class ProductBatchDto {
  name!: string;
  gtin!: string;
  productCategory!: string;
  productImageUrl?: string;
  competitor?: string;
  productUrl?: string;

  constructor(data: Partial<ProductBatchDto>) {
    this.name = data.name!;
    this.gtin = data.gtin!;
    this.productCategory = data.productCategory!;
    this.productImageUrl = data.productImageUrl;
    this.competitor = data.competitor;
    this.productUrl = data.productUrl;
  }

  validate(): string[] {
    const errors: string[] = [];

    if (!this.name || this.name.length < 1 || this.name.length > 255) {
      errors.push('Invalid product name (1-255 characters required)');
    }

    if (this.gtin && !/^[0-9]{8,13}$/.test(this.gtin)) {
      errors.push('Invalid GTIN (must be 8-13 digits)');
    }

    if (!this.productCategory) {
      errors.push('Product category is required');
    }

    if (this.productImageUrl && !this.isValidUrl(this.productImageUrl)) {
      errors.push('Invalid product image URL');
    }

    if (this.productUrl && !this.isValidUrl(this.productUrl)) {
      errors.push('Invalid product URL');
    }

    if (this.competitor && this.productUrl && !this.productUrl.includes(this.competitor.toLowerCase())) {
      errors.push('Product URL must contain the competitor');
    }

    return errors;
  }

  private isValidUrl(url: string): boolean {
    const urlRegex =
      /^(https?:\/\/)([^\s\/$.?#].[^\s]*)$/i;
    return urlRegex.test(url);
  }
}
