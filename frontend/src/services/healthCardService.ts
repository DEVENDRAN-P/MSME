import api from './api';
import type { CreditHealthCard } from '../types/healthCard';

export const getMyHealthCard = async (): Promise<CreditHealthCard> => {
  const response = await api.get<CreditHealthCard>('/health-card/my-card');
  return response.data;
};

export const getBusinessHealthCard = async (businessId: string): Promise<CreditHealthCard> => {
  const response = await api.get<CreditHealthCard>(`/health-card/${businessId}`);
  return response.data;
};
