import { Button } from "@/components/ui/button";
import { StoreInspectionForm } from "@/components/store-rentals/store-inspection-form";
import { conditionLabels, pickupActions, reservationStatusLabels, type InspectionDraft } from "@/components/store-rentals/reservation-helpers";
import { formatDate } from "@/lib/utils/rental-dates";
import type { ReservationItem, ReturnUnitCondition, StoreReservationItemStatus } from "@/types/rental";

export function StoreReservationRow({ item, now, busy, error, drafts, onDraft, onAction, onReview }: {
  item: ReservationItem; now: number; busy: boolean; error?: string; drafts: Record<string, InspectionDraft>;
  onDraft: (unitId: string, changes: Partial<InspectionDraft>) => void;
  onAction: (status: StoreReservationItemStatus, triggerId: string) => void; onReview: (triggerId: string) => void;
}) {
  const timing = pickupActions(item, now);
  const closed = item.status === "CANCELLED" || item.status === "RETURNED";
  const style = closed ? "status-pill-neutral" : item.status === "RETURN_RECEIVED" || item.status === "RETURN_REQUESTED" ? "status-pill-warning" : "status-pill-active";
  return <li className="min-w-0 space-y-5 p-5 sm:p-6" data-reservation-item={item.itemId}>
    <div className="flex flex-wrap items-start justify-between gap-3"><div className="min-w-0 space-y-2"><p className="text-xs text-[var(--muted)]">{item.quantity} adet ekipman</p>
      <h3 className="break-words text-lg font-semibold">{item.listingName}</h3></div><span className={`rounded-full px-3 py-1 text-xs font-semibold ${style}`}>{reservationStatusLabels[item.status]}</span></div>
    <dl className="grid gap-4 rounded-xl bg-[var(--page)] p-4 text-sm sm:grid-cols-3">
      <div><dt className="text-xs text-[var(--muted)]">Teslim alma</dt><dd className="mt-1 font-medium">{formatDate(item.pickupDate)}<span className="block text-xs font-normal text-[var(--muted)]">Saat {item.pickupTime.slice(0, 5)}</span></dd></div>
      <div><dt className="text-xs text-[var(--muted)]">Geri teslim</dt><dd className="mt-1 font-medium">{formatDate(item.returnDate)}<span className="block text-xs font-normal text-[var(--muted)]">Saat {item.returnTime.slice(0, 5)}</span></dd></div>
      <div><dt className="text-xs text-[var(--muted)]">Rezervasyon tutarı</dt><dd className="mt-1 font-semibold">{new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(item.totalAmount)}</dd></div>
    </dl>
    {item.pickupMissed || item.overdue || item.returnedLate ? <div className="notice-warning space-y-1 rounded-lg p-3 text-sm">
      {item.pickupMissed ? <p>Teslim alma zamanı geçti; rezervasyonun durumunu kontrol et.</p> : null}
      {item.overdue ? <p>Geri teslim zamanı geçti; cihazlar henüz mağazada teslim alınmadı.</p> : null}
      {item.returnedLate ? <p>Cihazlar planlanan geri teslim zamanından sonra alındı.</p> : null}</div> : null}
    {item.assignedUnits.length ? <details className="rental-unit-details rounded-xl border border-[var(--border)] p-4">
      <summary className="cursor-pointer text-sm font-semibold">{item.status === "RETURN_RECEIVED" ? "Cihaz kontrolünü aç" : `Atanan cihazlar (${item.assignedUnits.length})`}</summary>
      <div className="mt-4 border-t border-[var(--border)] pt-4">
        {item.status === "RETURN_RECEIVED" ? <StoreInspectionForm item={item} drafts={drafts} busy={busy} onDraft={onDraft} onReview={onReview} /> :
          <ul className="space-y-3">{item.assignedUnits.map((unit) => <li key={unit.unitId} className="space-y-1 text-sm"><p className="break-all font-mono font-semibold">{unit.unitCode}</p>
            {unit.returnCondition && unit.returnCondition in conditionLabels ? <p>{conditionLabels[unit.returnCondition as ReturnUnitCondition]}</p> : null}
            {unit.damageNote ? <p className="break-words text-[var(--muted)]">Kontrol notu: {unit.damageNote}</p> : null}
            {unit.manualChargeAmount != null ? <p className="text-[var(--muted)]">Kaydedilen ek tutar: {new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(unit.manualChargeAmount)}</p> : null}
          </li>)}</ul>}
      </div>
    </details> : item.status === "CONFIRMED" ? <p className="text-xs text-[var(--muted)]">Cihaz kodları teslim kaydı sırasında otomatik atanır.</p> : null}
    {error ? <p role="alert" className="notice-danger rounded-lg p-3 text-sm">{error}</p> : null}
    {item.status === "CONFIRMED" ? <div className="space-y-3 border-t border-[var(--border)] pt-4"><div className="flex flex-wrap gap-2">
      <Button id={`handover-${item.itemId}`} disabled={busy || !timing.canHandover} onClick={() => onAction("PICKED_UP", `handover-${item.itemId}`)}>Teslimi kaydet</Button>
      <Button id={`cancel-${item.itemId}`} variant="ghost" disabled={busy || !timing.canCancel} className="text-[var(--danger)]" onClick={() => onAction("CANCELLED", `cancel-${item.itemId}`)}>Rezervasyonu iptal et</Button>
    </div><p className="text-xs leading-5 text-[var(--muted)]">{timing.explanation}</p></div> : null}
    {item.status === "PICKED_UP" || item.status === "RETURN_REQUESTED" ? <div className="border-t border-[var(--border)] pt-4"><Button id={`receive-${item.itemId}`} variant="secondary" disabled={busy} onClick={() => onAction("RETURN_RECEIVED", `receive-${item.itemId}`)}>Geri teslimi kaydet</Button></div> : null}
  </li>;
}
