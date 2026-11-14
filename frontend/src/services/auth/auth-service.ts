import { apiRequest } from "@/lib/http/api-client";
import type {
  AccountProfile,
  AccountProfileUpdateRequest,
  CsrfTokenResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from "@/types/auth";

let csrfRequest: Promise<CsrfTokenResponse> | null = null;

async function getCsrfToken() {
  if (!csrfRequest) {
    csrfRequest = apiRequest<CsrfTokenResponse>("/auth/csrf")
      .finally(() => {
        csrfRequest = null;
      });
  }

  return csrfRequest;
}

async function csrfHeaders() {
  const token = await getCsrfToken();
  return { [token.headerName]: token.token };
}

export const authService = {
  getCsrfToken,

  getOwnProfile(accessToken: string) {
    return apiRequest<AccountProfile>("/auth/me", {}, accessToken);
  },

  async updateOwnProfile(request: AccountProfileUpdateRequest, accessToken: string) {
    return apiRequest<AccountProfile>("/auth/me", {
      method: "PUT",
      headers: await csrfHeaders(),
      body: JSON.stringify(request),
    }, accessToken);
  },

  async register(request: RegisterRequest) {
    return apiRequest<RegisterResponse>("/auth/register", {
      method: "POST",
      headers: await csrfHeaders(),
      body: JSON.stringify(request),
    });
  },

  async login(request: LoginRequest) {
    return apiRequest<LoginResponse>("/auth/login", {
      method: "POST",
      headers: await csrfHeaders(),
      body: JSON.stringify(request),
    });
  },

  async refresh() {
    return apiRequest<LoginResponse>("/auth/refresh", {
      method: "POST",
      headers: await csrfHeaders(),
    });
  },

  async logout() {
    return apiRequest<void>("/auth/logout", {
      method: "POST",
      headers: await csrfHeaders(),
    });
  },
};
