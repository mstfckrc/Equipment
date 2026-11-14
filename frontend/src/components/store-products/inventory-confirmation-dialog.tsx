"use client";

import { useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";

export function InventoryConfirmationDialog({ title, description, triggerId, busy, error, onConfirm, onClose }: {
  title: string; description: string; triggerId: string; busy: boolean; error: string | null;
  onConfirm: () => void; onClose: () => void;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const node = dialog.current;
    if (node && !node.open) node.showModal();
    return () => { node?.close(); queueMicrotask(() => (document.getElementById(triggerId) ?? document.getElementById("inventory-title"))?.focus()); };
  }, [triggerId]);
  return <dialog ref={dialog} aria-labelledby="inventory-confirm-title" aria-describedby="inventory-confirm-description"
    onCancel={(event) => { event.preventDefault(); if (!busy) onClose(); }}
    className="m-auto w-[min(92vw,32rem)] rounded-2xl border border-[var(--border)] bg-[var(--surface)] p-6 text-[var(--foreground)] shadow-xl backdrop:bg-[#20252b]/40">
    <div className="space-y-5"><h2 id="inventory-confirm-title" className="text-xl font-semibold">{title}</h2>
      <p id="inventory-confirm-description" className="break-words text-sm leading-6 text-[var(--muted)]">{description}</p>
      {error ? <p role="alert" className="notice-danger rounded-lg p-3 text-sm">{error}</p> : null}
      <div className="flex flex-wrap justify-end gap-3 border-t border-[var(--border)] pt-5">
        <Button variant="secondary" disabled={busy} onClick={onClose}>Vazgeç</Button>
        <Button variant="danger" disabled={busy} onClick={onConfirm}>{busy ? "İşlem yapılıyor…" : "Onayla"}</Button>
      </div>
    </div>
  </dialog>;
}
