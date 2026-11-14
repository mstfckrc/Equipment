"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ApiError } from "@/lib/http/api-client";
import { useSession } from "@/providers/session-provider";
import { rentalService } from "@/services/rental/rental-service";
import { storeInventoryQueryKey } from "@/hooks/catalog/use-store-products";
import type { EquipmentUnitStatus } from "@/types/rental";

export function useListingInventory(listingId: string) {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const inventoryKey = ["listing-inventory", identity?.id, listingId];

  const inventoryQuery = useQuery({
    queryKey: inventoryKey,
    queryFn: () => withAccessToken((token) => rentalService.getListingInventory(listingId, token))
      .catch((error: unknown) => {
        if (error instanceof ApiError && error.status === 404) return null;
        throw error;
      }),
    enabled: identity?.role === "STORE_OWNER" && Boolean(listingId),
  });

  const refreshInventory = () => Promise.all([
    queryClient.invalidateQueries({ queryKey: inventoryKey }),
    queryClient.invalidateQueries({ queryKey: storeInventoryQueryKey(identity?.id) }),
    queryClient.invalidateQueries({ queryKey: ["rental-availability"] }),
  ]);

  const setTotalUnits = useMutation({
    mutationFn: (totalUnits: number) => withAccessToken(
      (token) => rentalService.setListingInventory(listingId, totalUnits, token),
    ),
    onSuccess: refreshInventory,
  });

  const updateUnitStatus = useMutation({
    mutationFn: ({ unitId, status }: { unitId: string; status: EquipmentUnitStatus }) =>
      withAccessToken((token) => rentalService.updateUnitStatus(unitId, status, token)),
    onSuccess: refreshInventory,
  });

  return { inventoryQuery, setTotalUnits, updateUnitStatus };
}
