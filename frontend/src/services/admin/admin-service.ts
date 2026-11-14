import { apiRequest } from "@/lib/http/api-client";
import type {
  AccountEnabledUpdateRequest,
  AdminAccount,
  AdminProduct,
  AdminStore,
  AdminStoreSummary,
  AdminStoreStatusRequest,
  ProductModerationRequest,
  StoreRegistration,
  StoreRegistrationReviewRequest,
  StoreRegistrationSummary,
} from "@/types/admin";

export const adminService = {
  listRegistrations(accessToken: string) {
    return apiRequest<StoreRegistrationSummary[]>("/api/admin/store-registrations", {}, accessToken);
  },

  getRegistration(registrationId: string, accessToken: string) {
    return apiRequest<StoreRegistration>(
      `/api/admin/store-registrations/${encodeURIComponent(registrationId)}`,
      {},
      accessToken,
    );
  },

  reviewRegistration(registrationId: string, request: StoreRegistrationReviewRequest, accessToken: string) {
    return apiRequest<StoreRegistration>(
      `/api/admin/store-registrations/${encodeURIComponent(registrationId)}`,
      { method: "PATCH", body: JSON.stringify(request) },
      accessToken,
    );
  },

  listAccounts(accessToken: string) {
    return apiRequest<AdminAccount[]>("/api/admin/accounts", {}, accessToken);
  },

  updateAccount(accountId: string, request: AccountEnabledUpdateRequest, accessToken: string) {
    return apiRequest<AdminAccount>(
      `/api/admin/accounts/${encodeURIComponent(accountId)}`,
      { method: "PATCH", body: JSON.stringify(request) },
      accessToken,
    );
  },

  listStores(accessToken: string) {
    return apiRequest<AdminStoreSummary[]>("/api/admin/stores", {}, accessToken);
  },

  getStore(storeId: string, accessToken: string) {
    return apiRequest<AdminStore>(`/api/admin/stores/${encodeURIComponent(storeId)}`, {}, accessToken);
  },

  updateStoreStatus(storeId: string, request: AdminStoreStatusRequest, accessToken: string) {
    return apiRequest<AdminStore>(
      `/api/admin/stores/${encodeURIComponent(storeId)}`,
      { method: "PATCH", body: JSON.stringify(request) },
      accessToken,
    );
  },

  listProducts(accessToken: string) {
    return apiRequest<AdminProduct[]>("/api/admin/catalog/products", {}, accessToken);
  },

  getProduct(productId: string, accessToken: string) {
    return apiRequest<AdminProduct>(
      `/api/admin/catalog/products/${encodeURIComponent(productId)}`,
      {},
      accessToken,
    );
  },

  moderateProduct(productId: string, request: ProductModerationRequest, accessToken: string) {
    return apiRequest<AdminProduct>(
      `/api/admin/catalog/products/${encodeURIComponent(productId)}`,
      { method: "PATCH", body: JSON.stringify(request) },
      accessToken,
    );
  },
};
