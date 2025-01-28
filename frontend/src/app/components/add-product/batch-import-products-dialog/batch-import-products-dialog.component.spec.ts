import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BatchImportProductsDialogComponent } from './batch-import-products-dialog.component';

describe('BatchImportProductsDialogComponent', () => {
  let component: BatchImportProductsDialogComponent;
  let fixture: ComponentFixture<BatchImportProductsDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BatchImportProductsDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BatchImportProductsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
