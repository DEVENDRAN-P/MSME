import api from './api';
import type { CreditFeatures } from '../types/features';

export const getMyFeatures = async (): Promise<CreditFeatures> => {
  const response = await api.get<CreditFeatures>('/features/my-features');
  return response.data;
};

export const getBusinessFeatures = async (businessId: string): Promise<CreditFeatures> => {
  const response = await api.get<CreditFeatures>(`/features/${businessId}`);
  return response.data;
};
