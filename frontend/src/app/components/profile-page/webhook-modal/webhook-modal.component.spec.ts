import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WebhookModalComponent } from './webhook-modal.component';

describe('WebhookModalComponent', () => {
  let component: WebhookModalComponent;
  let fixture: ComponentFixture<WebhookModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WebhookModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WebhookModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
