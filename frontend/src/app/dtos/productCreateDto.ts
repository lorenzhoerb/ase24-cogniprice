export class ProductCreateDto {
  constructor(
  public name: string,
  public gtin: string,
  public productCategoryId: string,
  ) {}
}
