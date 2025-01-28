import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class Globals {
  readonly backendUri = this.getBackendUri();

  private getBackendUri(): string {
    // Check if it's running in local development mode
    if (window.location.port == '4200') {
      return 'http://localhost:8080/api/';  // This works locally when running with `ng serve`
    } else {
      // hard coded because of some nginx deploy issue which i could not fix

      return 'https://24ws-ase-pr-qse-02.apps.student.inso-w.at/' + 'api/';
      //return window.location.protocol + '//' + window.location.host + window.location.pathname + 'api/';
    }
  }
}
