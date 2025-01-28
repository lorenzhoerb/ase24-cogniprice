import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {AuthService} from '../services/auth/auth.service';
import {Observable} from 'rxjs';
import {Globals} from '../global/globals';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService, private globals: Globals) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authUri = this.globals.backendUri + 'auth';
    if (!req.url.startsWith(authUri)) {
      const authReq = req.clone({
        headers: req.headers.set('Authorization', '' + this.authService.getToken())
      });
      console.log('Intercepted request:', authReq);
      return next.handle(authReq);
    }
    return next.handle(req);
  }
}
