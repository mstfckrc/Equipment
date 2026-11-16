"use client";

import { useEffect, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { Check, FileText, LoaderCircle } from "lucide-react";
import { CustomerGuard } from "@/components/auth/customer-guard";
import { PageNavigation } from "@/components/layout/page-navigation";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { ApiError } from "@/lib/http/api-client";
import { useSession } from "@/providers/session-provider";
import { rentalService } from "@/services/rental/rental-service";
import type { ConfirmationContent, Reservation } from "@/types/rental";

const dateTime = new Intl.DateTimeFormat("tr-TR", {
  dateStyle: "long", timeStyle: "short", timeZone: "Europe/Istanbul",
});

function ConfirmationDocument({ documentId, content }: { documentId: string; content: ConfirmationContent }) {
  const money = new Intl.NumberFormat("tr-TR", { style: "currency", currency: content.currencyCode });

  return <article className="surface-card overflow-hidden" aria-labelledby="confirmation-document-title">
    <header className="space-y-5 border-b border-[var(--border)] bg-[var(--page)] p-5 sm:p-8">
      <div className="flex items-center gap-2 text-sm font-medium text-[var(--accent-strong)]">
        <Check className="size-4" aria-hidden="true" /> Rezervasyon onayı
      </div>
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="space-y-2"><h2 id="confirmation-document-title" className="text-xl font-semibold">Kiralama onay belgesi</h2>
          <p className="text-sm text-[var(--muted)]">Rezervasyon #{content.checkoutId.slice(0, 8)}</p></div>
        <div className="space-y-1 sm:text-right"><p className="text-xs text-[var(--muted)]">Onaylanan toplam</p>
          <p className="text-2xl font-semibold">{money.format(content.totalAmount)}</p></div>
      </div>
      <dl className="grid gap-4 text-sm sm:grid-cols-2">
        <div><dt className="text-xs text-[var(--muted)]">Rezervasyon tarihi</dt><dd className="mt-1">{dateTime.format(new Date(content.reservedAt))}</dd></div>
        <div><dt className="text-xs text-[var(--muted)]">Belgenin hazırlanma tarihi</dt><dd className="mt-1">{dateTime.format(new Date(content.preparedAt))}</dd></div>
      </dl>
    </header>
    <div className="divide-y divide-[var(--border)]">
      {content.stores.map((store) => <section key={store.reservationId} className="space-y-5 p-5 sm:p-8">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="min-w-0 space-y-1"><h3 className="break-words font-semibold">{store.storeName ?? `Mağaza #${store.storeId.slice(0, 8)}`}</h3>
            <p className="text-xs text-[var(--muted)]">Mağaza rezervasyonu #{store.reservationId.slice(0, 8)}</p></div>
          <p className="text-sm font-medium">{money.format(store.totalAmount)}</p>
        </div>
        <ul className="space-y-4">
          {store.items.map((item) => <li key={item.itemId} className="rounded-xl border border-[var(--border)] p-4 sm:p-5">
            <div className="flex flex-wrap justify-between gap-3">
              <h4 className="min-w-0 break-words font-medium">{item.listingName}</h4>
              <span className="text-sm font-semibold">{money.format(item.totalAmount)}</span>
            </div>
            <p className="mt-2 text-sm text-[var(--muted)]">{item.rentalDays} gün × {item.quantity} adet × {money.format(item.dailyPrice)}</p>
            <dl className="mt-4 grid gap-3 text-sm sm:grid-cols-2">
              <div><dt className="text-xs text-[var(--muted)]">Teslim alma</dt><dd className="mt-1">{dateTime.format(new Date(item.pickupAt))}</dd></div>
              <div><dt className="text-xs text-[var(--muted)]">Geri teslim</dt><dd className="mt-1">{dateTime.format(new Date(item.returnAt))}</dd></div>
            </dl>
          </li>)}
        </ul>
      </section>)}
    </div>
    <footer className="space-y-3 border-t border-[var(--border)] p-5 sm:p-8">
      <p className="text-sm leading-6 text-[var(--muted)]">Bu belge rezervasyon onayındaki tarihleri ve tutarları gösterir. Güncel kiralama durumunu ve işlemlerini Kiralamalarım ekranından takip edebilirsin. Saatler Türkiye saatidir.</p>
      <p className="break-all text-xs text-[var(--muted)]">Belge referansı: {documentId}</p>
    </footer>
  </article>;
}

function ConfirmationPageContent({ checkoutId }: { checkoutId: string }) {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const [pollUntil, setPollUntil] = useState(() => Date.now() + 60000);
  const query = useQuery({
    queryKey: ["rental-confirmation", identity?.id, checkoutId],
    queryFn: () => withAccessToken((token) => rentalService.getConfirmation(checkoutId, token)),
    enabled: identity?.role === "CUSTOMER",
    retry: false,
    staleTime: (current) => current.state.data?.status === "READY" ? Infinity : 0,
    refetchOnWindowFocus: false,
    refetchIntervalInBackground: false,
    refetchInterval: (current) => current.state.status !== "error" && current.state.data?.status === "PENDING"
      && Date.now() < pollUntil ? 3000 : false,
  });
  const confirmation = query.data;
  const missing = query.error instanceof ApiError && [400, 404].includes(query.error.status);

  useEffect(() => {
    if (!confirmation || !identity) return;
    queryClient.setQueryData<Reservation[]>(["my-reservations", identity.id], (current) => current?.map((reservation) =>
      reservation.checkoutId === checkoutId ? { ...reservation, confirmationStatus: confirmation.status } : reservation));
  }, [confirmation, identity, checkoutId, queryClient]);

  function refresh() {
    setPollUntil(Date.now() + 60000);
    void query.refetch();
  }

  return <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-4xl space-y-7 px-5 pb-16 pt-8 sm:px-8 sm:pt-12">
    <PageNavigation href="/rentals" label="Kiralamalarıma dön" items={[
      { label: "Ana sayfa", href: "/" }, { label: "Kiralamalarım", href: "/rentals" }, { label: "Onay belgesi" },
    ]} />
    <header className="space-y-3"><p className="home-eyebrow">Kiralama belgelerin</p>
      <h1 className="catalog-title">Onay belgesi</h1>
      <p className="text-sm leading-6 text-[var(--muted)]">Rezervasyonunda onayladığın ekipmanlar, tarihler ve tutarlar bir arada.</p>
    </header>
    {query.isPending ? <p role="status" className="text-sm text-[var(--muted)]">Belge bilgileri yükleniyor…</p> : null}
    {query.isError ? <section role="alert" className="surface-card space-y-4 p-6">
      <h2 className="font-semibold">{missing ? "Onay belgesi bulunamadı" : "Belge bilgileri alınamadı"}</h2>
      <p className="text-sm text-[var(--muted)]">{missing ? "Kendi rezervasyonlarının belgelerine Kiralamalarım ekranından ulaşabilirsin." : "Bağlantını kontrol edip yeniden deneyebilirsin."}</p>
      {missing ? <LinkButton variant="secondary" href="/rentals">Kiralamalarıma git</LinkButton> :
        <Button variant="secondary" disabled={query.isFetching} onClick={refresh}>Yeniden dene</Button>}
    </section> : null}
    {!query.isError && confirmation?.status === "PENDING" ? <section className="surface-card space-y-4 p-6" role="status">
      <LoaderCircle className="size-6 motion-safe:animate-spin text-[var(--accent-strong)]" aria-hidden="true" />
      <h2 className="font-semibold">Onay belgen hazırlanıyor</h2>
      <p className="text-sm leading-6 text-[var(--muted)]">Rezervasyonun oluşturuldu. Bu sayfayı açık tutman gerekmiyor; hazır olduğunda belgene Kiralamalarım ekranından ulaşabilirsin.</p>
      <Button variant="secondary" disabled={query.isFetching} onClick={refresh}>Durumu yenile</Button>
    </section> : null}
    {!query.isError && confirmation?.status === "FAILED" ? <section className="surface-card space-y-4 p-6" role="status">
      <FileText className="size-6 text-[var(--muted)]" aria-hidden="true" />
      <h2 className="font-semibold">Belge şu anda hazırlanamadı</h2>
      <p className="text-sm leading-6 text-[var(--muted)]">Rezervasyonun bu durumdan etkilenmedi. Kiralama bilgilerini Kiralamalarım ekranından inceleyebilirsin.</p>
      <Button variant="secondary" disabled={query.isFetching} onClick={refresh}>Durumu yenile</Button>
      <LinkButton variant="secondary" href="/rentals">Kiralamalarıma dön</LinkButton>
    </section> : null}
    {!query.isError && confirmation?.status === "READY" ? confirmation.contentVersion === 1 && confirmation.content && confirmation.documentId
      ? <ConfirmationDocument documentId={confirmation.documentId} content={confirmation.content} />
      : <p role="alert" className="surface-card p-6 text-sm">Bu belge biçimi şu anda görüntülenemiyor. Kiralama bilgilerine Kiralamalarım ekranından ulaşabilirsin.</p> : null}
  </main>;
}

export function RentalConfirmationPage({ checkoutId }: { checkoutId: string }) {
  return <CustomerGuard><ConfirmationPageContent key={checkoutId} checkoutId={checkoutId} /></CustomerGuard>;
}
