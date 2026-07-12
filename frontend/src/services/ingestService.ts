import api from './api';
import type { IngestSummary } from '../types/ingestion';

export const getIngestSummary = async (): Promise<IngestSummary> => {
  const response = await api.get<IngestSummary>('/data-ingest/summary');
  return response.data;
};

export const syncAlternateData = async (streamType: string = 'ALL'): Promise<IngestSummary> => {
  const response = await api.post<IngestSummary>(`/data-ingest/sync?streamType=${streamType}`);
  return response.data;
};
