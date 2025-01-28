export class WebhookDto {
  constructor(
    public callbackUrl: string,
    public secret: string)
    {}
}
