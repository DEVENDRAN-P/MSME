export interface Business {
  id: string;
  ownerId: string;
  legalName: string;
  tradeName?: string;
  gstin: string;
  pan: string;
  udyamNumber: string;
  incorporationDate: string;
  constitution: string;
  industrySector: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  pincode: string;
  createdAt: string;
  updatedAt: string;
}

export interface RegisterBusinessInput {
  legalName: string;
  tradeName?: string;
  gstin: string;
  pan: string;
  udyamNumber: string;
  incorporationDate: string;
  constitution: string;
  industrySector: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  pincode: string;
}
