import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProductSearchSelectorComponent } from './product-search-selector.component';

describe('ProductSearchSelectorComponent', () => {
  let component: ProductSearchSelectorComponent;
  let fixture: ComponentFixture<ProductSearchSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductSearchSelectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ProductSearchSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
