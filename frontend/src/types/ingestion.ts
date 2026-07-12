export interface GstRecord {
  month: string;
  turnover: number;
  taxPaid: number;
  status: string;
}

export interface UpiRecord {
  month: string;
  creditVolume: number;
  creditCount: number;
  debitVolume: number;
  debitCount: number;
}

export interface BankRecord {
  month: string;
  avgBalance: number;
  inflows: number;
  outflows: number;
}

export interface EpfoRecord {
  month: string;
  employeeCount: number;
  contribution: number;
}

export interface UtilityRecord {
  type: 'ELECTRICITY' | 'TELECOM' | 'WATER';
  month: string;
  amount: number;
  status: string;
}

export interface EcommRecord {
  platform: 'AMAZON' | 'FLIPKART' | 'ONDC';
  month: string;
  sales: number;
  orders: number;
}

export interface IngestSummary {
  businessId: string;
  gstSynced: boolean;
  upiSynced: boolean;
  aaSynced: boolean;
  epfoSynced: boolean;
  utilitySynced: boolean;
  ecommSynced: boolean;
  
  gstRecords: GstRecord[];
  upiRecords: UpiRecord[];
  bankRecords: BankRecord[];
  epfoRecords: EpfoRecord[];
  utilityRecords: UtilityRecord[];
  ecommRecords: EcommRecord[];
}
