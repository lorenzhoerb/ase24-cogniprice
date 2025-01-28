import { TestBed } from '@angular/core/testing';

import { ApiDocsService } from './api-docs.service';

describe('ApiDocsService', () => {
  let service: ApiDocsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ApiDocsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
