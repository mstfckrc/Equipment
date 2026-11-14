"use client";

import { useMemo } from "react";
import { AdminEmptyState, AdminFilterButton, AdminLoadState, AdminPageHeader, AdminStatus } from "@/components/admin/admin-ui";
import { LinkButton } from "@/components/ui/link-button";
import { useUrlFilters } from "@/hooks/use-url-filters";
import { Input } from "@/components/ui/input";
import { useAdminStores } from "@/hooks/admin/use-admin-data";
import type { AdminStoreStatus } from "@/types/admin";

type StoreFilter = AdminStoreStatus | "ALL";
const labels: Record<AdminStoreStatus, string> = { ACTIVE: "Faaliyette", SUSPENDED: "Askıda" };
const emptyStores: NonNullable<ReturnType<typeof useAdminStores>["storesQuery"]["data"]> = [];

export function AdminStoresPage() {
  const { storesQuery } = useAdminStores();
  const { searchParams, updateFilters, href } = useUrlFilters();
  const statusParam = searchParams.get("status") ?? "ALL";
  const filter = (["ALL","ACTIVE","SUSPENDED"].includes(statusParam) ? statusParam : "ALL") as StoreFilter;
  const search = (searchParams.get("q") ?? "").slice(0, 120);
  const setFilter = (value: StoreFilter) => updateFilters({ status: value });
  const setSearch = (value: string) => updateFilters({ q: value });
  const stores = storesQuery.data ?? emptyStores;
  const visible = useMemo(() => {
    const needle = search.trim().toLocaleLowerCase("tr-TR");
    return stores.filter((store) => (filter === "ALL" || store.status === filter)
      && (!needle || `${store.storeName} ${store.city} ${store.district} ${store.phone}`.toLocaleLowerCase("tr-TR").includes(needle)));
  }, [filter, search, stores]);

  return (
    <div className="space-y-6">
      <AdminPageHeader title="Mağazalar" description="Operasyonel mağaza profilini ve faaliyette/askıda durumunu yönetin. Askıya alınan mağaza müşterilere gösterilmez." />
      {storesQuery.isPending ? <p role="status" className="py-8 text-sm text-[var(--muted)]">Mağazalar yükleniyor…</p> : null}
      {storesQuery.isError ? <AdminLoadState title="Mağazalar yüklenemedi" message="Admin servisine şu anda ulaşılamıyor." onRetry={() => void storesQuery.refetch()} /> : null}
      {storesQuery.isSuccess ? <>
        <div className="flex flex-wrap gap-2" role="group" aria-label="Mağaza durumuna göre filtrele">
          {([{ value: "ALL", label: "Tümü" }, { value: "ACTIVE", label: "Faaliyette" }, { value: "SUSPENDED", label: "Askıda" }] as const).map((item) =>
            <AdminFilterButton key={item.value} active={filter === item.value} onClick={() => setFilter(item.value)}>
              {item.label} ({item.value === "ALL" ? stores.length : stores.filter((store) => store.status === item.value).length})
            </AdminFilterButton>)}
        </div>
        <Input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Mağaza, şehir veya telefon ara" aria-label="Mağazalarda ara" />
        {!visible.length ? <AdminEmptyState title={stores.length ? "Eşleşen mağaza yok" : "Mağaza bulunamadı"} message="Arama ve durum filtresini değiştirebilirsiniz." /> : (
          <div className="space-y-3">
            {visible.map((store) => {
              return <article key={store.storeId} className="surface-card p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0 max-w-full"><div className="flex flex-wrap items-center gap-2"><h2 className="min-w-0 max-w-full break-words font-semibold">{store.storeName}</h2><AdminStatus tone={store.status === "ACTIVE" ? "success" : "warning"}>{labels[store.status]}</AdminStatus></div><p className="mt-1 break-words text-sm text-[var(--muted)]">{store.district}, {store.city} · {store.phone}</p></div>
                  <LinkButton variant="secondary" href={`/admin/stores/${store.storeId}?returnTo=${encodeURIComponent(href)}`}>Detayı gör</LinkButton>
                </div>
              </article>;
            })}
          </div>
        )}
      </> : null}
    </div>
  );
}
