import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StoreProductSelectorComponent } from './store-product-selector.component';

describe('StoreProductSelectorComponent', () => {
  let component: StoreProductSelectorComponent;
  let fixture: ComponentFixture<StoreProductSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StoreProductSelectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StoreProductSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
