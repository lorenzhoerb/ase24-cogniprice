import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Globals} from '../../global/globals';
import {Observable} from 'rxjs';
import {UserDetails} from '../../models/user-details';
import {EditUserDto} from '../../dtos/editUserDto';
import {ApiKeyInfo} from '../../models/ApiKeyInfo';

@Injectable({
  providedIn: 'root'
})
export class UserDetailsService {

  private baseUri: string;

  constructor(private httpClient: HttpClient, private globals: Globals) {
    this.baseUri = this.globals.backendUri + 'users'
  }

  /**
   * Get user details based on token
   */
  getUserDetails(): Observable<UserDetails> {
    const url = `${this.baseUri}/details`;
    return this.httpClient.get<UserDetails>(url);
  }

  /**
   * Get user details by its username
   * @param username
   */
  public getUserDetailsByUsername(username: string): Observable<UserDetails> {
    const url = `${this.baseUri}/${username}`;
    return this.httpClient.get<UserDetails>(url);
  }

  /**
   * Update user details
   * @param editUserDTO
   */
  public editUser(editUserDTO: EditUserDto): Observable<string | void> {
    const url = `${this.baseUri}/edit`;
    return this.httpClient.put(url, editUserDTO, {responseType: 'text'});
  }

  /**
   * Delete the currently logged-in user.
   *
   * @return an Observable for the deleted user
   */
  public deleteCurrentUser(): Observable<UserDetails> {
    const url = `${this.baseUri}`;
    return this.httpClient.delete<UserDetails>(
      url
    );
  }

  /**
   * Get API Key Information without cleartext.
   *
   * @return the details about the api key.
   */
  public getApiKeyInfo(): Observable<ApiKeyInfo> {
    const url = `${this.baseUri}/api-key`;
    return this.httpClient.get<ApiKeyInfo>(url);
  }

  /**
   * Generate an API Key.
   *
   * @return the api key to copy it.
   */
  generateApiKey(): Observable<string> {
    const url = `${this.baseUri}/api-key/generate`;
    return this.httpClient.post<string>(url, null, { responseType: 'text' as 'json' });
  }


  public getBackendUri(): string {
    return this.globals.backendUri;
  }

  changePassword(oldPassword: any, newPassword: any): Observable<void> {
    const url = this.baseUri + '/changePassword';
    const body = {
      oldPassword: oldPassword,
      newPassword: newPassword
    };
    // Send the request as a JSON body
    return this.httpClient.post<void>(url, body);
  }
}
