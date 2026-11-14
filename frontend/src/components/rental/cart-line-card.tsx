import { CalendarDays, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { LinkButton } from "@/components/ui/link-button";
import { withReturnTo } from "@/lib/utils/navigation";
import { calendarDayDifference, formatDate } from "@/lib/utils/rental-dates";
import type { ApiAvailabilityConflict } from "@/lib/http/api-client";
import type { CartLine } from "@/types/cart";

const money = new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" });

export function CartLineCard({ line, conflicts, busy, onQuantity, onRemove, onReselect }: {
  line: CartLine; conflicts: ApiAvailabilityConflict[]; busy: boolean;
  onQuantity: (quantity: number) => void; onRemove: () => void; onReselect: (href: string) => void;
}) {
  const days = calendarDayDifference(line.pickupDate, line.returnDate);
  const total = Math.round(line.dailyPrice * 100) * days * line.quantity / 100;
  const productHref = withReturnTo(`/shops/${encodeURIComponent(line.storeId)}/products/${encodeURIComponent(line.listingId)}`, "/cart");

  return <li className={`cart-line-card space-y-5 ${conflicts.length ? "cart-line-conflict" : ""}`}>
    <div className="flex items-start justify-between gap-3">
      <div className="min-w-0 space-y-2">
        <p className="break-words text-xs font-medium text-[var(--muted)]">{line.category}</p>
        <h3 className="break-words text-base font-semibold">{line.listingName}</h3>
      </div>
      <Button variant="ghost" className="shrink-0 px-2 text-[var(--danger)]" disabled={busy} onClick={onRemove} aria-label={`${line.listingName} satırını kaldır`}>
        <Trash2 aria-hidden="true" className="size-4" /><span className="hidden sm:inline">Kaldır</span>
      </Button>
    </div>
    <dl className="grid gap-3 rounded-lg bg-[var(--page)] p-3 sm:grid-cols-2">
      <div className="space-y-1"><dt className="flex items-center gap-1.5 text-xs text-[var(--muted)]"><CalendarDays aria-hidden="true" className="size-3.5" />Teslim alma</dt>
        <dd className="text-sm font-medium">{formatDate(line.pickupDate)} · 12.00</dd></div>
      <div className="space-y-1"><dt className="text-xs text-[var(--muted)]">Geri teslim</dt>
        <dd className="text-sm font-medium">{formatDate(line.returnDate)} · 09.00</dd></div>
    </dl>
    {conflicts.length ? <div className="notice-danger space-y-2 rounded-lg p-3 text-sm" role="alert">
      <p className="font-semibold">Bu satır için uygunluk değişmiş.</p>
      {conflicts.slice(0, 3).map((conflict) => <p key={conflict.date}>{formatDate(conflict.date)}: {conflict.remainingQuantity} adet kaldı, {conflict.requestedQuantity} istendi.</p>)}
      {conflicts.length > 3 ? <p>Diğer uygun olmayan günler de bu satırda.</p> : null}
      <Button variant="secondary" disabled={busy} onClick={() => onReselect(productHref)}>Tarih ve adedi yeniden seç</Button>
    </div> : null}
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div className="space-y-2"><label htmlFor={`quantity-${line.lineId}`} className="block text-xs font-medium text-[var(--muted)]">Adet</label>
        <Input id={`quantity-${line.lineId}`} className="w-24" type="number" min={1} max={1000} step={1} value={line.quantity} disabled={busy}
          onChange={(event) => onQuantity(Math.max(1, Math.min(1000, Math.trunc(Number(event.target.value)) || 1)))} />
      </div>
      <div className="min-w-0 space-y-1 text-right text-sm"><p className="break-words text-[var(--muted)]">{money.format(line.dailyPrice)} × {days} gün × {line.quantity}</p>
        <p className="text-lg font-semibold">{money.format(total)}</p></div>
    </div>
    {!busy ? <div className="border-t border-[var(--border)] pt-4"><LinkButton href={productHref} variant="secondary">Ürünü incele</LinkButton></div> : null}
  </li>;
}
