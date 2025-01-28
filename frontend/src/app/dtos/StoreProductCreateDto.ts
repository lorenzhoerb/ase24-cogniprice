export class StoreProductCreateDto {
  constructor(
    public productId: number,
    public competitorId: number,
    public productUrl: string)
  {}
}
