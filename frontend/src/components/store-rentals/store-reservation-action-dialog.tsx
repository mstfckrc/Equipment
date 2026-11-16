"use client";

import { useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { formatDate } from "@/lib/utils/rental-dates";
import { conditionLabels } from "@/components/store-rentals/reservation-helpers";
import type { ReservationItem, StoreReservationItemUpdateRequest } from "@/types/rental";

export type ReservationAction = { item: ReservationItem; request: StoreReservationItemUpdateRequest; triggerId: string };
const labels = { CANCELLED: "Rezervasyon satırını iptal et", PICKED_UP: "Teslim kaydını onayla", RETURN_RECEIVED: "Geri teslimi kaydet", RETURNED: "Cihaz kontrolünü tamamla" };
const descriptions = {
  CANCELLED: "Yalnız bu ürün ve tarih aralığındaki satır iptal edilecek. Diğer satırlar etkilenmez.",
  PICKED_UP: "Cihazları müşteriye fiziksel olarak teslim ettiğini kaydedeceksin. Sistem hazır cihazları bu satıra otomatik atayacak.",
  RETURN_RECEIVED: "Cihazları mağazada fiziksel olarak teslim aldığını kaydedeceksin. Cihazlar kontrol bekleyecek; henüz hazır sayılmayacak.",
  RETURNED: "Aşağıdaki kontrol sonuçları kaydedilecek ve bu kiralama satırı tamamlanacak.",
};

export function StoreReservationActionDialog({ action, busy, error, onConfirm, onClose }: {
  action: ReservationAction; busy: boolean; error?: string; onConfirm: () => void; onClose: () => void;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const node = dialog.current;
    if (node && !node.open) {
      node.showModal();
      node.querySelector<HTMLHeadingElement>("h2")?.focus({ preventScroll: true });
      node.scrollTop = 0;
    }
    return () => { node?.close(); queueMicrotask(() => (document.getElementById(action.triggerId) ?? document.getElementById("store-rentals-title"))?.focus()); };
  }, [action.triggerId]);
  const { item, request } = action;
  const destructive = request.status === "CANCELLED" || request.unitInspections?.some((unit) => unit.condition === "RETIRED");
  return <dialog ref={dialog} aria-labelledby="store-rental-action-title" aria-describedby="store-rental-action-description"
    onCancel={(event) => { event.preventDefault(); if (!busy) onClose(); }}
    className="m-auto max-h-[85dvh] w-[min(92vw,36rem)] overflow-y-auto rounded-2xl border border-[var(--border)] bg-[var(--surface)] p-5 text-[var(--foreground)] shadow-xl backdrop:bg-[#20252b]/40 sm:p-7">
    <div className="space-y-5"><h2 id="store-rental-action-title" tabIndex={-1} className="text-xl font-semibold">{labels[request.status]}</h2>
      <div className="space-y-2 rounded-xl bg-[var(--page)] p-4 text-sm"><p className="break-words font-semibold">{item.listingName}</p>
        <p className="text-[var(--muted)]">{item.quantity} adet · {formatDate(item.pickupDate)} – {formatDate(item.returnDate)}</p></div>
      <p id="store-rental-action-description" className="text-sm leading-6 text-[var(--muted)]">{descriptions[request.status]}</p>
      {request.unitInspections ? <ul className="space-y-3">{request.unitInspections.map((inspection) => <li key={inspection.unitId} className="space-y-2 rounded-lg border border-[var(--border)] p-3 text-sm">
        <p className="break-all font-mono font-semibold">{item.assignedUnits.find((unit) => unit.unitId === inspection.unitId)?.unitCode}</p>
        <p>{conditionLabels[inspection.condition]}</p>
        {inspection.damageNote ? <p className="break-words text-[var(--muted)]">Not: {inspection.damageNote}</p> : null}
        {inspection.manualChargeAmount !== undefined ? <p>Kaydedilecek ek tutar: {new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(inspection.manualChargeAmount)}</p> : null}
      </li>)}</ul> : null}
      {request.unitInspections?.some((unit) => unit.condition === "RETIRED") ? <p className="notice-warning rounded-lg p-3 text-sm">Kullanımdan kaldırılacak cihazlar yeniden etkinleştirilemez. Bu karar kalıcıdır.</p> : null}
      {error ? <p role="alert" className="notice-danger rounded-lg p-3 text-sm">{error}</p> : null}
      <div className="flex flex-wrap justify-end gap-3 border-t border-[var(--border)] pt-5"><Button variant="secondary" disabled={busy} onClick={onClose}>Vazgeç</Button>
        <Button variant={destructive ? "danger" : "primary"} disabled={busy} onClick={onConfirm}>{busy ? "Kaydediliyor…" : "Onayla ve kaydet"}</Button></div>
    </div>
  </dialog>;
}
