import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Exchange } from 'src/app/models/exchange-model';
import { AppDialogService } from 'src/app/services/app-dialog.service';
import { AuthService } from 'src/app/services/auth.service';
import { ExchangeService } from 'src/app/services/exchange.service';
import { LiveAnnouncerService } from 'src/app/services/live-announcer.service';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-exchange',
  templateUrl: './exchange.component.html',
  styleUrls: ['./exchange.component.css'],
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent]
})
export class ExchangeComponent implements OnInit {

  public state:string;
  public exchanges:Exchange[];
  public errorMessage:string;
  public successMessage:string;

  constructor(private authService:AuthService, private exchangeService:ExchangeService, private cdr: ChangeDetectorRef, private liveAnnouncer: LiveAnnouncerService, private appDialog: AppDialogService) {
    this.state="";
    this.exchanges=[];
    this.errorMessage = '';
    this.successMessage = '';
  }

  ngOnInit(): void {
    this.state = this.authService.getCurrentUserRole();
    this.exchangeService.getAllExchanges().subscribe( allExchanges => {
      this.exchanges = allExchanges;
      this.cdr.detectChanges();
    });
  }

  public async deleteExchange(exchange:Exchange):Promise<void> {
    const confirmed = await this.appDialog.confirm(`Do you want to delete exchange ${exchange.name} with ID ${exchange.id}?`, {
      title: 'Delete Exchange',
      confirmLabel: 'Delete'
    });
    if (!confirmed) {
      return;
    }

    this.liveAnnouncer.announceStatus(`Deleting exchange ${exchange.name} (ID ${exchange.id}).`);
    this.exchangeService.deleteExchange(exchange.id).subscribe({
      next: () => {
        this.successMessage = `Exchange ${exchange.name} (ID ${exchange.id}) deleted successfully.`;
        this.errorMessage = '';
        this.liveAnnouncer.announceSuccess(this.successMessage);
        this.ngOnInit();
        this.cdr.detectChanges();
      },
      error: (err) => {
          this.errorMessage = err?.message || `Failed to delete exchange. Please try again.`;
        this.successMessage = '';
        this.liveAnnouncer.announceError(this.errorMessage);
      }
    });
  }

}
