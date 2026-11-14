import { apiRequest } from "@/lib/http/api-client";
import type {
  PublicStore,
  PublicStoreSummary,
  StoreProfile,
  StoreProfileUpdateRequest,
} from "@/types/store";

export const storeService = {
  listPublicStores() {
    return apiRequest<PublicStoreSummary[]>("/api/stores");
  },

  getPublicStore(storeId: string) {
    return apiRequest<PublicStore>(`/api/stores/${encodeURIComponent(storeId)}`);
  },

  getOwnProfile(accessToken: string) {
    return apiRequest<StoreProfile>("/api/stores/me", {}, accessToken);
  },

  updateOwnProfile(request: StoreProfileUpdateRequest, accessToken: string) {
    return apiRequest<StoreProfile>("/api/stores/me", {
      method: "PUT",
      body: JSON.stringify(request),
    }, accessToken);
  },
};
