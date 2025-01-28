import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PriceRulePageComponent } from './price-rule-page.component';

describe('PriceRulePageComponent', () => {
  let component: PriceRulePageComponent;
  let fixture: ComponentFixture<PriceRulePageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PriceRulePageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PriceRulePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
