"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSession } from "@/providers/session-provider";
import { storeService } from "@/services/store/store-service";
import type { StoreProfileUpdateRequest } from "@/types/store";

export function useStoreProfile() {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const queryKey = ["store-profile", identity?.id];

  const profileQuery = useQuery({
    queryKey,
    queryFn: () => withAccessToken((token) => storeService.getOwnProfile(token)),
    enabled: identity?.role === "STORE_OWNER",
  });

  const updateProfile = useMutation({
    mutationFn: (request: StoreProfileUpdateRequest) =>
      withAccessToken((token) => storeService.updateOwnProfile(request, token)),
    onSuccess: (profile) => {
      queryClient.setQueryData(queryKey, profile);
      return Promise.all([
        queryClient.invalidateQueries({ queryKey: ["public-stores"] }),
        queryClient.invalidateQueries({ queryKey: ["public-store", profile.storeId] }),
        queryClient.invalidateQueries({ queryKey: ["public-store-summaries"] }),
      ]);
    },
  });

  return { profileQuery, updateProfile };
}
