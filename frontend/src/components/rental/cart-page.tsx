"use client";

import { ShoppingBag, Store, ShieldCheck } from "lucide-react";
import { useRef, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { CustomerGuard } from "@/components/auth/customer-guard";
import { PageNavigation } from "@/components/layout/page-navigation";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { CartLineCard } from "@/components/rental/cart-line-card";
import { PriceChangeDialog } from "@/components/rental/price-change-dialog";
import { ApiError, type ApiAvailabilityConflict, type ApiPriceChange } from "@/lib/http/api-client";
import { calendarDayDifference } from "@/lib/utils/rental-dates";
import { useCart } from "@/providers/cart-provider";
import { useSession } from "@/providers/session-provider";
import { rentalService } from "@/services/rental/rental-service";
import type { CheckoutItemRequest, ReservationCheckoutRequest, ReservationCheckoutResponse } from "@/types/rental";
import type { CartLine } from "@/types/cart";

const money = new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" });

function checkoutRequest(lines: ReturnType<typeof useCart>["lines"]): ReservationCheckoutRequest {
  return {
    items: lines.map((line): CheckoutItemRequest => ({
      storeId: line.storeId,
      listingId: line.listingId,
      quantity: line.quantity,
      pickupDate: line.pickupDate,
      returnDate: line.returnDate,
      expectedDailyPrice: line.dailyPrice,
    })),
  };
}

export function CartPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { identity, withAccessToken } = useSession();
  const { lines, isReady, setQuantity, updatePrice, removeLine, clear } = useCart();
  const [priceChanges, setPriceChanges] = useState<ApiPriceChange[]>([]);
  const [availabilityConflicts, setAvailabilityConflicts] = useState<ApiAvailabilityConflict[]>([]);
  const [requestError, setRequestError] = useState<string | null>(null);
  const attempt = useRef<{ fingerprint: string; key: string } | null>(null);

  const checkout = useMutation<ReservationCheckoutResponse, Error, { body: ReservationCheckoutRequest; key: string }>({
    mutationFn: ({ body, key }) => withAccessToken((token) => rentalService.checkout(body, key, token)),
    onSuccess: (response) => {
      clear();
      attempt.current = null;
      void queryClient.invalidateQueries({ queryKey: ["my-reservations", identity?.id] });
      router.push(`/rentals?checkoutId=${encodeURIComponent(response.checkoutId)}`);
    },
    onError: (error) => {
      setRequestError(error.message || "Rezervasyon oluşturulamadı.");
      setAvailabilityConflicts(error instanceof ApiError ? error.availabilityConflicts : []);
      if (error instanceof ApiError && error.code === "PRICE_CHANGED") {
        setPriceChanges(error.priceChanges);
      }
    },
  });

  const linesTotalCents = lines.reduce((sum, line) =>
    sum + Math.round(line.dailyPrice * 100) * calendarDayDifference(line.pickupDate, line.returnDate) * line.quantity, 0);
  const linesTotal = linesTotalCents / 100;
  const storeGroups = new Map<string, { name: string; items: Array<{ line: CartLine; index: number }> }>();
  lines.forEach((line, index) => {
    const group = storeGroups.get(line.storeId) ?? { name: line.storeName, items: [] };
    group.items.push({ line, index });
    storeGroups.set(line.storeId, group);
  });

  function submitCheckout() {
    if (checkout.isPending || !isReady || !lines.length) return;
    const body = checkoutRequest(lines);
    const fingerprint = JSON.stringify(body);
    if (!attempt.current || attempt.current.fingerprint !== fingerprint) {
      attempt.current = { fingerprint, key: crypto.randomUUID() };
    }
    setRequestError(null);
    setAvailabilityConflicts([]);
    checkout.mutate({ body, key: attempt.current.key });
  }

  function removeConflictedLine(lineId: string) {
    removeLine(lineId);
    setAvailabilityConflicts([]);
    setRequestError(null);
    checkout.reset();
  }

  function acceptPrices() {
    for (const change of priceChanges) {
      const line = lines[change.itemIndex];
      if (line) updatePrice(line.lineId, change.currentDailyPrice);
    }
    setPriceChanges([]);
    setAvailabilityConflicts([]);
    setRequestError(null);
    checkout.reset();
  }

  return (
    <CustomerGuard>
      <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-6xl space-y-8 px-5 pb-16 pt-8 sm:px-8 sm:pt-12">
        <PageNavigation href="/shops" label="Mağazalara dön" items={[{ label: "Ana sayfa", href: "/" }, { label: "Sepetim" }]} />
        <div className="space-y-2">
          <p className="home-eyebrow">Kiralama hazırlığı</p>
          <h1 className="catalog-title">Sepetim</h1>
          <p className="text-sm leading-6 text-[var(--muted)]">Seçimlerini gözden geçir. Tarih, adet ve güncel fiyatlar onay sırasında son kez kontrol edilir.</p>
        </div>

        {!isReady ? <p role="status" className="text-sm text-[var(--muted)]">Sepet hazırlanıyor…</p> : null}
        {isReady && lines.length === 0 ? (
          <section className="surface-card space-y-5 px-6 py-12 text-center">
            <ShoppingBag aria-hidden="true" className="mx-auto size-10 text-[var(--accent-strong)]" strokeWidth={1.4} />
            <h2 className="text-xl font-semibold">Sepetin henüz boş</h2>
            <p className="mx-auto max-w-md text-sm leading-6 text-[var(--muted)]">Mağazalardaki ekipmanları incele, tarih aralığını ve ihtiyacın olan adedi seçerek başlayabilirsin.</p>
            <LinkButton href="/shops">Mağazalara göz at</LinkButton>
          </section>
        ) : null}

        {lines.length > 0 ? (
          <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_20rem]">
            <div className="min-w-0 space-y-6">
              {Array.from(storeGroups, ([storeId, group]) => <section key={storeId} className="surface-card min-w-0 overflow-hidden" aria-labelledby={`cart-store-${storeId}`}>
                <header className="flex items-start gap-3 border-b border-[var(--border)] bg-[var(--page)] px-5 py-4">
                  <Store aria-hidden="true" className="mt-0.5 size-5 shrink-0 text-[var(--accent-strong)]" />
                  <div className="min-w-0 space-y-1"><h2 id={`cart-store-${storeId}`} className="break-words text-sm font-semibold">{group.name}</h2>
                    <p className="text-xs text-[var(--muted)]">{group.items.length} sepet satırı · Mağazadan teslim</p></div>
                </header>
                <ul className="divide-y divide-[var(--border)]">
                  {group.items.map(({ line, index }) => <CartLineCard key={line.lineId} line={line}
                    conflicts={availabilityConflicts.filter((conflict) => conflict.itemIndex === index)} busy={checkout.isPending}
                    onQuantity={(quantity) => { setQuantity(line.lineId, quantity); setAvailabilityConflicts([]); setRequestError(null); }}
                    onRemove={() => removeConflictedLine(line.lineId)}
                    onReselect={(href) => { removeConflictedLine(line.lineId); router.push(href); }} />)}
                </ul>
              </section>)}
              <LinkButton variant="secondary" href="/shops">Ekipman keşfetmeye devam et</LinkButton>
            </div>

            <aside className="surface-card min-w-0 space-y-5 p-5 sm:p-6 lg:sticky lg:top-5" aria-labelledby="checkout-summary-title">
              <h2 id="checkout-summary-title" className="text-lg font-semibold">Rezervasyon özeti</h2>
              <dl className="space-y-3 text-sm">
                <div className="flex justify-between gap-3"><dt className="text-[var(--muted)]">Mağaza</dt><dd>{storeGroups.size}</dd></div>
                <div className="flex justify-between gap-3"><dt className="text-[var(--muted)]">Sepet satırı</dt><dd>{lines.length}</dd></div>
                <div className="flex flex-wrap items-baseline justify-between gap-2 border-t border-[var(--border)] pt-4"><dt className="font-semibold">Toplam tutar</dt>
                  <dd className="text-2xl font-semibold text-[var(--accent-strong)]">{money.format(linesTotal)}</dd></div>
              </dl>
              <div className="rounded-lg bg-[var(--page)] p-3 text-xs leading-5 text-[var(--muted)]">Demo rezervasyon: gerçek ödeme alınmaz. Onay, seçilen ekipmanlar için rezervasyon oluşturur.</div>
              {requestError && availabilityConflicts.length === 0 && priceChanges.length === 0 ? <p role="alert" className="text-sm text-[var(--danger)]">{requestError}</p> : null}
              <Button id="checkout-submit" className="w-full" onClick={submitCheckout} disabled={!isReady || checkout.isPending || lines.length === 0}>
                {checkout.isPending ? "Uygunluk kontrol ediliyor…" : "Rezervasyonu onayla"}
              </Button>
              <p className="flex items-start gap-2 text-xs leading-5 text-[var(--muted)]"><ShieldCheck aria-hidden="true" className="mt-0.5 size-4 shrink-0 text-[var(--accent-strong)]" />Fiyat ve stok sunucuda kontrol edilir. Bir satır uygun değilse diğerleri de kesinleştirilmez.</p>
            </aside>
          </div>
        ) : null}
      </main>
      {priceChanges.length > 0 ? <PriceChangeDialog changes={priceChanges} onAccept={acceptPrices} onClose={() => setPriceChanges([])} returnFocusId="checkout-submit" /> : null}
    </CustomerGuard>
  );
}
