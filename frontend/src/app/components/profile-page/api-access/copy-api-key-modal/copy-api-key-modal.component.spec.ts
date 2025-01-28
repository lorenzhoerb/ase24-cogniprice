import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CopyApiKeyModalComponent } from './copy-api-key-modal.component';

describe('CopyApiKeyModalComponent', () => {
  let component: CopyApiKeyModalComponent;
  let fixture: ComponentFixture<CopyApiKeyModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CopyApiKeyModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CopyApiKeyModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
