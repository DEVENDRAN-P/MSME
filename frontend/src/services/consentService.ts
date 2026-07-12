import api from './api';
import type { ConsentRequest, ConsentResponse } from '../types/consent';

export const requestConsent = async (payload: ConsentRequest): Promise<ConsentResponse> => {
  const response = await api.post<ConsentResponse>('/consents/request', payload);
  return response.data;
};

export const getMyPendingConsents = async (): Promise<ConsentResponse[]> => {
  const response = await api.get<ConsentResponse[]>('/consents/my-pending');
  return response.data;
};

export const getMyAllConsents = async (): Promise<ConsentResponse[]> => {
  const response = await api.get<ConsentResponse[]>('/consents/my-all');
  return response.data;
};

export const updateConsentStatus = async (id: string, status: string): Promise<ConsentResponse> => {
  const response = await api.put<ConsentResponse>(`/consents/${id}/status?status=${status}`);
  return response.data;
};

export const getLenderRequests = async (): Promise<ConsentResponse[]> => {
  const response = await api.get<ConsentResponse[]>('/consents/lender-requests');
  return response.data;
};

export const checkConsent = async (businessId: string): Promise<boolean> => {
  const response = await api.get<boolean>(`/consents/check/${businessId}`);
  return response.data;
};
