import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PriceRuleFormComponent } from './price-rule-form.component';

describe('PriceRuleFormComponent', () => {
  let component: PriceRuleFormComponent;
  let fixture: ComponentFixture<PriceRuleFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PriceRuleFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PriceRuleFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
