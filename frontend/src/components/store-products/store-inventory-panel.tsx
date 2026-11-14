"use client";

import { useState, type FormEvent } from "react";
import { toast } from "sonner";
import { useListingInventory } from "@/hooks/rental/use-store-inventory";
import { ApiError } from "@/lib/http/api-client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { InventoryConfirmationDialog } from "@/components/store-products/inventory-confirmation-dialog";
import { StoreInventoryUnitRow, unitLabels } from "@/components/store-products/store-inventory-unit-row";
import type { EquipmentUnit, EquipmentUnitStatus } from "@/types/rental";

type Confirmation = { type: "count"; target: number; current: number; triggerId: string }
  | { type: "unit"; unit: EquipmentUnit; triggerId: string };
const filters: Array<EquipmentUnitStatus | "ALL"> = ["ALL", "READY", "RENTED", "RETURN_PENDING_INSPECTION", "MAINTENANCE", "RETIRED"];

function errorMessage(error: unknown, operation: "count" | "unit") {
  if (error instanceof ApiError && error.status === 409) return operation === "count"
    ? "Adet değiştirilemedi. Kiradaki, bakımdaki cihazlar veya yaklaşan rezervasyonlar azaltmayı engelliyor olabilir."
    : "Durum değiştirilemedi. Cihazın mevcut durumu veya yaklaşan rezervasyonu bu işlemi engelliyor olabilir.";
  if (error instanceof ApiError && (error.status === 401 || error.status === 403)) return "Bu envanteri yönetmek için mağaza sahibi yetkisi gerekiyor.";
  return "İşlem tamamlanamadı. Yeniden deneyebilirsin.";
}

