export type AccountRole = "CUSTOMER" | "STORE_OWNER" | "PLATFORM_ADMIN";
export type RegistrationType = "CUSTOMER" | "STORE_OWNER";

export interface CsrfTokenResponse {
  token: string;
  headerName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
}

export interface RegisterRequest {
  accountType: RegistrationType;
  email: string;
  password: string;
  firstName?: string;
  lastName?: string;
  storeName?: string;
  phone?: string;
  city?: string;
  district?: string;
  addressLine?: string;
}

export interface RegisterResponse {
  id: string;
  registrationId: string | null;
  storeId: string | null;
  email: string;
  accountType: RegistrationType;
  status: "ACTIVE" | "PENDING" | "REJECTED";
  message: string;
}

export interface AuthIdentity {
  id: string;
  role: AccountRole;
}

export interface AccountProfile {
  email: string;
  firstName: string | null;
  lastName: string | null;
}

export interface AccountProfileUpdateRequest {
  firstName: string;
  lastName: string;
}
