"use client";

import { useMemo } from "react";
import { AdminEmptyState, AdminFilterButton, AdminLoadState, AdminPageHeader, AdminStatus } from "@/components/admin/admin-ui";
import { LinkButton } from "@/components/ui/link-button";
import { useUrlFilters } from "@/hooks/use-url-filters";
import { Input } from "@/components/ui/input";
import { useAdminProducts } from "@/hooks/admin/use-admin-data";
import type { AdminProductStatus } from "@/types/admin";

type ProductFilter = AdminProductStatus | "ALL";
const labels: Record<AdminProductStatus, string> = { DRAFT: "Taslak", PUBLISHED: "Yayında", SUSPENDED: "Askıda", ARCHIVED: "Arşivde" };
const tones: Record<AdminProductStatus, "neutral" | "success" | "warning" | "danger"> = {
  DRAFT: "neutral", PUBLISHED: "success", SUSPENDED: "danger", ARCHIVED: "neutral",
};
const emptyProducts: NonNullable<ReturnType<typeof useAdminProducts>["productsQuery"]["data"]> = [];
const money = new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" });

export function AdminCatalogPage() {
  const { productsQuery } = useAdminProducts();
  const { searchParams, updateFilters, href } = useUrlFilters();
  const statusParam = searchParams.get("status") ?? "ALL";
  const filter = (["ALL","DRAFT","PUBLISHED","SUSPENDED","ARCHIVED"].includes(statusParam) ? statusParam : "ALL") as ProductFilter;
  const search = (searchParams.get("q") ?? "").slice(0, 120);
  const setFilter = (value: ProductFilter) => updateFilters({ status: value });
  const setSearch = (value: string) => updateFilters({ q: value });
  const products = productsQuery.data ?? emptyProducts;
  const visible = useMemo(() => {
    const needle = search.trim().toLocaleLowerCase("tr-TR");
    return products.filter((product) => (filter === "ALL" || product.status === filter)
      && (!needle || `${product.name} ${product.category} ${product.storeId}`.toLocaleLowerCase("tr-TR").includes(needle)));
  }, [filter, products, search]);

  return (
    <div className="space-y-6">
      <AdminPageHeader title="Ürün moderasyonu" description="İlanları görüntüleyin. Askıya almak için gerekçe yazın; askıdaki ilanı taslağa döndürerek mağazanın yeniden düzenlemesine izin verin." />
      {productsQuery.isPending ? <p role="status" className="py-8 text-sm text-[var(--muted)]">İlanlar yükleniyor…</p> : null}
      {productsQuery.isError ? <AdminLoadState title="İlanlar yüklenemedi" message="Admin servisine şu anda ulaşılamıyor." onRetry={() => void productsQuery.refetch()} /> : null}
      {productsQuery.isSuccess ? <>
        <div className="flex flex-wrap gap-2" role="group" aria-label="İlan durumuna göre filtrele">
          {([{ value: "ALL", label: "Tümü" }, { value: "DRAFT", label: "Taslak" }, { value: "PUBLISHED", label: "Yayında" }, { value: "SUSPENDED", label: "Askıda" }, { value: "ARCHIVED", label: "Arşivde" }] as const).map((item) =>
            <AdminFilterButton key={item.value} active={filter === item.value} onClick={() => setFilter(item.value)}>
              {item.label} ({item.value === "ALL" ? products.length : products.filter((product) => product.status === item.value).length})
            </AdminFilterButton>)}
        </div>
        <Input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="İlan adı, kategori veya mağaza kimliği ara" aria-label="İlanlarda ara" />
        {!visible.length ? <AdminEmptyState title={products.length ? "Eşleşen ilan yok" : "İlan bulunamadı"} message="Arama ve durum filtresini değiştirebilirsiniz." /> : (
          <div className="space-y-3">
            {visible.map((product) => {
              return <article key={product.productId} className="surface-card p-5">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2"><h2 className="min-w-0 max-w-full break-words font-semibold">{product.name}</h2><AdminStatus tone={tones[product.status]}>{labels[product.status]}</AdminStatus></div>
                    <p className="mt-1 text-sm text-[var(--muted)]">{product.category} · {money.format(product.dailyPrice)} / gün</p>
                    <p className="mt-1 text-xs text-[var(--muted)]" title={product.storeId}>Mağaza: {product.storeId.slice(0, 8)}…</p>
                  </div>
                  <LinkButton variant="secondary" href={`/admin/catalog/${product.productId}?returnTo=${encodeURIComponent(href)}`}>Detayı gör</LinkButton>
                </div>
              </article>;
            })}
          </div>
        )}
      </> : null}
    </div>
  );
}
