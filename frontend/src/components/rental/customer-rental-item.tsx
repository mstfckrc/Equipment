import { CalendarDays, Check, ChevronDown } from "lucide-react";
import { Button } from "@/components/ui/button";
import { formatDate } from "@/lib/utils/rental-dates";
import type { ReservationItem, ReservationItemStatus } from "@/types/rental";

export type CustomerRentalAction = "CANCELLED" | "RETURN_REQUESTED";

const money = new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" });
const statusLabels: Record<ReservationItemStatus, string> = {
  CONFIRMED: "Onaylandı", CANCELLED: "İptal edildi", PICKED_UP: "Teslim alındı",
  RETURN_REQUESTED: "Geri teslim talebi gönderildi", RETURN_RECEIVED: "Mağaza geri teslim aldı", RETURNED: "Kontrol tamamlandı",
};
const steps: Array<{ status: ReservationItemStatus; label: string }> = [
  { status: "CONFIRMED", label: "Onaylandı" }, { status: "PICKED_UP", label: "Teslim alındı" },
  { status: "RETURN_REQUESTED", label: "Talep gönderildi" }, { status: "RETURN_RECEIVED", label: "Mağaza teslim aldı" },
  { status: "RETURNED", label: "Kontrol tamamlandı" },
];

function canCustomerCancel(pickupDate: string) {
  const guessedUtc = Date.parse(`${pickupDate}T12:00:00Z`);
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Europe/Istanbul", year: "numeric", month: "2-digit", day: "2-digit",
    hour: "2-digit", minute: "2-digit", second: "2-digit", hourCycle: "h23",
  }).formatToParts(new Date(guessedUtc));
  const values = Object.fromEntries(parts.map((part) => [part.type, Number(part.value)]));
  const zoneTimeAsUtc = Date.UTC(values.year, values.month - 1, values.day, values.hour, values.minute, values.second);
  const pickupInstant = guessedUtc - (zoneTimeAsUtc - guessedUtc);
  return pickupInstant - Date.now() >= 24 * 60 * 60 * 1000;
}

export function CustomerRentalItem({ item, busy, saving, error, onAction }: {
  item: ReservationItem; busy: boolean; saving: boolean; error?: string;
  onAction: (item: ReservationItem, status: CustomerRentalAction, triggerId: string) => void;
}) {
  const showCancel = item.status === "CONFIRMED" && canCustomerCancel(item.pickupDate);
  const currentStep = steps.findIndex((step) => step.status === item.status);
  const actionId = `rental-action-${item.itemId}`;
  const statusStyle = item.status === "CANCELLED" ? "status-pill-neutral"
    : item.status === "RETURN_RECEIVED" ? "status-pill-warning"
      : item.status === "CONFIRMED" || item.status === "RETURNED" ? "status-pill-success" : "status-pill-active";

  return <li className="customer-rental-item space-y-5" data-item-id={item.itemId}>
    <div className="flex flex-wrap items-start justify-between gap-3">
      <div className="min-w-0 space-y-2"><p className="text-xs font-medium text-[var(--muted)]">{item.quantity} adet ekipman</p>
        <h4 className="break-words text-base font-semibold">{item.listingName}</h4></div>
      <span className={`max-w-full rounded-full px-3 py-1 text-xs font-semibold ${statusStyle}`}>{statusLabels[item.status]}</span>
    </div>
    <dl className="grid gap-4 rounded-xl bg-[var(--page)] p-4 sm:grid-cols-3">
      <div className="space-y-1"><dt className="flex items-center gap-1.5 text-xs text-[var(--muted)]"><CalendarDays aria-hidden="true" className="size-3.5" />Teslim alma</dt>
        <dd className="text-sm font-medium">{formatDate(item.pickupDate)}<span className="block text-xs font-normal text-[var(--muted)]">Saat {item.pickupTime.slice(0, 5)}</span></dd></div>
      <div className="space-y-1"><dt className="text-xs text-[var(--muted)]">Geri teslim</dt>
        <dd className="text-sm font-medium">{formatDate(item.returnDate)}<span className="block text-xs font-normal text-[var(--muted)]">Saat {item.returnTime.slice(0, 5)}</span></dd></div>
      <div className="space-y-1"><dt className="text-xs text-[var(--muted)]">Rezervasyon tutarı</dt>
        <dd className="text-base font-semibold">{money.format(item.totalAmount)}</dd></div>
    </dl>
    {item.status !== "CANCELLED" ? <ol className="rental-progress" aria-label="Kiralama süreci">
      {steps.map((step, index) => <li key={step.status} aria-current={index === currentStep ? "step" : undefined}
        className={index <= currentStep ? "rental-progress-reached" : ""}>
        <span className="rental-progress-dot" aria-hidden="true">{index < currentStep || item.status === "RETURNED" ? <Check className="size-3" /> : index + 1}</span>
        <span>{step.label}</span>
      </li>)}
    </ol> : <p className="rounded-lg bg-[var(--page)] px-3 py-2 text-sm text-[var(--muted)]">Bu satır iptal edilmiş. Kiralama süreci devam etmiyor.</p>}
    {item.pickupMissed ? <p className="notice-warning rounded-lg p-3 text-sm">Teslim alma saati geçti. Mağazayla iletişime geç.</p> : null}
    {item.overdue ? <p className="notice-danger rounded-lg p-3 text-sm">Geri teslim zamanı geçti. Lütfen mağazayla iletişime geç.</p> : null}
    {item.returnedLate ? <p className="notice-warning rounded-lg p-3 text-sm">Bu ekipman planlanan geri teslim zamanından sonra teslim edilmiş.</p> : null}
    {item.status === "CONFIRMED" && !showCancel ? <p className="text-xs leading-5 text-[var(--muted)]">Teslim alma zamanına 24 saatten az kaldığında online iptal yapılamaz. Mağazayla iletişime geçebilirsin.</p> : null}
    {item.assignedUnits.length ? <details className="rental-unit-details rounded-lg border border-[var(--border)] p-3">
      <summary className="flex cursor-pointer items-center justify-between gap-2 text-sm font-medium">Cihaz ve kontrol ayrıntıları<ChevronDown aria-hidden="true" className="size-4 shrink-0" /></summary>
      <ul className="mt-3 space-y-3 border-t border-[var(--border)] pt-3">
        {item.assignedUnits.map((unit) => <li key={unit.unitId} className="space-y-1 break-words text-sm">
          <p className="font-medium">Cihaz: {unit.unitCode}</p>
          {unit.damageNote ? <p className="text-[var(--muted)]">Mağaza kontrol notu: {unit.damageNote}</p> : null}
          {(unit.manualChargeAmount ?? 0) > 0 ? <p className="text-[var(--muted)]">Mağazanın belirttiği ek tutar: {money.format(unit.manualChargeAmount ?? 0)}</p> : null}
        </li>)}
      </ul>
    </details> : null}
    {error ? <p role="alert" className="notice-danger rounded-lg p-3 text-sm">{error}</p> : null}
    {item.status === "PICKED_UP" || showCancel ? <div className="border-t border-[var(--border)] pt-4">
      <Button id={actionId} variant="secondary" disabled={busy}
        onClick={() => onAction(item, showCancel ? "CANCELLED" : "RETURN_REQUESTED", actionId)}>
        {saving ? "İşlem yapılıyor…" : showCancel ? "Rezervasyonu iptal et" : "Geri teslim talebi gönder"}
      </Button>
    </div> : null}
  </li>;
}
