import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategorySearchSelectorComponent } from './category-search-selector.component';

describe('CategorySearchSelectorComponent', () => {
  let component: CategorySearchSelectorComponent;
  let fixture: ComponentFixture<CategorySearchSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategorySearchSelectorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategorySearchSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
