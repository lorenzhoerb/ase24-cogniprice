import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserEditDialogueComponent } from './user-edit-modal.component';

describe('UserEditDialogueComponent', () => {
  let component: UserEditDialogueComponent;
  let fixture: ComponentFixture<UserEditDialogueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserEditDialogueComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserEditDialogueComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
