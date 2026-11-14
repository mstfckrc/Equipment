"use client";

import { useState } from "react";
import { useQueries, useQuery } from "@tanstack/react-query";
import { DayPicker, type DateRange } from "react-day-picker";
import { CalendarDays, ChevronLeft, ChevronRight } from "lucide-react";
import { toast } from "sonner";
import { CustomerGuard } from "@/components/auth/customer-guard";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { PageNavigation } from "@/components/layout/page-navigation";
import { LinkButton } from "@/components/ui/link-button";
import { RentalProductInfo } from "@/components/rental/rental-product-info";
import { RentalSelectionSummary } from "@/components/rental/rental-selection-summary";
import { withReturnTo } from "@/lib/utils/navigation";
import { useCart } from "@/providers/cart-provider";
import { useSession } from "@/providers/session-provider";
import { catalogService } from "@/services/catalog/catalog-service";
import { rentalService } from "@/services/rental/rental-service";
import { storeService } from "@/services/store/store-service";
import {
  addCalendarDays,
  calendarDayDifference,
  formatLocalDate,
  todayInIstanbul,
} from "@/lib/utils/rental-dates";
import type { AvailabilityDay } from "@/types/rental";

function firstOfMonth(date: Date) {
  return formatLocalDate(new Date(date.getFullYear(), date.getMonth(), 1, 12));
}

function addMonths(date: Date, amount: number) {
  return new Date(date.getFullYear(), date.getMonth() + amount, 1, 12);
}

function monthOffset(start: Date, date: Date) {
  return (date.getFullYear() - start.getFullYear()) * 12 + date.getMonth() - start.getMonth();
}

function availabilityWindow(start: Date, index: number) {
  const from = addMonths(start, index * 12);
  const to = addMonths(from, 12);
  return { fromDate: formatLocalDate(from), toDateExclusive: formatLocalDate(to) };
}

function monthLabel(date: Date) {
  return new Intl.DateTimeFormat("tr-TR", { month: "long", year: "numeric" }).format(date);
}

function availabilityMap(days: AvailabilityDay[]) {
  return new Map(days.map((day) => [day.date, day]));
}

