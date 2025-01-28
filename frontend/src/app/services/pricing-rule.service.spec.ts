import { TestBed } from '@angular/core/testing';

import { PricingRuleService } from './pricing-rule.service';

describe('PricingRuleService', () => {
  let service: PricingRuleService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PricingRuleService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
