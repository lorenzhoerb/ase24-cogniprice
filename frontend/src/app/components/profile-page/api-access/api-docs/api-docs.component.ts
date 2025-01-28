import {AfterViewInit, Component, OnInit, ViewEncapsulation} from '@angular/core';
import SwaggerUI from 'swagger-ui';
import {ApiDocsService} from '../../../../services/api/api-docs.service';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-api-docs',
  standalone: true,
  imports: [
    NgIf
  ],
  templateUrl: './api-docs.component.html',
  styleUrl: './api-docs.component.scss',
  encapsulation: ViewEncapsulation.None
})
export class ApiDocsComponent implements AfterViewInit {
  fetchSuccessful: boolean = true;

  private readonly pathWhitelist: { path: string; methods: string[] }[] = [
    /* Webhook Endpoints */
    /*{ path: '/api/webhook/update-url', methods: ['PUT'] },*/
    { path: '/api/webhook/send-data', methods: ['POST'] },
    { path: '/api/webhook/create', methods: ['POST'] },
    /*{ path: '/api/webhook/add-product/{productId}', methods: ['POST'] },*/
    { path: '/api/webhook', methods: ['GET', 'DELETE'] },
    /*{ path: '/api/webhook/remove-product/{productId}', methods: ['DELETE'] },*/

    /* Application User */
    { path: '/api/users/edit', methods: ['PUT'] },
    { path: '/api/users/details', methods: ['GET'] },
    { path: '/api/users/api-key', methods: ['GET'] },

    /* Products */
    { path: '/api/products/create', methods: ['POST'] },
    { path: '/api/products/search', methods: ['GET'] },
    /*{ path: '/api/products/details/{productId}', methods: ['GET'] },*/

    /* Pricing Rule */
    /*{ path: '/api/pricing-rules/{id}', methods: ['GET', 'PUT', 'DELETE'] },*/
    { path: '/api/pricing-rules', methods: ['GET', 'POST'] },

    /* competitor controller */
    /*{ path: '/api/competitors/{id}', methods: ['GET'] },*/
    { path: '/api/competitors', methods: ['GET', 'POST'] },

    /* Store Products */
    { path: '/api/storeProducts/view', methods: ['GET'] },

    /* Product Price */
    /*{ path: '/api/productPrices/{productId}', methods: ['GET'] },*/

    /* Product Category */
    { path: '/api/product-categories', methods: ['GET'] },
  ];


  constructor(private apiDocsService: ApiDocsService) {

  }

  ngAfterViewInit() {
    this.apiDocsService.fetchApiDocs().subscribe({
      next: (openApiSpec) => {
        this.fetchSuccessful = true;
        const filteredPaths: Record<string, object> = Object.keys(openApiSpec.paths).reduce((result, path) => {
          const pathSpec = openApiSpec.paths[path];
          const whitelistedPath = this.pathWhitelist.find((entry) => entry.path === path);

          if (whitelistedPath) {
            const filteredMethods = Object.keys(pathSpec).reduce((methodsResult, method) => {
              if (whitelistedPath.methods.includes(method.toUpperCase())) {
                methodsResult[method] = pathSpec[method];
              }
              return methodsResult;
            }, {} as Record<string, object>);

            if (Object.keys(filteredMethods).length > 0) {
              result[path] = filteredMethods;
            }
          }

          return result;
        }, {} as Record<string, object>);

        openApiSpec.paths = filteredPaths;
        SwaggerUI({
          dom_id: '#swagger-ui',
          spec: openApiSpec,
        });
      },
      error: (err) => {
        this.fetchSuccessful = false;
        console.log('Failed to fetch Api documentation');
      }
    });
  }
}