export function StoreInventoryPanel({ listingId }: { listingId: string }) {
  const { inventoryQuery, setTotalUnits, updateUnitStatus } = useListingInventory(listingId);
  const [editedTotal, setEditedTotal] = useState<string | null>(null);
  const [filter, setFilter] = useState<EquipmentUnitStatus | "ALL">("ALL");
  const [confirmation, setConfirmation] = useState<Confirmation | null>(null);
  const [actionError, setActionError] = useState<{ target: string; message: string } | null>(null);
  const inventory = inventoryQuery.data;
  const busy = setTotalUnits.isPending || updateUnitStatus.isPending;
  const total = editedTotal ?? String(inventory?.totalUnits ?? 0);

  async function saveCount(target: number) {
    if (busy) return;
    setActionError(null);
    try {
      await setTotalUnits.mutateAsync(target);
      setEditedTotal(null); setConfirmation(null);
      toast.success("Envanter adedi güncellendi.");
    } catch (error) { setActionError({ target: "count", message: errorMessage(error, "count") }); }
  }

  function submitCount(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    setActionError(null);
    const target = Number(total);
    if (!total.trim() || !Number.isInteger(target) || target < 0 || target > 1000) {
      setActionError({ target: "count", message: "Toplam adet 0 ile 1000 arasında tam sayı olmalı." }); return;
    }
    const current = inventory?.totalUnits ?? 0;
    if (target === current) return;
    if (target < current) setConfirmation({ type: "count", target, current, triggerId: "inventory-save-count" });
    else void saveCount(target);
  }

  async function changeUnit(unit: EquipmentUnit, status: EquipmentUnitStatus) {
    if (busy) return;
    setActionError(null);
    try {
      await updateUnitStatus.mutateAsync({ unitId: unit.unitId, status });
      setConfirmation(null);
      toast.success(`${unit.unitCode} durumu güncellendi.`);
    } catch (error) { setActionError({ target: unit.unitId, message: errorMessage(error, "unit") }); }
  }

  function requestUnitChange(unit: EquipmentUnit, status: EquipmentUnitStatus, triggerId: string) {
    if (busy) return;
    setActionError(null);
    if (status === "RETIRED") setConfirmation({ type: "unit", unit, triggerId });
    else void changeUnit(unit, status);
  }

  if (inventoryQuery.isPending) return <p role="status" className="text-sm text-[var(--muted)]">Envanter yükleniyor…</p>;
  if (inventoryQuery.isError) return <section className="surface-card space-y-3 p-5" role="alert"><h2 className="font-semibold">Envanter alınamadı</h2>
    <p className="text-sm text-[var(--muted)]">İlan bilgilerini düzenlemeye devam edebilirsin.</p>
    <Button variant="secondary" onClick={() => void inventoryQuery.refetch()}>Envanteri yeniden dene</Button></section>;

  const units = inventory?.units ?? [];
  const visible = units.filter((unit) => filter === "ALL" || unit.status === filter);
  const errorTarget = confirmation?.type === "unit" ? confirmation.unit.unitId : "count";
  return <section className="space-y-5" aria-labelledby="inventory-title">
    <header className="space-y-2"><h2 id="inventory-title" tabIndex={-1} className="text-xl font-semibold">Cihaz envanteri</h2>
      <p className="text-sm leading-6 text-[var(--muted)]">Her cihaz ayrı takip edilir. Hazır sayısı tarih bazlı uygunluk değildir; rezervasyonlar ayrıca kontrol edilir.</p></header>
    <div className="surface-card space-y-5 p-5 sm:p-7">
      <dl className="grid grid-cols-2 gap-3 sm:grid-cols-3">
        {[["Aktif toplam", inventory?.totalUnits ?? 0], ["Hazır", inventory?.readyUnits ?? 0], ["Kirada", inventory?.rentedUnits ?? 0],
          ["Kontrol bekliyor", inventory?.pendingInspectionUnits ?? 0], ["Bakımda", inventory?.maintenanceUnits ?? 0], ["Kullanımdan kaldırıldı", inventory?.retiredUnits ?? 0]].map(([label, value]) =>
          <div key={label} className="rounded-xl bg-[var(--page)] p-4"><dt className="text-xs text-[var(--muted)]">{label}</dt><dd className="mt-2 text-xl font-semibold">{value}</dd></div>)}
      </dl>
      <form onSubmit={submitCount} className="space-y-3 border-t border-[var(--border)] pt-5" noValidate>
        <div className="flex flex-wrap items-end gap-3"><div className="w-40 space-y-2"><label htmlFor="totalUnits" className="text-sm font-medium">Toplam aktif cihaz</label>
          <Input id="totalUnits" type="number" min={0} max={1000} step={1} disabled={busy} value={total} onChange={(event) => setEditedTotal(event.target.value)} /></div>
          <Button id="inventory-save-count" type="submit" variant="secondary" disabled={busy || Number(total) === (inventory?.totalUnits ?? 0)}>{setTotalUnits.isPending ? "Kaydediliyor…" : "Adedi kaydet"}</Button></div>
        <p className="text-xs leading-5 text-[var(--muted)]">Artışta yeni hazır cihazlar eklenir. Azaltmada yalnız uygun hazır cihazlar kullanımdan kaldırılır; geçmiş kayıtları silinmez.</p>
        {actionError?.target === "count" ? <p role="alert" className="notice-danger rounded-lg p-3 text-sm">{actionError.message}</p> : null}
      </form>
    </div>
    <div className="surface-card overflow-hidden">
      <header className="space-y-4 border-b border-[var(--border)] p-5"><h3 className="font-semibold">Tekil cihazlar</h3>
        <div className="flex flex-wrap gap-2" role="group" aria-label="Cihaz durumuna göre filtrele">{filters.map((status) =>
          <Button key={status} variant={filter === status ? "primary" : "secondary"} aria-pressed={filter === status} onClick={() => setFilter(status)}>
            {status === "ALL" ? "Tümü" : unitLabels[status]} ({status === "ALL" ? units.length : units.filter((unit) => unit.status === status).length})</Button>)}</div>
      </header>
      {visible.length ? <ul className="divide-y divide-[var(--border)]">{visible.map((unit) => <StoreInventoryUnitRow key={unit.unitId} unit={unit} busy={busy}
        saving={updateUnitStatus.isPending && updateUnitStatus.variables?.unitId === unit.unitId} error={actionError?.target === unit.unitId ? actionError.message : undefined} onChange={requestUnitChange} />)}</ul>
        : <div className="space-y-3 p-5"><p className="text-sm text-[var(--muted)]">{units.length ? "Bu durumda cihaz yok." : "Henüz cihaz eklenmedi. Yukarıdan toplam adedi belirleyebilirsin."}</p>
          {units.length ? <Button variant="secondary" onClick={() => setFilter("ALL")}>Tüm cihazları göster</Button> : null}</div>}
      <p className="border-t border-[var(--border)] p-5 text-xs leading-5 text-[var(--muted)]">Kiradaki ve kontrol bekleyen cihazlar Kiralamalar ekranından yönetilir. Kullanımdan kaldırılan cihaz yeniden etkinleştirilemez.</p>
    </div>
    {confirmation ? <InventoryConfirmationDialog title={confirmation.type === "count" ? "Cihaz adedini azalt" : "Cihazı kullanımdan kaldır"}
      description={confirmation.type === "count" ? `Aktif cihaz adedi ${confirmation.current} yerine ${confirmation.target} olacak. Uygun hazır cihazlar kalıcı olarak kullanımdan kaldırılır. Bu cihazlar yeniden etkinleştirilemez; daha sonra adet artırmak yeni cihaz oluşturur.`
        : `${confirmation.unit.unitCode} cihazı kullanımdan kaldırılacak. Bu işlem geri alınamaz; cihazın geçmiş kayıtları korunur.`}
      triggerId={confirmation.triggerId} busy={busy} error={actionError?.target === errorTarget ? actionError.message : null}
      onClose={() => { if (!busy) setConfirmation(null); }} onConfirm={() => { if (confirmation.type === "count") void saveCount(confirmation.target); else void changeUnit(confirmation.unit, "RETIRED"); }} /> : null}
  </section>;
}
