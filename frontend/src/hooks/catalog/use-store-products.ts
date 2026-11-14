"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSession } from "@/providers/session-provider";
import { catalogService } from "@/services/catalog/catalog-service";
import { rentalService } from "@/services/rental/rental-service";
import type { ProductListingRequest, ProductStatus } from "@/types/catalog";

export const storeProductsQueryKey = (accountId?: string) => ["store-products", accountId];
export const storeInventoryQueryKey = (accountId?: string) => ["store-inventory", accountId];

export function useStoreProducts() {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const productsKey = storeProductsQueryKey(identity?.id);
  const productsQuery = useQuery({
    queryKey: productsKey,
    queryFn: () => withAccessToken((token) => catalogService.listOwnProducts(token)),
    enabled: identity?.role === "STORE_OWNER",
  });
  const inventoryQuery = useQuery({
    queryKey: storeInventoryQueryKey(identity?.id),
    queryFn: () => withAccessToken((token) => rentalService.getOwnInventory(token)),
    enabled: identity?.role === "STORE_OWNER",
  });

  const updateStatus = useMutation({
    mutationFn: ({ productId, status }: { productId: string; status: ProductStatus }) =>
      withAccessToken((token) => catalogService.updateOwnProductStatus(productId, status, token)),
    onSuccess: (product) => {
      queryClient.setQueryData(["store-product", identity?.id, product.productId], product);
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: productsKey }),
        queryClient.invalidateQueries({ queryKey: ["public-store-products", product.storeId] }),
        queryClient.invalidateQueries({ queryKey: ["public-product", product.storeId, product.productId] }),
      ]);
    },
  });

  return { productsQuery, inventoryQuery, updateStatus };
}

export function useCreateStoreProduct() {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (request: ProductListingRequest) =>
      withAccessToken((token) => catalogService.createOwnProduct(request, token)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: storeProductsQueryKey(identity?.id) }),
  });
}

export function useStoreProduct(productId: string) {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const query = useQuery({
    queryKey: ["store-product", identity?.id, productId],
    queryFn: () => withAccessToken((token) => catalogService.getOwnProduct(productId, token)),
    enabled: identity?.role === "STORE_OWNER" && Boolean(productId),
  });
  const updateProduct = useMutation({
    mutationFn: (request: ProductListingRequest) =>
      withAccessToken((token) => catalogService.updateOwnProduct(productId, request, token)),
    onSuccess: (product) => {
      queryClient.setQueryData(["store-product", identity?.id, productId], product);
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: storeProductsQueryKey(identity?.id) }),
        queryClient.invalidateQueries({ queryKey: ["public-store-products", product.storeId] }),
        queryClient.invalidateQueries({ queryKey: ["public-product", product.storeId, product.productId] }),
        queryClient.invalidateQueries({ queryKey: ["rental-availability"] }),
      ]);
    },
  });
  return { query, updateProduct };
}
