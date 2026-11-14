"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSession } from "@/providers/session-provider";
import { authService } from "@/services/auth/auth-service";
import type { AccountProfileUpdateRequest } from "@/types/auth";

export function useAccountProfile() {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const queryKey = ["account-profile", identity?.id];

  const profileQuery = useQuery({
    queryKey,
    queryFn: () => withAccessToken((token) => authService.getOwnProfile(token)),
    enabled: identity?.role === "CUSTOMER",
  });

  const updateProfile = useMutation({
    mutationFn: (request: AccountProfileUpdateRequest) =>
      withAccessToken((token) => authService.updateOwnProfile(request, token)),
    onSuccess: (profile) => queryClient.setQueryData(queryKey, profile),
  });

  return { profileQuery, updateProfile };
}
