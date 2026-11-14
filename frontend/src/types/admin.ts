export type AccountStatus = "ACTIVE" | "PENDING" | "REJECTED";
export type AccountRole = "CUSTOMER" | "STORE_OWNER";
export type AdminStoreStatus = "ACTIVE" | "SUSPENDED";
export type AdminProductStatus = "DRAFT" | "PUBLISHED" | "SUSPENDED" | "ARCHIVED";
export type StoreRegistrationStatus = "PENDING" | "APPROVED" | "REJECTED";
export type StoreRegistrationDecision = "APPROVED" | "REJECTED";

export interface StoreRegistrationSummary {
  id: string;
  storeId: string;
  email: string;
  storeName: string;
  city: string;
  district: string;
  status: StoreRegistrationStatus;
  submittedAt: string;
  reviewedAt: string | null;
}

export interface StoreRegistration extends StoreRegistrationSummary {
  accountId: string;
  phone: string;
  addressLine: string;
  reviewedBy: string | null;
  rejectionReason: string | null;
}

export interface AdminAccount {
  id: string;
  email: string;
  role: AccountRole;
  accountStatus: AccountStatus;
  enabled: boolean;
  firstName: string | null;
  lastName: string | null;
}

export interface AdminStoreSummary {
  storeId: string;
  accountId: string;
  storeName: string;
  phone: string;
  city: string;
  district: string;
  status: AdminStoreStatus;
}

export interface AdminStore extends AdminStoreSummary {
  addressLine: string;
}

export interface AdminProduct {
  productId: string;
  storeId: string;
  name: string;
  category: string;
  description: string;
  imageUrl: string | null;
  dailyPrice: number;
  status: AdminProductStatus;
  moderationReason: string | null;
  moderatedBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface StoreRegistrationReviewRequest {
  status: StoreRegistrationDecision;
  reason?: string;
}

export interface AccountEnabledUpdateRequest {
  enabled: boolean;
}

export interface AdminStoreStatusRequest {
  status: AdminStoreStatus;
}

export interface ProductModerationRequest {
  status: "DRAFT" | "SUSPENDED";
  reason?: string;
}
