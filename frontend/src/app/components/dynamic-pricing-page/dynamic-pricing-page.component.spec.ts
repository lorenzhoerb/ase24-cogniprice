import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DynamicPricingPageComponent } from './dynamic-pricing-page.component';

describe('DynamicPricingPageComponent', () => {
  let component: DynamicPricingPageComponent;
  let fixture: ComponentFixture<DynamicPricingPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DynamicPricingPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DynamicPricingPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
