"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useSession } from "@/providers/session-provider";
import { rentalService } from "@/services/rental/rental-service";
import { storeInventoryQueryKey } from "@/hooks/catalog/use-store-products";
import type { Reservation, StoreReservationItemUpdateRequest } from "@/types/rental";

export function useStoreReservations() {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const queryKey = ["store-reservations", identity?.id];

  const reservationsQuery = useQuery({
    queryKey,
    queryFn: () => withAccessToken((token) => rentalService.getStoreReservations(token)),
    enabled: identity?.role === "STORE_OWNER",
  });

  const updateReservationItem = useMutation({
    mutationFn: ({ itemId, request }: { itemId: string; request: StoreReservationItemUpdateRequest }) =>
      withAccessToken((token) => rentalService.updateStoreReservationItem(itemId, request, token)),
    onSuccess: (updated) => {
      queryClient.setQueryData<Reservation[]>(queryKey, (current) => current?.map((reservation) => ({
        ...reservation, items: reservation.items.map((item) => item.itemId === updated.itemId ? updated : item),
      })));
      return Promise.all([
        queryClient.invalidateQueries({ queryKey }),
        queryClient.invalidateQueries({ queryKey: storeInventoryQueryKey(identity?.id) }),
        queryClient.invalidateQueries({ queryKey: ["listing-inventory", identity?.id, updated.listingId] }),
        queryClient.invalidateQueries({ queryKey: ["rental-availability"] }),
      ]);
    },
  });

  return { reservationsQuery, updateReservationItem };
}
