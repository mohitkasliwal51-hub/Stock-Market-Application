import { Sector } from "./sector-model";

export interface Company{
  id:number;
  companyName:string;
  turnover:number;
  ceo:string;
  boardOfDirectors:string;
  sectorId:number;
  briefWriteup:string;
  name?:string;
  brief?:string;
  bod?:string;
  sector?:Sector;
}
