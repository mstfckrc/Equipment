"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { StoreReservationRow } from "@/components/store-rentals/store-reservation-row";
import { StoreReservationActionDialog, type ReservationAction } from "@/components/store-rentals/store-reservation-action-dialog";
import { buildInspections, emptyInspectionDraft, reservationError, reservationStatusLabels, type InspectionDraft } from "@/components/store-rentals/reservation-helpers";
import { useStoreReservations } from "@/hooks/rental/use-store-reservations";
import type { ReservationItem, ReservationItemStatus, StoreReservationItemStatus } from "@/types/rental";

const filters: Array<ReservationItemStatus | "ALL"> = ["ALL", "CONFIRMED", "PICKED_UP", "RETURN_REQUESTED", "RETURN_RECEIVED", "RETURNED", "CANCELLED"];

export function StoreReservationsPage() {
  const { reservationsQuery, updateReservationItem } = useStoreReservations();
  const [filter, setFilter] = useState<ReservationItemStatus | "ALL">("ALL");
  const [search, setSearch] = useState("");
  const [now, setNow] = useState(() => Date.now());
  const [drafts, setDrafts] = useState<Record<string, Record<string, InspectionDraft>>>({});
  const [selected, setSelected] = useState<ReservationAction | null>(null);
  const [actionError, setActionError] = useState<{ itemId: string; message: string } | null>(null);
  useEffect(() => {
    const timer = window.setInterval(() => setNow(Date.now()), 30_000);
    return () => window.clearInterval(timer);
  }, []);
  const reservations = reservationsQuery.data ?? [];
  const items = reservations.flatMap((reservation) => reservation.items);
  const term = search.trim().toLocaleLowerCase("tr-TR");
  const groups = reservations.map((reservation) => ({ ...reservation, items: reservation.items.filter((item) =>
    (filter === "ALL" || item.status === filter) && `${reservation.reservationId} ${reservation.checkoutId} ${item.listingName} ${item.assignedUnits.map((unit) => unit.unitCode).join(" ")}`.toLocaleLowerCase("tr-TR").includes(term)) }))
    .filter((reservation) => reservation.items.length).sort((a, b) => Date.parse(b.createdAt) - Date.parse(a.createdAt));
  const visibleCount = groups.reduce((sum, group) => sum + group.items.length, 0);
  const busy = updateReservationItem.isPending;

  function openAction(item: ReservationItem, status: StoreReservationItemStatus, triggerId: string) {
    if (busy) return;
    setActionError(null);
    try {
      setSelected({ item, triggerId, request: status === "RETURNED"
        ? { status, unitInspections: buildInspections(item, drafts[item.itemId] ?? {}) } : { status } });
    } catch (error) { setActionError({ itemId: item.itemId, message: error instanceof Error ? error.message : "Kontrol bilgilerini gözden geçir." }); }
  }

  async function confirmAction() {
    if (!selected || busy) return;
    setActionError(null);
    try {
      await updateReservationItem.mutateAsync({ itemId: selected.item.itemId, request: selected.request });
      setDrafts((current) => { const next = { ...current }; delete next[selected.item.itemId]; return next; });
      setSelected(null);
      toast.success("Kiralama satırı güncellendi.");
    } catch (error) { setActionError({ itemId: selected.item.itemId, message: reservationError(error) }); }
  }

  return <div className="mx-auto max-w-5xl space-y-7">
    <header className="flex flex-wrap items-end justify-between gap-4"><div className="space-y-3"><p className="home-eyebrow">Mağaza yönetimi</p>
      <h1 id="store-rentals-title" tabIndex={-1} className="catalog-title">Kiralamalar</h1>
      <p className="max-w-2xl text-sm leading-6 text-[var(--muted)]">Teslim alma, geri teslim ve cihaz kontrolünü her ürün ve tarih aralığı için ayrı yönet.</p></div>
      <Button variant="secondary" disabled={busy || reservationsQuery.isFetching} onClick={() => { setNow(Date.now()); void reservationsQuery.refetch(); }}>Listeyi yenile</Button></header>
    {reservationsQuery.isPending ? <p role="status" className="py-8 text-sm text-[var(--muted)]">Kiralamalar yükleniyor…</p> : null}
    {reservationsQuery.isError ? <section className="surface-card space-y-3 p-6" role="alert"><h2 className="text-lg font-semibold">Kiralamalar yüklenemedi</h2>
      <p className="text-sm text-[var(--muted)]">Mağaza rezervasyonlarına şu anda ulaşılamıyor. Listeyi yenileyerek tekrar deneyebilirsin.</p></section> : null}
    {reservationsQuery.isSuccess ? <>
      <section className="surface-card space-y-4 p-5" aria-label="Kiralama filtreleri">
        <div className="max-w-md space-y-2"><label htmlFor="rental-search" className="text-sm font-medium">Ürün, rezervasyon veya cihaz ara</label>
          <Input id="rental-search" type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Ürün adı, referans veya cihaz kodu" /></div>
        <div role="group" aria-label="Kiralamaları duruma göre filtrele" className="flex flex-wrap gap-2">{filters.map((status) => <Button key={status}
          variant={filter === status ? "primary" : "secondary"} aria-pressed={filter === status} onClick={() => setFilter(status)}>
          {status === "ALL" ? "Tümü" : reservationStatusLabels[status]} ({status === "ALL" ? items.length : items.filter((item) => item.status === status).length})</Button>)}</div>
      </section>
      <p role="status" className="text-xs text-[var(--muted)]">{visibleCount} ürün satırı gösteriliyor. Her satırın işlemi bağımsızdır.</p>
      {groups.length ? <div className="space-y-8">{groups.map((reservation) => <section key={reservation.reservationId} className="space-y-4" aria-labelledby={`reservation-${reservation.reservationId}`}>
        <header className="flex flex-wrap items-start justify-between gap-3 border-b border-[var(--border)] pb-3"><div className="space-y-1">
          <h2 id={`reservation-${reservation.reservationId}`} className="text-base font-semibold">Rezervasyon #{reservation.reservationId.slice(0, 8)}</h2>
          <p className="text-xs text-[var(--muted)]">{new Intl.DateTimeFormat("tr-TR", { dateStyle: "medium", timeStyle: "short", timeZone: "Europe/Istanbul" }).format(new Date(reservation.createdAt))}</p></div>
          <p className="text-xs text-[var(--muted)]">{reservation.items.length} {filter === "ALL" && !term ? "ürün satırı" : "filtreye uyan ürün satırı"}</p></header>
        <ul className="surface-card divide-y divide-[var(--border)] overflow-hidden">{reservation.items.map((item) => <StoreReservationRow key={item.itemId} item={item} now={now}
          busy={busy} drafts={drafts[item.itemId] ?? {}} error={actionError?.itemId === item.itemId ? actionError.message : undefined}
          onDraft={(unitId, changes) => setDrafts((current) => ({ ...current, [item.itemId]: { ...current[item.itemId], [unitId]: { ...(current[item.itemId]?.[unitId] ?? emptyInspectionDraft), ...changes } } }))}
          onAction={(status, triggerId) => openAction(item, status, triggerId)} onReview={(triggerId) => openAction(item, "RETURNED", triggerId)} />)}</ul>
      </section>)}</div> : <section className="surface-card space-y-4 p-7 text-center"><h2 className="text-xl font-semibold">{items.length ? "Bu filtrede kiralama yok" : "Henüz kiralama yok"}</h2>
        <p className="text-sm text-[var(--muted)]">{items.length ? "Aramanı veya durum filtresini değiştirebilirsin." : "Müşterilerin rezervasyonları burada ürün ve tarih aralığına göre görünecek."}</p>
        {items.length ? <Button variant="secondary" onClick={() => { setFilter("ALL"); setSearch(""); }}>Filtreleri temizle</Button> : null}</section>}
    </> : null}
    {selected ? <StoreReservationActionDialog action={selected} busy={busy} error={actionError?.itemId === selected.item.itemId ? actionError.message : undefined}
      onConfirm={() => void confirmAction()} onClose={() => { if (!busy) setSelected(null); }} /> : null}
  </div>;
}