export function RentalProductDetail({
  storeId,
  productId,
  returnTo,
}: {
  storeId: string;
  productId: string;
  returnTo: string;
}) {
  const { status, identity, withAccessToken, retry } = useSession();
  const { addLine } = useCart();
  const [month, setMonth] = useState(() => new Date());
  const [availabilityStart] = useState(() => new Date(new Date().getFullYear(), new Date().getMonth(), 1, 12));
  const [monthPickerOpen, setMonthPickerOpen] = useState(false);
  const [monthPickerValue, setMonthPickerValue] = useState(() => firstOfMonth(new Date()).slice(0, 7));
  const [range, setRange] = useState<DateRange>();
  const [quantity, setQuantity] = useState(1);
  const [selectionError, setSelectionError] = useState<string | null>(null);
  const customer = status === "authenticated" && identity?.role === "CUSTOMER";

  const productQuery = useQuery({
    queryKey: ["public-product", storeId, productId],
    queryFn: () => catalogService.getStoreProduct(storeId, productId),
  });
  const storeQuery = useQuery({
    queryKey: ["public-store", storeId],
    queryFn: () => storeService.getPublicStore(storeId),
  });

  const relevantDates = [month];
  if (range?.from) relevantDates.push(range.from);
  if (range?.to) relevantDates.push(range.to);
  const windowIndices = relevantDates.map((date) =>
    Math.floor(monthOffset(availabilityStart, date) / 12),
  );
  const firstWindow = Math.min(...windowIndices);
  const lastWindow = Math.max(...windowIndices);
  const availabilityWindowIndices = Array.from(
    { length: lastWindow - firstWindow + 1 },
    (_, index) => firstWindow + index,
  );
  const availabilityQueries = useQueries({
    queries: availabilityWindowIndices.map((index) => {
      const window = availabilityWindow(availabilityStart, index);
      return {
        queryKey: ["rental-availability", identity?.id, storeId, productId, window.fromDate],
        queryFn: () =>
          withAccessToken((token) =>
            rentalService.getAvailability(
              storeId,
              productId,
              window.fromDate,
              window.toDateExclusive,
              token,
            ),
          ),
        enabled: customer,
        staleTime: 5 * 60 * 1000,
        gcTime: 30 * 60 * 1000,
      };
    }),
  });
  const days = availabilityQueries.flatMap((query) => query.data?.days ?? []);
  const daysByDate = availabilityMap(days);
  const availabilityIsLoading = availabilityQueries.some(
    (query) => query.isPending || query.isFetching,
  );
  const availabilityHasError = availabilityQueries.some((query) => query.isError);
  const remainingOn = (date: Date) =>
    daysByDate.get(formatLocalDate(date))?.remainingUnits ?? 0;

  function changeMonth(nextMonth: Date) {
    setMonth(nextMonth);
  }

  function selectRange(nextRange: DateRange | undefined) {
    setSelectionError(null);
    if (!nextRange?.from) {
      setRange(undefined);
      return;
    }
    const pickupDate = formatLocalDate(nextRange.from);
    if (!nextRange.to) {
      const pickupAvailability = remainingOn(nextRange.from);
      if (pickupAvailability < 1) {
        setRange(undefined);
        setSelectionError(
          "Seçilen teslim alma gününde uygun cihaz yok.",
        );
        return;
      }
      if (quantity > pickupAvailability) {
        setQuantity(pickupAvailability);
        toast.info(`Bu gün için adet ${pickupAvailability} olarak ayarlandı.`);
      }
      setRange({ from: nextRange.from });
      return;
    }

    const returnDate = formatLocalDate(nextRange.to);
    const rentalDays = calendarDayDifference(pickupDate, returnDate);
    if (rentalDays < 1) {
      setRange({ from: nextRange.from });
      setSelectionError(
        "Geri teslim günü, teslim alma gününden sonra olmalı.",
      );
      return;
    }

    let rangeCapacity = Number.MAX_SAFE_INTEGER;
    const conflicts: string[] = [];
    for (let offset = 0; offset < rentalDays; offset += 1) {
      const date = addCalendarDays(pickupDate, offset);
      const day = daysByDate.get(date);
      if (!day || day.remainingUnits < 1) conflicts.push(date);
      else rangeCapacity = Math.min(rangeCapacity, day.remainingUnits);
    }
    if (conflicts.length > 0) {
      setRange({ from: nextRange.from });
      setSelectionError(
        "Bu tarih aralığında uygun olmayan gün var. Tarih aralığını değiştirip yeniden dene.",
      );
      return;
    }
    if (quantity > rangeCapacity) {
      setQuantity(rangeCapacity);
      toast.info(
        `Bu tarih aralığında en fazla ${rangeCapacity} adet uygun; adet buna göre ayarlandı.`,
      );
    }
    setRange(nextRange);
  }

  const product = productQuery.data;
  const rentalDays =
    range?.from && range.to
      ? calendarDayDifference(
          formatLocalDate(range.from),
          formatLocalDate(range.to),
        )
      : 0;
  const activeDailyPrice = product?.dailyPrice ?? 0;
  const total =
    (Math.round(activeDailyPrice * 100) * rentalDays * quantity) / 100;
  const selectedRemaining =
    range?.from && range.to
      ? Math.min(
          ...Array.from(
            { length: rentalDays },
            (_, index) =>
              daysByDate.get(
                addCalendarDays(formatLocalDate(range.from!), index),
              )?.remainingUnits ?? 0,
          ),
        )
      : null;
  const pickupRemaining = range?.from ? remainingOn(range.from) : null;
  const currentMonthStart = firstOfMonth(month);
  const nextMonthStart = formatLocalDate(
    new Date(month.getFullYear(), month.getMonth() + 1, 1, 12),
  );
  const currentMonthDays = days.filter(
    (day) => day.date >= currentMonthStart && day.date < nextMonthStart,
  );
  const calendarMaximum = currentMonthDays.length
    ? Math.max(...currentMonthDays.map((day) => day.remainingUnits))
    : 1;
  const availableMaximum = selectedRemaining ?? pickupRemaining ?? calendarMaximum;
  const quantityLimit = Math.max(1, availableMaximum);
  const monthPickerMinimum = firstOfMonth(new Date()).slice(0, 7);
  const isFirstMonth =
    new Date(month.getFullYear(), month.getMonth(), 1, 12).getTime() <=
    new Date(new Date().getFullYear(), new Date().getMonth(), 1, 12).getTime();
  const returnPath = withReturnTo(`/shops/${encodeURIComponent(storeId)}/products/${encodeURIComponent(productId)}`, returnTo);
  const loginHref = `/login?returnTo=${encodeURIComponent(returnPath)}`;

  function addToCart() {
    if (
      !product ||
      !storeQuery.data ||
      !range?.from ||
      !range.to ||
      rentalDays < 1
    )
      return;
    addLine({
      storeId,
      storeName: storeQuery.data.storeName,
      listingId: product.productId,
      listingName: product.name,
      category: product.category,
      dailyPrice: activeDailyPrice,
      quantity,
      pickupDate: formatLocalDate(range.from),
      returnDate: formatLocalDate(range.to),
    });
    setRange(undefined);
    toast.success(
      "Ürün sepete eklendi. Başka bir tarih aralığı da seçebilirsin.",
    );
  }

  if (productQuery.isPending || storeQuery.isPending) {
    return (
      <>
        <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-6xl px-5 py-12">
          <p role="status" className="text-sm text-[var(--muted)]">
            Ürün bilgileri yükleniyor…
          </p>
        </main>
      </>
    );
  }

  if (
    productQuery.isError ||
    storeQuery.isError ||
    !product ||
    !storeQuery.data
  ) {
    return (
      <>
        <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-6xl space-y-5 px-5 py-12">
          <p
            role="alert"
            className="rounded-lg border border-[var(--border)] bg-white p-4 text-sm"
          >
            Ürün bulunamadı veya şu anda yayında değil.
          </p>
          <PageNavigation href={returnTo} label={returnTo === "/cart" ? "Sepete dön" : "Mağazaya dön"} items={[{ label: "Ana sayfa", href: "/" }, { label: "Ürün" }]} />
        </main>
      </>
    );
  }

  return (
    <>
      <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-6xl space-y-8 px-5 pb-16 pt-8 sm:px-8 sm:pt-12">
        <PageNavigation href={returnTo} label={returnTo === "/cart" ? "Sepete dön" : "Mağazaya dön"}
          items={[{ label: "Ana sayfa", href: "/" }, { label: "Mağazalar", href: "/shops" }, { label: storeQuery.data.storeName, href: returnTo === "/cart" ? `/shops/${encodeURIComponent(storeId)}` : returnTo }, { label: product.name }]} />
        <div className="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(22rem,0.9fr)] lg:gap-8">
          <RentalProductInfo product={product} store={storeQuery.data} />

          <section
            className="surface-card min-w-0 space-y-5 p-5 sm:p-6"
            aria-labelledby="rental-select-title"
          >
            <div className="space-y-2">
              <h2
                id="rental-select-title"
                className="text-xl font-semibold tracking-tight"
              >
                Tarih ve adet seç
              </h2>
              <p className="text-sm leading-6 text-[var(--muted)]">
                Teslim alma günü 12.00, geri teslim günü 09.00’dur. Geri teslim
                günü kiralama gününe dahil değildir.
              </p>
            </div>

            {status === "checking" ? (
              <p role="status" className="text-sm text-[var(--muted)]">
                Oturum kontrol ediliyor…
              </p>
            ) : null}
            {status === "error" ? (
              <p role="alert" className="text-sm text-[var(--danger)]">
                Oturum doğrulanamadı. Yeniden deneyip takvimi aç.
              </p>
            ) : null}
            {status === "anonymous" ? (
              <div className="subtle-panel space-y-3 p-4">
                <p className="text-sm leading-6">
                  Uygunluk takvimini görmek ve ürünü sepete eklemek için müşteri
                  hesabınla giriş yap.
                </p>
                <div className="flex flex-wrap gap-2">
                  <LinkButton href={loginHref}>Giriş yap</LinkButton>
                  <LinkButton variant="secondary" href={`/register?returnTo=${encodeURIComponent(returnPath)}`}>Hesap oluştur</LinkButton>
                </div>
              </div>
            ) : null}
            {status === "error" ? (
              <Button variant="secondary" onClick={retry}>
                Oturumu yeniden dene
              </Button>
            ) : null}
            {status === "authenticated" && identity?.role !== "CUSTOMER" ? (
              <p className="text-sm text-[var(--muted)]">
                Kiralama takvimi müşteri hesaplarına açıktır.
              </p>
            ) : null}

            {customer ? (
              <CustomerGuard>
                <div className="space-y-5">
                  <div className="rental-calendar rounded-lg border border-[var(--border)] p-3 sm:p-4">
                    <p
                      role="status"
                      aria-live="polite"
                      className="min-h-6 px-2 py-1 text-xs text-[var(--muted)]"
                    >
                      {availabilityIsLoading ? "Uygunluk yükleniyor…" : " "}
                    </p>
                    {availabilityHasError ? (
                      <p
                        role="alert"
                        className="px-2 py-3 text-sm text-[var(--danger)]"
                      >
                        Takvim alınamadı. Sayfayı yenileyip tekrar dene.
                      </p>
                    ) : null}
                      <div className="relative mb-2 grid grid-cols-[2.75rem_minmax(0,1fr)_2.75rem] items-center">
                        <button
                          type="button"
                          aria-label="Önceki ay"
                          disabled={isFirstMonth}
                          className="inline-flex size-11 items-center justify-center rounded-lg text-[var(--muted)] hover:bg-[var(--accent-soft)] hover:text-[var(--accent-strong)] disabled:cursor-not-allowed disabled:opacity-40"
                          onClick={() =>
                            changeMonth(new Date(month.getFullYear(), month.getMonth() - 1, 1, 12))
                          }
                        >
                          <ChevronLeft aria-hidden="true" className="size-5" />
                        </button>
                        <button
                          type="button"
                          aria-haspopup="dialog"
                          aria-expanded={monthPickerOpen}
                          className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg px-3 text-sm font-semibold capitalize hover:bg-[var(--background)]"
                          onClick={() => {
                            setMonthPickerValue(firstOfMonth(month).slice(0, 7));
                            setMonthPickerOpen((open) => !open);
                          }}
                        >
                          <CalendarDays aria-hidden="true" className="size-4 text-[var(--accent-strong)]" />
                          {monthLabel(month)}
                        </button>
                        <button
                          type="button"
                          aria-label="Sonraki ay"
                          className="inline-flex size-11 items-center justify-center rounded-lg text-[var(--muted)] hover:bg-[var(--accent-soft)] hover:text-[var(--accent-strong)] disabled:cursor-not-allowed disabled:opacity-40"
                          onClick={() =>
                            changeMonth(new Date(month.getFullYear(), month.getMonth() + 1, 1, 12))
                          }
                        >
                          <ChevronRight aria-hidden="true" className="size-5" />
                        </button>
                        {monthPickerOpen ? (
                          <div
                            role="dialog"
                            aria-label="Ay ve yıl seç"
                            className="absolute left-1/2 top-12 z-20 w-64 -translate-x-1/2 space-y-3 rounded-xl border border-[var(--border)] bg-white p-4 shadow-lg"
                          >
                            <label htmlFor="rental-month-picker" className="block text-sm font-medium">
                              Ay ve yıl
                            </label>
                            <Input
                              id="rental-month-picker"
                              type="month"
                              min={monthPickerMinimum}
                              value={monthPickerValue}
                              onChange={(event) => setMonthPickerValue(event.target.value)}
                            />
                            <Button
                              type="button"
                              className="w-full"
                              disabled={!monthPickerValue || monthPickerValue < monthPickerMinimum}
                              onClick={() => {
                                const [year, selectedMonth] = monthPickerValue.split("-").map(Number);
                                changeMonth(new Date(year, selectedMonth - 1, 1, 12));
                                setMonthPickerOpen(false);
                              }}
                            >
                              Bu aya git
                            </Button>
                          </div>
                        ) : null}
                      </div>
                      <DayPicker
                        className="mx-auto w-fit"
                        lang="tr-TR"
                        fixedWeeks
                        classNames={{ month_caption: "hidden" }}
                        mode="range"
                        selected={range}
                        onSelect={selectRange}
                        month={month}
                        onMonthChange={changeMonth}
                        numberOfMonths={1}
                        hideNavigation
                        weekStartsOn={1}
                        disabled={(date) => {
                          const dateValue = formatLocalDate(date);
                          const day = daysByDate.get(dateValue);
                          return dateValue <= todayInIstanbul() ||
                            !day ||
                            day.remainingUnits < 1;
                        }}
                        modifiers={{
                          unavailable: (date) => {
                            if (formatLocalDate(date) <= todayInIstanbul()) return false;
                            const day = daysByDate.get(formatLocalDate(date));
                            return Boolean(
                              day && day.remainingUnits < quantity,
                            );
                          },
                          available: (date) => {
                            if (formatLocalDate(date) <= todayInIstanbul()) return false;
                            const day = daysByDate.get(formatLocalDate(date));
                            return Boolean(day && day.remainingUnits >= quantity);
                          },
                        }}
                        modifiersClassNames={{
                          unavailable: "rental-day-unavailable",
                          available: "rental-day-available",
                        }}
                        formatters={{
                          formatWeekdayName: (date) =>
                            new Intl.DateTimeFormat("tr-TR", {
                              weekday: "short",
                            }).format(date),
                        }}
                      />
                  </div>
                  <p className="text-xs text-[var(--muted)]">
                    <span className="mr-2 inline-block size-3 rounded-sm bg-[#eaf5ef] align-middle" />
                    Uygun gün{" "}
                    <span className="mx-2 inline-block size-3 rounded-sm bg-[#fff0ee] align-middle" />
                    Seçilen adet için uygun değil
                  </p>
                  {selectionError ? (
                    <p role="alert" className="text-sm text-[var(--danger)]">
                      {selectionError}
                    </p>
                  ) : null}
                  <RentalSelectionSummary
                    pickupDate={range?.from ? formatLocalDate(range.from) : undefined}
                    returnDate={range?.to ? formatLocalDate(range.to) : undefined}
                    days={rentalDays} quantity={quantity} remaining={selectedRemaining} total={total}
                  />
                  <label
                    className="block max-w-44 space-y-2 text-sm font-medium"
                    htmlFor="rental-quantity"
                  >
                    Adet
                    <Input
                      id="rental-quantity"
                      type="number"
                      min={1}
                      max={quantityLimit}
                      step={1}
                      disabled={availableMaximum < 1}
                      value={quantity}
                      onChange={(event) =>
                        setQuantity(
                          Math.max(
                            1,
                            Math.min(quantityLimit, Number(event.target.value) || 1),
                          ),
                        )
                      }
                    />
                    <span className="block text-xs font-normal text-[var(--muted)]">
                      Seçili tarihlerde en fazla {availableMaximum} adet
                    </span>
                  </label>
                  <Button
                    className="w-full"
                    onClick={addToCart}
                    disabled={
                      !range?.from ||
                      !range.to ||
                      rentalDays < 1 ||
                      selectedRemaining === null ||
                      quantity > selectedRemaining
                    }
                  >
                    Sepete ekle
                  </Button>
                  <p className="text-xs leading-5 text-[var(--muted)]">
                    Sepete eklemek stok ayırmaz. Rezervasyon sepeti onaylarken
                    son uygunluk kontrolünden geçer.
                  </p>
                </div>
              </CustomerGuard>
            ) : null}
          </section>
        </div>
      </main>
    </>
  );
}
