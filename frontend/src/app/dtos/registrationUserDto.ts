export class RegistrationUserDto {
    constructor(
        public email: string,
        public password: string,
        public username: string,
        public firstName: string,
        public lastName: string
    ) {}
}
