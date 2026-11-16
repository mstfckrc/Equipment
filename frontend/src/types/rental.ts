export interface AvailabilityDay {
  date: string;
  readyUnits: number;
  reservedUnits: number;
  remainingUnits: number;
}

export type EquipmentUnitStatus =
  | "READY"
  | "RENTED"
  | "RETURN_PENDING_INSPECTION"
  | "MAINTENANCE"
  | "RETIRED";

export interface EquipmentUnit {
  unitId: string;
  unitCode: string;
  status: EquipmentUnitStatus;
  createdAt: string;
  updatedAt: string;
}

export interface RentalInventorySummary {
  listingId: string;
  totalUnits: number;
  readyUnits: number;
  maintenanceUnits: number;
  retiredUnits: number;
  rentedUnits: number;
  pendingInspectionUnits: number;
}

export interface RentalInventory extends RentalInventorySummary {
  units: EquipmentUnit[];
}

export interface AvailabilityCalendar {
  storeId: string;
  listingId: string;
  fromDate: string;
  toDateExclusive: string;
  currencyCode: string;
  listingName: string;
  dailyPrice: number;
  days: AvailabilityDay[];
}

export interface CheckoutItemRequest {
  storeId: string;
  listingId: string;
  quantity: number;
  pickupDate: string;
  returnDate: string;
  expectedDailyPrice: number;
}

export interface ReservationCheckoutRequest {
  items: CheckoutItemRequest[];
}

export type ReservationItemStatus =
  | "CONFIRMED"
  | "CANCELLED"
  | "PICKED_UP"
  | "RETURN_REQUESTED"
  | "RETURN_RECEIVED"
  | "RETURNED";

export interface ReservationItem {
  itemId: string;
  listingId: string;
  listingName: string;
  quantity: number;
  pickupDate: string;
  returnDate: string;
  pickupTime: string;
  returnTime: string;
  dailyPrice: number;
  totalAmount: number;
  currencyCode: string;
  status: ReservationItemStatus;
  pickupMissed: boolean;
  overdue: boolean;
  returnedLate: boolean;
  assignedUnits: Array<{
    unitId: string;
    unitCode: string;
    equipmentStatus: string;
    handedOverAt: string | null;
    receivedAt: string | null;
    inspectedAt: string | null;
    returnCondition: string | null;
    damageNote: string | null;
    manualChargeAmount: number | null;
    currencyCode: string | null;
  }>;
}

export type StoreReservationItemStatus = Extract<
  ReservationItemStatus,
  "CANCELLED" | "PICKED_UP" | "RETURN_RECEIVED" | "RETURNED"
>;

export type ReturnUnitCondition = Extract<EquipmentUnitStatus, "READY" | "MAINTENANCE" | "RETIRED">;

export interface UnitReturnInspection {
  unitId: string;
  condition: ReturnUnitCondition;
  damageNote?: string;
  manualChargeAmount?: number;
}

export interface StoreReservationItemUpdateRequest {
  status: StoreReservationItemStatus;
  unitInspections?: UnitReturnInspection[];
}

export interface Reservation {
  reservationId: string;
  checkoutId: string;
  storeId: string;
  totalAmount: number;
  currencyCode: string;
  createdAt: string;
  items: ReservationItem[];
  confirmationStatus: ConfirmationStatus;
}

export type ConfirmationStatus = "PENDING" | "READY" | "FAILED";

export interface ConfirmationContent {
  checkoutId: string;
  reservedAt: string;
  preparedAt: string;
  timeZone: string;
  currencyCode: string;
  totalAmount: number;
  stores: Array<{
    storeId: string;
    reservationId: string;
    storeName: string | null;
    totalAmount: number;
    items: Array<{
      itemId: string;
      listingId: string;
      listingName: string;
      quantity: number;
      pickupAt: string;
      returnAt: string;
      rentalDays: number;
      dailyPrice: number;
      totalAmount: number;
    }>;
  }>;
}

export interface RentalConfirmation {
  documentId: string | null;
  checkoutId: string;
  status: ConfirmationStatus;
  contentVersion: number;
  preparedAt: string | null;
  errorCode: string | null;
  message: string;
  content: ConfirmationContent | null;
}

export interface ReservationCheckoutResponse {
  checkoutId: string;
  totalAmount: number;
  currencyCode: string;
  createdAt: string;
  reservations: Reservation[];
}
