export interface PublicStoreSummary {
  storeId: string;
  storeName: string;
  city: string;
  district: string;
}

export interface PublicStore {
  storeId: string;
  storeName: string;
  phone: string;
  city: string;
  district: string;
  addressLine: string;
}

export type StoreStatus = "ACTIVE" | "SUSPENDED";

export interface StoreProfile extends PublicStore {
  accountId: string;
  status: StoreStatus;
}

export interface StoreProfileUpdateRequest {
  storeName: string;
  phone: string;
  city: string;
  district: string;
  addressLine: string;
}
