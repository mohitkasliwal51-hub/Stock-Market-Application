import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ExcelData } from 'src/app/models/excel-data';
import { AuthService } from 'src/app/services/auth.service';
import { ExcelService } from 'src/app/services/excel.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import * as XLSX from 'xlsx';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-excel-data',
  templateUrl: './excel-data.component.html',
  styleUrls: ['./excel-data.component.css'],
  standalone: true,
  imports: [CommonModule, NavbarComponent]
})
export class ExcelDataComponent implements OnInit {

  public state:string;
  public fileData: [number, number, number, string, string][];
  public unsuccessfullAttempts:number;
  public total:number;
  public isLoading:boolean;
  public errorMessage:string;
  public successMessage:string;

  constructor(private authService:AuthService, private excelService:ExcelService, private liveAnnouncer: LiveAnnouncerService) {
    this.state="";
    this.fileData = [];
    this.unsuccessfullAttempts=0;
    this.total=0;
    this.isLoading = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
  }

  onFileChange(event: any){
    this.errorMessage = '';
    this.successMessage = '';
    const target: DataTransfer = <DataTransfer>(event.target);
    if (!target.files || target.files.length === 0) {
      this.errorMessage = 'Please select an Excel file';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }
    const reader: FileReader = new FileReader();
    reader.onload = (e: any) => {
      const bstr: string = e.target.result;
      const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });
      const wsname : string = wb.SheetNames[0];
      const ws: XLSX.WorkSheet = wb.Sheets[wsname];
      this.fileData = (XLSX.utils.sheet_to_json(ws, { header: 1 }));
      let x = this.fileData.slice(1);
      this.fileData = x;
    };
    reader.readAsBinaryString(target.files[0]);
  }

  onSubmit(){
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.fileData.length) {
      this.errorMessage = 'Please select and parse a file before uploading';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    let excelData: ExcelData[] = [];
    this.fileData.forEach((data)=>{
      var dataLine: [number, number, number, string, string] = data;
      if(dataLine.length){
        excelData.push({
        companyId: dataLine[0],
        exchangeId: dataLine[1],
        price: dataLine[2],
        timestamp: dataLine[3].trim() +"T"+dataLine[4].trim() +".000+05:30"
      });
      }
    })
    this.total = excelData.length;

    if (!excelData.length) {
      this.errorMessage = 'No valid rows found in the selected file';
      this.liveAnnouncer.announceError(this.errorMessage);
      return;
    }

    this.isLoading = true;
    this.liveAnnouncer.announceStatus('Uploading Excel data.');
    console.log(excelData);
    this.excelService.uploadData(excelData).subscribe({
      next: (rowsNotAdded) => {
        this.isLoading = false;
        console.log(rowsNotAdded);
        this.unsuccessfullAttempts = rowsNotAdded.length;
        const rowsAdded = this.total - this.unsuccessfullAttempts;
        this.successMessage = `Upload complete. ${rowsAdded} row(s) added and ${this.unsuccessfullAttempts} row(s) failed.`;
        this.liveAnnouncer.announceSuccess(this.successMessage);
      },
      error: (err) => {
        this.isLoading = false;
          this.errorMessage = err?.message || 'Failed to upload Excel data. Please check your file and try again.';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

}
