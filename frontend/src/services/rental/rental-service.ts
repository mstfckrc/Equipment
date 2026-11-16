import { apiRequest } from "@/lib/http/api-client";
import type {
  RentalConfirmation,
  AvailabilityCalendar,
  Reservation,
  ReservationCheckoutRequest,
  ReservationCheckoutResponse,
  ReservationItem,
  ReservationItemStatus,
  StoreReservationItemUpdateRequest,
  EquipmentUnitStatus,
  EquipmentUnit,
  RentalInventory,
  RentalInventorySummary,
} from "@/types/rental";

export const rentalService = {
  getConfirmation(checkoutId: string, accessToken: string) {
    return apiRequest<RentalConfirmation>(
      `/api/rentals/checkouts/${encodeURIComponent(checkoutId)}/confirmation`, {}, accessToken,
    );
  },

  getOwnInventory(accessToken: string) {
    return apiRequest<RentalInventorySummary[]>("/api/rentals/stores/me/inventory", {}, accessToken);
  },

  getListingInventory(listingId: string, accessToken: string) {
    return apiRequest<RentalInventory>(
      `/api/rentals/stores/me/listings/${encodeURIComponent(listingId)}/inventory`,
      {},
      accessToken,
    );
  },

  setListingInventory(listingId: string, totalUnits: number, accessToken: string) {
    return apiRequest<RentalInventory>(
      `/api/rentals/stores/me/listings/${encodeURIComponent(listingId)}/inventory`,
      { method: "PUT", body: JSON.stringify({ totalUnits }) },
      accessToken,
    );
  },

  updateUnitStatus(unitId: string, status: EquipmentUnitStatus, accessToken: string) {
    return apiRequest<EquipmentUnit>(`/api/rentals/stores/me/inventory/units/${encodeURIComponent(unitId)}`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    }, accessToken);
  },

  getAvailability(
    storeId: string,
    listingId: string,
    fromDate: string,
    toDateExclusive: string,
    accessToken: string,
  ) {
    const query = new URLSearchParams({ fromDate, toDateExclusive });
    return apiRequest<AvailabilityCalendar>(
      `/api/rentals/stores/${encodeURIComponent(storeId)}/listings/${encodeURIComponent(listingId)}/availability?${query}`,
      {},
      accessToken,
    );
  },

  checkout(request: ReservationCheckoutRequest, idempotencyKey: string, accessToken: string) {
    return apiRequest<ReservationCheckoutResponse>("/api/rentals/reservations", {
      method: "POST",
      headers: { "Idempotency-Key": idempotencyKey },
      body: JSON.stringify(request),
    }, accessToken);
  },

  getMyReservations(accessToken: string) {
    return apiRequest<Reservation[]>("/api/rentals/reservations/me", {}, accessToken);
  },

  getStoreReservations(accessToken: string) {
    return apiRequest<Reservation[]>("/api/rentals/stores/me/reservations", {}, accessToken);
  },

  updateStoreReservationItem(itemId: string, request: StoreReservationItemUpdateRequest, accessToken: string) {
    return apiRequest<ReservationItem>(`/api/rentals/stores/me/reservations/items/${encodeURIComponent(itemId)}`, {
      method: "PATCH",
      body: JSON.stringify(request),
    }, accessToken);
  },

  updateMyReservationItem(itemId: string, status: Extract<ReservationItemStatus, "CANCELLED" | "RETURN_REQUESTED">, accessToken: string) {
    return apiRequest<ReservationItem>(`/api/rentals/reservations/me/items/${encodeURIComponent(itemId)}`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    }, accessToken);
  },
};
