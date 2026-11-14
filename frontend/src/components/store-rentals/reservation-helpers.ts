import { ApiError } from "@/lib/http/api-client";
import type { ReservationItem, ReservationItemStatus, ReturnUnitCondition, UnitReturnInspection } from "@/types/rental";

export const reservationStatusLabels: Record<ReservationItemStatus, string> = {
  CONFIRMED: "Teslim alma bekliyor", PICKED_UP: "Kirada", RETURN_REQUESTED: "Geri teslim talebi",
  RETURN_RECEIVED: "Kontrol bekliyor", RETURNED: "Tamamlandı", CANCELLED: "İptal edildi",
};
export const conditionLabels: Record<ReturnUnitCondition, string> = {
  READY: "Kullanıma hazır", MAINTENANCE: "Bakım gerekli", RETIRED: "Kullanımdan kaldır",
};
export type InspectionDraft = { condition: ReturnUnitCondition; damageNote: string; manualChargeAmount: string };
export const emptyInspectionDraft: InspectionDraft = { condition: "READY", damageNote: "", manualChargeAmount: "" };

export function pickupActions(item: ReservationItem, now: number) {
  const parts = new Intl.DateTimeFormat("en-CA", { timeZone: "Europe/Istanbul", year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", second: "2-digit", hourCycle: "h23" }).formatToParts(now);
  const values = Object.fromEntries(parts.map((part) => [part.type, part.value]));
  const localNow = `${values.year}-${values.month}-${values.day}T${values.hour}:${values.minute}:${values.second}`;
  const pickup = `${item.pickupDate}T${item.pickupTime.slice(0, 5)}:00`;
  const end = `${item.returnDate}T${item.returnTime.slice(0, 5)}:00`;
  return { canCancel: localNow < pickup, canHandover: localNow >= pickup && localNow < end,
    explanation: localNow < pickup ? "Teslim kaydı planlanan teslim alma saatinde açılır."
      : localNow >= end ? "Kiralama dönemi bittiği için teslim kaydı ve iptal yapılamaz." : "Teslim alma saati geçtiği için iptal yapılamaz." };
}

export function reservationError(error: unknown) {
  if (error instanceof ApiError) {
    if (error.status === 401 || error.status === 403) return "Bu işlem için mağaza sahibi erişimi gerekiyor.";
    if (error.status === 404) return "Rezervasyon satırı bulunamadı. Listeyi yenileyip tekrar kontrol et.";
    const messages: Record<string, string> = {
      PICKUP_NOT_STARTED: "Planlanan teslim alma saati henüz gelmedi.",
      PICKUP_WINDOW_CLOSED: "Kiralama dönemi sona erdi; teslim kaydı yapılamaz.",
      EQUIPMENT_UNAVAILABLE: "Bu kiralama için yeterli hazır cihaz atanamıyor. Envanteri kontrol et.",
      RESERVATION_ALREADY_STARTED: "Teslim alma saati geçtiği için rezervasyon iptal edilemiyor.",
      INVALID_RESERVATION_TRANSITION: "Satırın durumu bu işleme uygun değil. Listeyi yenileyip kontrol et.",
      EQUIPMENT_ASSIGNMENT_INCOMPLETE: "Atanmış cihaz bilgileri eksik. Listeyi yenileyip kontrol et.",
      EQUIPMENT_STATE_CHANGED: "Atanmış cihazlardan birinin durumu değişmiş. Listeyi yenileyip kontrol et.",
    };
    if (error.code && messages[error.code]) return messages[error.code];
    if (error.status === 409) return "Rezervasyon veya cihaz durumu değişmiş olabilir. Listeyi yenileyip kontrol et.";
    if (error.status === 400) return "Kontrol bilgileri kabul edilmedi. Her cihazın sonucunu ve tutarını kontrol et.";
  }
  return "İşlem tamamlanamadı. Bilgilerin korunuyor; yeniden deneyebilirsin.";
}

export function buildInspections(item: ReservationItem, drafts: Record<string, InspectionDraft>): UnitReturnInspection[] {
  if (!item.assignedUnits.length || item.assignedUnits.length !== item.quantity) throw new Error("Her kiralanan cihaz için atama bulunmalı. Listeyi yenileyip kontrol et.");
  return item.assignedUnits.map((unit) => {
    const draft = drafts[unit.unitId] ?? emptyInspectionDraft;
    const amount = draft.manualChargeAmount.trim().replace(",", ".");
    if (amount && !/^\d{1,10}(\.\d{1,2})?$/.test(amount)) throw new Error(`${unit.unitCode}: Ek tutarı en fazla iki ondalık basamakla, sıfır veya pozitif gir.`);
    return { unitId: unit.unitId, condition: draft.condition,
      ...(draft.damageNote.trim() ? { damageNote: draft.damageNote.trim() } : {}),
      ...(amount ? { manualChargeAmount: Number(amount) } : {}) };
  });
}
