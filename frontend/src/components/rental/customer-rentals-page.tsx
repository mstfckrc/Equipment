"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ClipboardList } from "lucide-react";
import { toast } from "sonner";
import { CustomerGuard } from "@/components/auth/customer-guard";
import { PageNavigation } from "@/components/layout/page-navigation";
import { CustomerRentalGroup, matchesRentalFilter, type RentalFilter } from "@/components/rental/customer-rental-group";
import { RentalActionDialog } from "@/components/rental/rental-action-dialog";
import type { CustomerRentalAction } from "@/components/rental/customer-rental-item";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { ApiError } from "@/lib/http/api-client";
import { useSession } from "@/providers/session-provider";
import { rentalService } from "@/services/rental/rental-service";
import { storeService } from "@/services/store/store-service";
import type { Reservation, ReservationItem } from "@/types/rental";

const filters: Array<{ value: RentalFilter; label: string }> = [
  { value: "ALL", label: "Tümü" }, { value: "ACTIVE", label: "Devam eden" },
  { value: "COMPLETED", label: "Tamamlanan" }, { value: "CANCELLED", label: "İptal edilen" },
];

function CustomerRentalsContent({ checkoutId }: { checkoutId?: string }) {
  const { identity, withAccessToken } = useSession();
  const queryClient = useQueryClient();
  const [filter, setFilter] = useState<RentalFilter>("ALL");
  const [selected, setSelected] = useState<{ item: ReservationItem; status: CustomerRentalAction; triggerId: string } | null>(null);
  const [actionError, setActionError] = useState<{ itemId: string; message: string } | null>(null);
  const queryKey = ["my-reservations", identity?.id];
  const reservationsQuery = useQuery({
    queryKey,
    queryFn: () => withAccessToken((token) => rentalService.getMyReservations(token)),
    enabled: identity?.role === "CUSTOMER",
  });
  const storesQuery = useQuery({
    queryKey: ["public-store-summaries"],
    queryFn: () => storeService.listPublicStores(),
    enabled: identity?.role === "CUSTOMER",
  });
  const updateStatus = useMutation({
    mutationFn: ({ itemId, status }: { itemId: string; status: CustomerRentalAction }) =>
      withAccessToken((token) => rentalService.updateMyReservationItem(itemId, status, token)),
    onSuccess: (updated) => {
      queryClient.setQueryData<Reservation[]>(queryKey, (current) => current?.map((reservation) => ({
        ...reservation, items: reservation.items.map((item) => item.itemId === updated.itemId ? updated : item),
      })));
      setActionError(null);
      setSelected(null);
      toast.success(updated.status === "CANCELLED" ? "Rezervasyon satırı iptal edildi." : "Geri teslim talebin gönderildi.");
      return Promise.all([
        queryClient.invalidateQueries({ queryKey }),
        queryClient.invalidateQueries({ queryKey: ["rental-availability"] }),
      ]);
    },
    onError: (error: Error, variables) => {
      setActionError({ itemId: variables.itemId, message: error instanceof ApiError && error.status === 409
        ? "Bu satırın durumu değişmiş olabilir. Listeyi yenileyip tekrar kontrol et."
        : error instanceof ApiError && error.status === 403 ? "Bu işlem için erişim iznin yok."
          : "İşlem tamamlanamadı. Yeniden deneyebilirsin." });
    },
  });

  const reservations = reservationsQuery.data ?? [];
  const storeNames = new Map((storesQuery.data ?? []).map((store) => [store.storeId, store.storeName]));
  const allItems = reservations.flatMap((reservation) => reservation.items);
  const checkoutGroups = new Map<string, Reservation[]>();
  for (const reservation of reservations) {
    const group = checkoutGroups.get(reservation.checkoutId) ?? [];
    group.push(reservation);
    checkoutGroups.set(reservation.checkoutId, group);
  }
  const groups = Array.from(checkoutGroups).sort((a, b) => Date.parse(b[1][0].createdAt) - Date.parse(a[1][0].createdAt));
  const visibleCount = allItems.filter((item) => matchesRentalFilter(item, filter)).length;
  const checkoutReservations = checkoutId ? reservations.filter((reservation) => reservation.checkoutId === checkoutId) : [];
  const checkoutTotal = checkoutReservations.reduce((sum, reservation) => sum + Math.round(reservation.totalAmount * 100), 0) / 100;

  function openAction(item: ReservationItem, status: CustomerRentalAction, triggerId: string) {
    if (updateStatus.isPending) return;
    setActionError(null);
    setSelected({ item, status, triggerId });
  }

  function confirmAction() {
    if (!selected || updateStatus.isPending) return;
    setActionError(null);
    updateStatus.mutate({ itemId: selected.item.itemId, status: selected.status });
  }

  return <>
    <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-6xl space-y-8 px-5 pb-16 pt-8 sm:px-8 sm:pt-12">
      <PageNavigation href="/shops" label="Mağazalara dön" items={[{ label: "Ana sayfa", href: "/" }, { label: "Kiralamalarım" }]} />
      <header className="space-y-3">
        <p className="home-eyebrow">Hesabım</p>
        <h1 id="rentals-title" tabIndex={-1} className="catalog-title">Kiralamalarım</h1>
        <p className="max-w-2xl text-sm leading-6 text-[var(--muted)]">Rezervasyonlarını mağaza ve ürün bazında takip et. Teslim alma, geri teslim ve kontrol adımlarını buradan görebilirsin.</p>
      </header>
      {reservationsQuery.isSuccess && checkoutReservations.length > 0 ? <section className="notice-success space-y-2 rounded-xl p-5" role="status">
        <h2 className="font-semibold">Rezervasyonun oluşturuldu</h2>
        <p className="text-sm leading-6">Onaylanan rezervasyon tutarı: {new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(checkoutTotal)}. Kiralama detaylarını ve onay belgeni aşağıdan takip edebilirsin.</p>
      </section> : null}
      {reservationsQuery.isPending ? <p role="status" className="text-sm text-[var(--muted)]">Rezervasyonların yükleniyor…</p> : null}
      {reservationsQuery.isError ? <section role="alert" className="surface-card space-y-4 p-6">
        <h2 className="text-lg font-semibold">Kiralamaların yüklenemedi</h2>
        <p className="text-sm text-[var(--muted)]">Şu anda bilgileri alamıyoruz. Yeniden deneyebilirsin.</p>
        <Button variant="secondary" onClick={() => void reservationsQuery.refetch()}>Yeniden dene</Button>
      </section> : null}
      {reservationsQuery.isSuccess && reservations.length === 0 ? <section className="surface-card space-y-5 px-6 py-12 text-center">
        <ClipboardList aria-hidden="true" className="mx-auto size-10 text-[var(--accent-strong)]" strokeWidth={1.4} />
        <h2 className="text-xl font-semibold">Henüz kiralaman yok</h2>
        <p className="text-sm leading-6 text-[var(--muted)]">İhtiyacın olan ekipmanı bul, tarihlerini seç ve ilk rezervasyonunu oluştur.</p>
        <LinkButton href="/shops">Mağazalara göz at</LinkButton>
      </section> : null}
      {reservationsQuery.isSuccess && reservations.length > 0 ? <>
        <div className="flex flex-wrap gap-2" role="group" aria-label="Kiralamaları durumuna göre filtrele">
          {filters.map(({ value, label }) => <Button key={value} variant={filter === value ? "primary" : "secondary"}
            aria-pressed={filter === value} onClick={() => setFilter(value)}>
            {label} ({allItems.filter((item) => matchesRentalFilter(item, value)).length})
          </Button>)}
        </div>
        <p role="status" className="text-xs text-[var(--muted)]">{visibleCount} ürün satırı gösteriliyor. Başlıklardaki tutarlar rezervasyon anındaki toplamdır.</p>
        {storesQuery.isError ? <p className="text-xs text-[var(--muted)]">Mağaza adları şu anda alınamıyor; kiralama kayıtlarını yine de inceleyebilirsin.</p> : null}
        {!visibleCount ? <section className="surface-card space-y-3 p-6">
          <h2 className="text-lg font-semibold">Bu filtrede kiralama yok</h2>
          <p className="text-sm text-[var(--muted)]">Diğer durumları görmek için filtreyi değiştirebilirsin.</p>
          <Button variant="secondary" onClick={() => setFilter("ALL")}>Tümünü göster</Button>
        </section> : <div className="space-y-9">
          {groups.map(([groupId, group]) => <CustomerRentalGroup key={groupId} checkoutId={groupId} reservations={group}
            filter={filter} storeNames={storeNames} busy={updateStatus.isPending} savingItemId={updateStatus.variables?.itemId}
            actionError={actionError} onAction={openAction} />)}
        </div>}
      </> : null}
    </main>
    {selected ? <RentalActionDialog key={selected.item.itemId} item={selected.item} action={selected.status} triggerId={selected.triggerId}
      busy={updateStatus.isPending} error={actionError?.itemId === selected.item.itemId ? actionError.message : undefined}
      onConfirm={confirmAction} onClose={() => { if (!updateStatus.isPending) setSelected(null); }} /> : null}
  </>;
}

export function CustomerRentalsPage({ checkoutId }: { checkoutId?: string }) {
  return <CustomerGuard><CustomerRentalsContent checkoutId={checkoutId} /></CustomerGuard>;
}
