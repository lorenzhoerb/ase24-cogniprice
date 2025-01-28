import {Directive, EventEmitter, HostBinding, HostListener, Output} from '@angular/core';
import {FileHandle} from '../models/file-handle';
import {DomSanitizer} from '@angular/platform-browser';

@Directive({
  selector: '[appDrag]',
  standalone: true
})
export class DragDirective {

  @Output() files: EventEmitter<FileHandle> = new EventEmitter();

  @HostBinding("style.background") private background = "#eee";
  constructor(private sanitizer: DomSanitizer) { }

  @HostListener("dragover", ["$event"])
  public onDragOver(evt: DragEvent){
    evt.preventDefault();
    evt.stopPropagation();
    this.background = "#999";
  }

  @HostListener("dragleave", ["$event"])
  public onDragLeave(evt: DragEvent) {
    evt.preventDefault();
    evt.stopPropagation();
    this.background = "#eee";
  }

  @HostListener("drop", ["$event"])
  public onDrop(evt: DragEvent){
    evt.preventDefault();
    evt.stopPropagation();
    this.background = "#eee";

    if (!evt.dataTransfer) {
      console.error("Data transfer is null.");
      return;
    }

    const file = evt.dataTransfer.files[0];
    const url = this.sanitizer.bypassSecurityTrustUrl(window.URL.createObjectURL(file));

    let fileHandle: FileHandle = {file, url};
    this.files.emit(fileHandle);


  }

}
