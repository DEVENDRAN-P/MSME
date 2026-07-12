import api from './api';
import type { ApproveLoanRequest, LoanResponse, ForecastResponse } from '../types/loan';

export const approveLoan = async (payload: ApproveLoanRequest): Promise<LoanResponse> => {
  const response = await api.post<LoanResponse>('/loans/approve', payload);
  return response.data;
};

export const getMyLoans = async (): Promise<LoanResponse[]> => {
  const response = await api.get<LoanResponse[]>('/loans/my-loans');
  return response.data;
};

export const getBusinessLoans = async (businessId: string): Promise<LoanResponse[]> => {
  const response = await api.get<LoanResponse[]>(`/loans/business/${businessId}`);
  return response.data;
};

export const simulateForecast = async (amount: number, interestRate: number, tenureMonths: number): Promise<ForecastResponse[]> => {
  const response = await api.post<ForecastResponse[]>('/loans/simulate-forecast', {
    amount,
    interestRate,
    tenureMonths
  });
  return response.data;
};

export const simulateBusinessForecast = async (businessId: string, amount: number, interestRate: number, tenureMonths: number): Promise<ForecastResponse[]> => {
  const response = await api.post<ForecastResponse[]>(`/loans/simulate-forecast/${businessId}`, {
    amount,
    interestRate,
    tenureMonths
  });
  return response.data;
};
