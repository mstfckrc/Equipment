import { CalendarDays } from "lucide-react";
import { formatDate } from "@/lib/utils/rental-dates";

export function RentalSelectionSummary({ pickupDate, returnDate, days, quantity, remaining, total }: {
  pickupDate?: string; returnDate?: string; days: number; quantity: number; remaining: number | null; total: number;
}) {
  return <section className="rental-selection-summary space-y-4" aria-labelledby="rental-summary-title" aria-live="polite">
    <h3 id="rental-summary-title" className="text-sm font-semibold">Seçiminin özeti</h3>
    <dl className="grid gap-4 sm:grid-cols-2">
      <div className="space-y-1"><dt className="flex items-center gap-2 text-xs text-[var(--muted)]"><CalendarDays aria-hidden="true" className="size-3.5" />Teslim alma</dt>
        <dd className="text-sm font-medium">{pickupDate ? `${formatDate(pickupDate)} · 12.00` : "Takvimden seç"}</dd></div>
      <div className="space-y-1"><dt className="flex items-center gap-2 text-xs text-[var(--muted)]"><CalendarDays aria-hidden="true" className="size-3.5" />Geri teslim</dt>
        <dd className="text-sm font-medium">{returnDate ? `${formatDate(returnDate)} · 09.00` : pickupDate ? "Geri teslim gününü seç" : "Takvimden seç"}</dd></div>
    </dl>
    {returnDate ? <div className="space-y-3 border-t border-[var(--accent-border)] pt-4">
      <div className="flex flex-wrap items-baseline justify-between gap-2">
        <p className="text-sm text-[var(--muted)]">{days} gün × {quantity} adet</p>
        <p className="text-xl font-semibold text-[var(--accent-strong)]">{new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(total)}</p>
      </div>
      {remaining !== null ? <p className="text-xs text-[var(--muted)]">Bu aralıkta kalan en düşük adet: {remaining}</p> : null}
      {remaining !== null && quantity > remaining ? <p role="alert" className="text-sm text-[var(--danger)]">Bu tarih aralığında seçilen adet uygun değil.</p> : null}
    </div> : <p className="text-xs leading-5 text-[var(--muted)]">Tarih aralığını tamamladığında toplam ücret burada görünecek.</p>}
  </section>;
}
