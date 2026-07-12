export interface ConsentResponse {
  id: string;
  businessId: string;
  businessName: string;
  requestedBy: string;
  requestedByName: string;
  consentType: 'GST' | 'UPI' | 'AA' | 'ALL';
  validUntil: string;
  status: 'PENDING' | 'APPROVED' | 'DENIED' | 'REVOKED' | 'EXPIRED';
  createdAt: string;
}

export interface ConsentRequest {
  businessId: string;
  consentType: 'GST' | 'UPI' | 'AA' | 'ALL';
}
