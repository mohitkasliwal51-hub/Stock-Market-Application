import { Sector } from "./sector-model";

export type CompanyStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED' | 'DEACTIVATED' | 'BANNED';

export interface Company{
  id:number;
  companyName:string;
  turnover:number;
  ceo:string;
  boardOfDirectors:string;
  sectorId:number;
  briefWriteup:string;
  status?:CompanyStatus;
  name?:string;
  brief?:string;
  bod?:string;
  sector?:Sector;
}
