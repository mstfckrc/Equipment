"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSession } from "@/providers/session-provider";
import { adminService } from "@/services/admin/admin-service";
import type {
  AccountEnabledUpdateRequest,
  AdminAccount,
  AdminStoreStatusRequest,
  ProductModerationRequest,
  StoreRegistrationReviewRequest,
} from "@/types/admin";

const adminKey = (accountId?: string) => ["admin", accountId] as const;

export function useAdminRegistrations(loadList = true) {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const key = [...adminKey(identity?.id), "registrations"] as const;
  const registrationsQuery = useQuery({
    queryKey: key,
    queryFn: () => withAccessToken((token) => adminService.listRegistrations(token)),
    enabled: identity?.role === "PLATFORM_ADMIN" && loadList,
  });
  const reviewRegistration = useMutation({
    mutationFn: ({ id, request }: { id: string; request: StoreRegistrationReviewRequest }) =>
      withAccessToken((token) => adminService.reviewRegistration(id, request, token)),
    onSuccess: (registration) => {
      queryClient.setQueryData([...adminKey(identity?.id), "registration", registration.id], registration);
      return queryClient.invalidateQueries({ queryKey: key });
    },
  });
  return { registrationsQuery, reviewRegistration };
}

export function useAdminRegistration(registrationId: string | null) {
  const { identity, withAccessToken } = useSession();
  return useQuery({
    queryKey: [...adminKey(identity?.id), "registration", registrationId],
    queryFn: () => withAccessToken((token) => adminService.getRegistration(registrationId!, token)),
    enabled: identity?.role === "PLATFORM_ADMIN" && Boolean(registrationId),
  });
}

export function useAdminAccounts() {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const key = [...adminKey(identity?.id), "accounts"] as const;
  const accountsQuery = useQuery({
    queryKey: key,
    queryFn: () => withAccessToken((token) => adminService.listAccounts(token)),
    enabled: identity?.role === "PLATFORM_ADMIN",
  });
  const updateAccount = useMutation({
    mutationFn: ({ id, request }: { id: string; request: AccountEnabledUpdateRequest }) =>
      withAccessToken((token) => adminService.updateAccount(id, request, token)),
    onSuccess: (account) => {
      queryClient.setQueryData<AdminAccount[]>(key, (accounts) =>
        accounts?.map((item) => item.id === account.id ? account : item));
      return queryClient.invalidateQueries({ queryKey: key });
    },
  });
  return { accountsQuery, updateAccount };
}

export function useAdminStores(loadList = true) {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const key = [...adminKey(identity?.id), "stores"] as const;
  const storesQuery = useQuery({
    queryKey: key,
    queryFn: () => withAccessToken((token) => adminService.listStores(token)),
    enabled: identity?.role === "PLATFORM_ADMIN" && loadList,
  });
  const updateStatus = useMutation({
    mutationFn: ({ id, request }: { id: string; request: AdminStoreStatusRequest }) =>
      withAccessToken((token) => adminService.updateStoreStatus(id, request, token)),
    onSuccess: (store) => {
      queryClient.setQueryData([...adminKey(identity?.id), "store", store.storeId], store);
      return queryClient.invalidateQueries({ queryKey: key });
    },
  });
  return { storesQuery, updateStatus };
}

export function useAdminStore(storeId: string | null) {
  const { identity, withAccessToken } = useSession();
  return useQuery({
    queryKey: [...adminKey(identity?.id), "store", storeId],
    queryFn: () => withAccessToken((token) => adminService.getStore(storeId!, token)),
    enabled: identity?.role === "PLATFORM_ADMIN" && Boolean(storeId),
  });
}

export function useAdminProducts(loadList = true) {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const key = [...adminKey(identity?.id), "products"] as const;
  const productsQuery = useQuery({
    queryKey: key,
    queryFn: () => withAccessToken((token) => adminService.listProducts(token)),
    enabled: identity?.role === "PLATFORM_ADMIN" && loadList,
  });
  const moderateProduct = useMutation({
    mutationFn: ({ id, request }: { id: string; request: ProductModerationRequest }) =>
      withAccessToken((token) => adminService.moderateProduct(id, request, token)),
    onSuccess: (product) => {
      queryClient.setQueryData([...adminKey(identity?.id), "product", product.productId], product);
      return queryClient.invalidateQueries({ queryKey: key });
    },
  });
  return { productsQuery, moderateProduct };
}

export function useAdminProduct(productId: string | null) {
  const { identity, withAccessToken } = useSession();
  return useQuery({
    queryKey: [...adminKey(identity?.id), "product", productId],
    queryFn: () => withAccessToken((token) => adminService.getProduct(productId!, token)),
    enabled: identity?.role === "PLATFORM_ADMIN" && Boolean(productId),
  });
}
