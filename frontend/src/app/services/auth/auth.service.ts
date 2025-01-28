import {HttpClient, HttpParams} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Globals } from '../../global/globals';
import { RegistrationUserDto } from '../../dtos/registrationUserDto';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import {AuthUserDto} from '../../dtos/authUserDto';
import jwt_decode, {jwtDecode} from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authBaseUri: string;

  constructor(private httpClient: HttpClient, private globals: Globals) {
    this.authBaseUri = this.globals.backendUri + 'auth'
  }

  /**
   * Registers the user
   *
   * @param registrationUserDto
   * @returns string
   */
  registerUser(registrationUserDto: RegistrationUserDto): Observable<string> {
    const url = this.authBaseUri + '/registration';
    console.log(url)
    console.log("Sending request to: ", url);
    return this.httpClient.post(url, registrationUserDto, {responseType: 'text', headers: {}})
      .pipe(
        tap(response => this.setToken(response))
      );
  }

  /**
   * login the user
   *
   * @param authRequest
   * @returns string
   */
  loginUser(authRequest: AuthUserDto): Observable<string> {
    const url = this.authBaseUri + '/login';
    console.log("Sending request to: ", url);
    return this.httpClient.post(url, authRequest, {responseType: 'text', headers: {}})
      .pipe(
        tap(response =>this.setToken(response))
      );
  }

  resetPassword(email: string): Observable<void>{
    const url = this.authBaseUri + '/resetPassword';
    return this.httpClient.post<void>(url, {email});
  }

  changePassword(token: string, newPassword: any): Observable<void> {
    const url = this.authBaseUri + '/changePassword';
    console.log(token);
    console.log(newPassword);
    const body = {
      token: token,
      newPassword: newPassword
    };

    // Send the request as a JSON body
    return this.httpClient.post<void>(url, body);
  }

  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    return !!this.getToken() && (this.getTokenExpirationDate(this.getToken()!)!.valueOf() > new Date().valueOf());
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('user');
    localStorage.removeItem('authToken');
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  /**
   * Returns the user role based on the current token
   */
  getUserRole() {
    const token = this.getToken();
    if (token) {
      const decoded: any = jwtDecode(token);
      const authInfo: string[] = decoded.role;
      if (authInfo.includes('ROLE_ADMIN')) {
        return 'ADMIN';
      } else if (authInfo.includes('ROLE_USER')) {
        return 'USER';
      } else if (authInfo.includes('ROLE_API_USER')) {
        return 'USER';
      }
    }
    return 'UNDEFINED';
  }

  updateToken(newToken: string) : void {
    if (newToken) {
      this.setToken(newToken);
    }
  }

  private setToken(authResponse: string) {
    localStorage.setItem('authToken', authResponse);
  }

  private getTokenExpirationDate(token: string): Date | null {

    const decoded: any = jwtDecode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }


}
