import api from './api';
import type { Business, RegisterBusinessInput } from '../types/business';

export const getMyBusiness = async (): Promise<Business> => {
  const response = await api.get<Business>('/business/my-business');
  return response.data;
};

export const registerBusiness = async (data: RegisterBusinessInput): Promise<Business> => {
  const response = await api.post<Business>('/business/register', data);
  return response.data;
};

export const getBusinessById = async (id: string): Promise<Business> => {
  const response = await api.get<Business>(`/business/${id}`);
  return response.data;
};
