"use client";

import { useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { formatDate } from "@/lib/utils/rental-dates";
import type { ReservationItem } from "@/types/rental";
import type { CustomerRentalAction } from "@/components/rental/customer-rental-item";

export function RentalActionDialog({ item, action, triggerId, busy, error, onConfirm, onClose }: {
  item: ReservationItem; action: CustomerRentalAction; triggerId: string; busy: boolean; error?: string;
  onConfirm: () => void; onClose: () => void;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const node = dialog.current;
    if (node && !node.open) node.showModal();
    return () => {
      node?.close();
      queueMicrotask(() => (document.getElementById(triggerId) ?? document.getElementById("rentals-title"))?.focus());
    };
  }, [triggerId]);
  const cancel = action === "CANCELLED";

  return <dialog ref={dialog} aria-labelledby="rental-action-title" aria-describedby="rental-action-description"
    onCancel={(event) => { event.preventDefault(); if (!busy) onClose(); }}
    className="m-auto w-[min(92vw,32rem)] rounded-2xl border border-[var(--border)] bg-[var(--surface)] p-0 text-[var(--foreground)] shadow-xl backdrop:bg-[#20252b]/40">
    <div className="space-y-5 p-5 sm:p-7">
      <h2 id="rental-action-title" className="text-xl font-semibold">{cancel ? "Rezervasyon satırını iptal et" : "Geri teslim talebini onayla"}</h2>
      <div className="space-y-2 rounded-xl bg-[var(--page)] p-4 text-sm">
        <p className="break-words font-semibold">{item.listingName}</p>
        <p className="text-[var(--muted)]">{item.quantity} adet · {formatDate(item.pickupDate)} – {formatDate(item.returnDate)}</p>
      </div>
      <p id="rental-action-description" className="text-sm leading-6 text-[var(--muted)]">{cancel
        ? "Yalnız bu ürün ve tarih aralığına ait satır iptal edilecek. Rezervasyondaki diğer satırlar etkilenmez."
        : "Mağazaya geri teslim talebin gönderilecek. Bu işlem, cihazın mağazaya teslim edildiği veya kontrolünün tamamlandığı anlamına gelmez."}</p>
      {error ? <p role="alert" className="notice-danger rounded-lg p-3 text-sm">{error}</p> : null}
      <div className="flex flex-col-reverse gap-3 border-t border-[var(--border)] pt-5 sm:flex-row sm:justify-end">
        <Button variant="secondary" disabled={busy} onClick={onClose}>Vazgeç</Button>
        <Button variant={cancel ? "danger" : "primary"} disabled={busy} onClick={onConfirm}>
          {busy ? "İşlem yapılıyor…" : cancel ? "İptali onayla" : "Talebi gönder"}</Button>
      </div>
    </div>
  </dialog>;
}
