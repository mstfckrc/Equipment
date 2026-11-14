"use client";

import { useMemo } from "react";
import { AdminEmptyState, AdminFilterButton, AdminLoadState, AdminPageHeader, AdminStatus } from "@/components/admin/admin-ui";
import { LinkButton } from "@/components/ui/link-button";
import { useUrlFilters } from "@/hooks/use-url-filters";
import { Input } from "@/components/ui/input";
import { useAdminRegistrations } from "@/hooks/admin/use-admin-data";
import type { StoreRegistrationStatus } from "@/types/admin";

const labels: Record<StoreRegistrationStatus, string> = { PENDING: "Bekliyor", APPROVED: "Onaylandı", REJECTED: "Reddedildi" };
const tones: Record<StoreRegistrationStatus, "warning" | "success" | "danger"> = { PENDING: "warning", APPROVED: "success", REJECTED: "danger" };
const filters: Array<{ value: StoreRegistrationStatus | "ALL"; label: string }> = [
  { value: "ALL", label: "Tümü" }, { value: "PENDING", label: "Bekleyen" },
  { value: "APPROVED", label: "Onaylanan" }, { value: "REJECTED", label: "Reddedilen" },
];
const emptyRegistrations: NonNullable<ReturnType<typeof useAdminRegistrations>["registrationsQuery"]["data"]> = [];
const dateTime = new Intl.DateTimeFormat("tr-TR", { dateStyle: "medium", timeStyle: "short" });

export function AdminRegistrationsPage() {
  const { registrationsQuery } = useAdminRegistrations();
  const { searchParams, updateFilters, href } = useUrlFilters();
  const statusParam = searchParams.get("status") ?? "PENDING";
  const filter = (["ALL","PENDING","APPROVED","REJECTED"].includes(statusParam) ? statusParam : "PENDING") as StoreRegistrationStatus | "ALL";
  const search = (searchParams.get("q") ?? "").slice(0, 120);
  const setFilter = (value: StoreRegistrationStatus | "ALL") => updateFilters({ status: value });
  const setSearch = (value: string) => updateFilters({ q: value });
  const registrations = registrationsQuery.data ?? emptyRegistrations;
  const visible = useMemo(() => {
    const needle = search.trim().toLocaleLowerCase("tr-TR");
    return registrations.filter((item) => (filter === "ALL" || item.status === filter)
      && (!needle || `${item.storeName} ${item.email} ${item.city} ${item.district}`.toLocaleLowerCase("tr-TR").includes(needle)));
  }, [filter, registrations, search]);

  return (
    <div className="space-y-6">
      <AdminPageHeader title="Mağaza başvuruları" description="Başvuru bilgilerini inceleyin. Onay veya ret kararı Auth hesabının inceleme durumunu günceller." />
      {registrationsQuery.isPending ? <p className="py-8 text-sm text-[var(--muted)]" role="status">Başvurular yükleniyor…</p> : null}
      {registrationsQuery.isError ? <AdminLoadState title="Başvurular yüklenemedi" message="Admin servisine şu anda ulaşılamıyor." onRetry={() => void registrationsQuery.refetch()} /> : null}
      {registrationsQuery.isSuccess ? <>
        <div className="flex flex-wrap gap-2" role="group" aria-label="Başvuru durumuna göre filtrele">
          {filters.map((item) => <AdminFilterButton key={item.value} active={filter === item.value} onClick={() => setFilter(item.value)}>
            {item.label} ({item.value === "ALL" ? registrations.length : registrations.filter((x) => x.status === item.value).length})
          </AdminFilterButton>)}
        </div>
        <Input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Mağaza, e-posta veya şehir ara" aria-label="Başvurularda ara" />
        {!visible.length ? <AdminEmptyState title={registrations.length ? "Eşleşen başvuru yok" : "Henüz başvuru yok"} message={registrations.length ? "Arama veya durum filtresini değiştirin." : "Yeni mağaza başvuruları burada görünür."} /> : (
          <div className="space-y-3">
            {visible.map((item) => {
              return <article key={item.id} className="surface-card p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2"><h2 className="min-w-0 max-w-full break-words font-semibold">{item.storeName}</h2><AdminStatus tone={tones[item.status]}>{labels[item.status]}</AdminStatus></div>
                    <p className="mt-1 break-all text-sm text-[var(--muted)]">{item.email}</p>
                    <p className="mt-1 text-sm text-[var(--muted)]">{item.district}, {item.city} · {dateTime.format(new Date(item.submittedAt))}</p>
                  </div>
                  <LinkButton variant="secondary" href={`/admin/store-registrations/${item.id}?returnTo=${encodeURIComponent(href)}`}>Detayı gör</LinkButton>
                </div>
              </article>;
            })}
          </div>
        )}
      </> : null}
    </div>
  );
}
