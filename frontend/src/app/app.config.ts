import {ApplicationConfig, importProvidersFrom, provideZoneChangeDetection} from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import {HttpClient, HttpClientModule, provideHttpClient} from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { httpInterceptorProviders } from './interceptors'; // Import the barrel

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    provideAnimationsAsync('noop'),
    importProvidersFrom(HttpClientModule),
    ...httpInterceptorProviders, // Spread the interceptor providers from the barrel
  ]
};
