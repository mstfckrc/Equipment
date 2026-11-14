"use client";

import { useEffect, useRef } from "react";
import { CircleAlert } from "lucide-react";
import { Button } from "@/components/ui/button";
import type { ApiPriceChange } from "@/lib/http/api-client";

const money = new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" });

export function PriceChangeDialog({ changes, onAccept, onClose, returnFocusId }: {
  changes: ApiPriceChange[]; onAccept: () => void; onClose: () => void; returnFocusId: string;
}) {
  const dialog = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const node = dialog.current;
    const previousFocus = document.activeElement;
    if (node && !node.open) node.showModal();
    return () => {
      node?.close();
      const target = document.getElementById(returnFocusId) ?? previousFocus;
      queueMicrotask(() => {
        if (target instanceof HTMLElement && target.isConnected) target.focus();
      });
    };
  }, [returnFocusId]);

  return <dialog ref={dialog} aria-labelledby="price-change-title" aria-describedby="price-change-description"
    onCancel={(event) => { event.preventDefault(); onClose(); }}
    className="m-auto w-[min(92vw,34rem)] rounded-2xl border border-[var(--border)] bg-[var(--surface)] p-0 text-[var(--foreground)] shadow-xl backdrop:bg-[#20252b]/40">
    <div className="space-y-5 p-5 sm:p-7">
      <div className="flex items-start gap-3">
        <CircleAlert aria-hidden="true" className="mt-1 size-6 shrink-0 text-[var(--accent-strong)]" />
        <div className="space-y-2"><h2 id="price-change-title" className="text-xl font-semibold">Fiyat değişmiş</h2>
          <p id="price-change-description" className="text-sm leading-6 text-[var(--muted)]">Yeni fiyatları sepete uygulayabilir veya seçimlerini gözden geçirebilirsin. Uyguladıktan sonra rezervasyonu yeniden onaylaman gerekir.</p></div>
      </div>
      <ul className="max-h-64 space-y-3 overflow-y-auto">
        {changes.map((change) => <li key={`${change.itemIndex}-${change.listingId}`} className="space-y-2 rounded-xl border border-[var(--border)] bg-[var(--page)] p-4 text-sm">
          <p className="break-words font-semibold">{change.listingName}</p>
          <p className="text-[var(--muted)]">Günlük: <del>{money.format(change.previouslyShownDailyPrice)}</del> → <strong className="text-[var(--foreground)]">{money.format(change.currentDailyPrice)}</strong></p>
          <p className="text-[var(--muted)]">Satır toplamı: {money.format(change.previouslyShownLineTotal)} → {money.format(change.currentLineTotal)}</p>
        </li>)}
      </ul>
      <div className="flex flex-col-reverse gap-3 border-t border-[var(--border)] pt-5 sm:flex-row sm:justify-end">
        <Button variant="secondary" onClick={onClose}>Sepete dön</Button>
        <Button onClick={onAccept}>Yeni fiyatları kabul et</Button>
      </div>
    </div>
  </dialog>;
}
