export class EditUserDto {
  constructor(
    public email: string,
    public username: string,
    public firstName: string,
    public lastName: string
  ) {}
}
