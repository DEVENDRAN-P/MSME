export interface LoanResponse {
  id: string;
  businessId: string;
  businessName: string;
  amount: number;
  interestRate: number;
  tenureMonths: number;
  status: 'APPROVED' | 'DISBURSED' | 'REPAID';
  disbursedAt: string;
}

export interface ApproveLoanRequest {
  businessId: string;
  amount: number;
  interestRate: number;
  tenureMonths: number;
}

export interface ForecastResponse {
  month: string;
  projectedSales: number;
  emi: number;
  netSurplus: number;
}
