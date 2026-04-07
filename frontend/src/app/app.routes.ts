import { Routes } from '@angular/router';
import { AddCompanyComponent } from './components/add-company/add-company.component';
import { AddExchangeComponent } from './components/add-exchange/add-exchange.component';
import { AddIpoComponent } from './components/add-ipo/add-ipo.component';
import { CompanyComponent } from './components/company/company.component';
import { ComparisonComponent } from './components/comparison/comparison.component';
import { ExcelDataComponent } from './components/excel-data/excel-data.component';
import { ExchangeComponent } from './components/exchange/exchange.component';
import { HomeComponent } from './components/home/home.component';
import { IpoComponent } from './components/ipo/ipo.component';
import { InvestorDeskComponent } from './components/investor-desk/investor-desk.component';
import { LoginComponent } from './components/login/login.component';
import { NotFoundComponent } from './components/not-found/not-found.component';
import { PlaceOrderComponent } from './components/place-order/place-order.component';
import { SectorComponent } from './components/sector/sector.component';
import { SignupComponent } from './components/signup/signup.component';
import { StockComponent } from './components/stock/stock.component';
import { adminGuard, authGuard } from './guards/route-auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'company', component: CompanyComponent, canActivate: [adminGuard] },
  { path: 'addCompany', component: AddCompanyComponent, canActivate: [adminGuard] },
  { path: 'updateCompany/:id', component: AddCompanyComponent, canActivate: [adminGuard] },
  { path: 'comparison', component: ComparisonComponent, canActivate: [authGuard] },
  { path: 'exchange', component: ExchangeComponent, canActivate: [adminGuard] },
  { path: 'addExchange', component: AddExchangeComponent, canActivate: [adminGuard] },
  { path: 'updateExchange/:id', component: AddExchangeComponent, canActivate: [adminGuard] },
  { path: 'ipo', component: IpoComponent, canActivate: [authGuard] },
  { path: 'investor', component: InvestorDeskComponent, canActivate: [authGuard] },
  { path: 'place-order', component: PlaceOrderComponent, canActivate: [authGuard] },
  { path: 'addIpo', component: AddIpoComponent, canActivate: [adminGuard] },
  { path: 'updateIpo/:id', component: AddIpoComponent, canActivate: [adminGuard] },
  { path: 'sector', component: SectorComponent, canActivate: [authGuard] },
  { path: 'stock', component: StockComponent, canActivate: [adminGuard] },
  { path: 'importData', component: ExcelDataComponent, canActivate: [adminGuard] },
  { path: '**', component: NotFoundComponent }
];