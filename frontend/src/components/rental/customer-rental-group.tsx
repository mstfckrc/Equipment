import { FileText, Store } from "lucide-react";
import { LinkButton } from "@/components/ui/link-button";
import { CustomerRentalItem, type CustomerRentalAction } from "@/components/rental/customer-rental-item";
import type { Reservation, ReservationItem } from "@/types/rental";

export type RentalFilter = "ALL" | "ACTIVE" | "COMPLETED" | "CANCELLED";

export function matchesRentalFilter(item: ReservationItem, filter: RentalFilter) {
  if (filter === "COMPLETED") return item.status === "RETURNED";
  if (filter === "CANCELLED") return item.status === "CANCELLED";
  if (filter === "ACTIVE") return item.status !== "RETURNED" && item.status !== "CANCELLED";
  return true;
}

export function CustomerRentalGroup({ checkoutId, reservations, filter, storeNames, busy, savingItemId, actionError, onAction }: {
  checkoutId: string; reservations: Reservation[]; filter: RentalFilter; storeNames: Map<string, string>;
  busy: boolean; savingItemId?: string; actionError: { itemId: string; message: string } | null;
  onAction: (item: ReservationItem, status: CustomerRentalAction, triggerId: string) => void;
}) {
  const total = reservations.reduce((sum, reservation) => sum + Math.round(reservation.totalAmount * 100), 0) / 100;
  const visible = reservations.map((reservation) => ({ ...reservation, items: reservation.items.filter((item) => matchesRentalFilter(item, filter)) }))
    .filter((reservation) => reservation.items.length > 0);
  if (!visible.length) return null;

  return <section className="surface-card min-w-0 overflow-hidden" aria-labelledby={`checkout-${checkoutId}`}>
    <header className="flex flex-col gap-4 border-b border-[var(--border)] px-5 py-5 sm:flex-row sm:items-center sm:justify-between sm:px-6">
      <div className="min-w-0 space-y-1"><h2 id={`checkout-${checkoutId}`} className="text-base font-semibold">Rezervasyon #{checkoutId.slice(0, 8)}</h2>
        <p className="text-xs text-[var(--muted)]">{new Intl.DateTimeFormat("tr-TR", { dateStyle: "medium", timeStyle: "short", timeZone: "Europe/Istanbul" }).format(new Date(reservations[0].createdAt))}</p></div>
      <div className="flex flex-wrap items-center justify-between gap-x-6 gap-y-3 sm:justify-end">
        <div className="space-y-1 sm:text-right"><p className="text-xs text-[var(--muted)]">Rezervasyon anındaki toplam</p>
          <p className="text-base font-semibold">{new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(total)}</p></div>
        <div className="space-y-2 sm:text-right">
          <LinkButton variant="secondary" href={`/rentals/confirmations/${encodeURIComponent(checkoutId)}`}>
            <FileText aria-hidden="true" className="size-4 shrink-0" />Onay belgesi
          </LinkButton>
          <p className="text-xs text-[var(--muted)]">
            {reservations[0].confirmationStatus === "READY" ? "Görüntülemeye hazır" :
              reservations[0].confirmationStatus === "FAILED" ? "Hazırlama tamamlanamadı" : "Hazırlanıyor"}
          </p>
        </div>
      </div>
    </header>
    <div className="divide-y divide-[var(--border)]">
      {visible.map((reservation) => <section key={reservation.reservationId} className="min-w-0" aria-labelledby={`rental-store-${reservation.reservationId}`}>
        <header className="flex items-start gap-3 border-b border-[var(--border)] bg-[var(--page)] px-5 py-4">
          <Store aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-[var(--accent-strong)]" />
          <div className="min-w-0 space-y-1"><h3 id={`rental-store-${reservation.reservationId}`} className="break-words text-sm font-semibold">{storeNames.get(reservation.storeId) ?? "Mağaza"}</h3>
            <p className="text-xs text-[var(--muted)]">{reservation.items.length} {filter === "ALL" ? "ürün satırı" : "filtreye uyan ürün satırı"} · Mağazadan teslim</p></div>
        </header>
        <ul className="divide-y divide-[var(--border)]">
          {reservation.items.map((item) => <CustomerRentalItem key={item.itemId} item={item} busy={busy}
            saving={busy && savingItemId === item.itemId} error={actionError?.itemId === item.itemId ? actionError.message : undefined} onAction={onAction} />)}
        </ul>
      </section>)}
    </div>
  </section>;
}
