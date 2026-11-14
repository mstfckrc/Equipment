import { apiRequest } from "@/lib/http/api-client";
import type { ProductListingRequest, ProductStatus, PublicProduct, StoreProduct } from "@/types/catalog";

export const catalogService = {
  listStoreProducts(storeId: string) {
    return apiRequest<PublicProduct[]>(
      `/api/catalog/stores/${encodeURIComponent(storeId)}/products`,
    );
  },

  getStoreProduct(storeId: string, productId: string) {
    return apiRequest<PublicProduct>(
      `/api/catalog/stores/${encodeURIComponent(storeId)}/products/${encodeURIComponent(productId)}`,
    );
  },

  listOwnProducts(accessToken: string) {
    return apiRequest<StoreProduct[]>("/api/catalog/stores/me/products", {}, accessToken);
  },

  getOwnProduct(productId: string, accessToken: string) {
    return apiRequest<StoreProduct>(
      `/api/catalog/stores/me/products/${encodeURIComponent(productId)}`,
      {},
      accessToken,
    );
  },

  createOwnProduct(request: ProductListingRequest, accessToken: string) {
    return apiRequest<StoreProduct>("/api/catalog/stores/me/products", {
      method: "POST",
      body: JSON.stringify(request),
    }, accessToken);
  },

  updateOwnProduct(productId: string, request: ProductListingRequest, accessToken: string) {
    return apiRequest<StoreProduct>(
      `/api/catalog/stores/me/products/${encodeURIComponent(productId)}`,
      { method: "PUT", body: JSON.stringify(request) },
      accessToken,
    );
  },

  updateOwnProductStatus(productId: string, status: ProductStatus, accessToken: string) {
    return apiRequest<StoreProduct>(
      `/api/catalog/stores/me/products/${encodeURIComponent(productId)}`,
      { method: "PATCH", body: JSON.stringify({ status }) },
      accessToken,
    );
  },
};
