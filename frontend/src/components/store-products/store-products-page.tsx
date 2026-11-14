"use client";

import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { LinkButton } from "@/components/ui/link-button";
import { StoreProductRow, productStatusLabels } from "@/components/store-products/store-product-row";
import { ApiError } from "@/lib/http/api-client";
import { useStoreProducts } from "@/hooks/catalog/use-store-products";
import type { ProductStatus } from "@/types/catalog";

const filters: Array<ProductStatus | "ALL"> = ["ALL", "DRAFT", "PUBLISHED", "SUSPENDED", "ARCHIVED"];

export function StoreProductsPage() {
  const [filter, setFilter] = useState<ProductStatus | "ALL">("ALL");
  const [search, setSearch] = useState("");
  const [actionError, setActionError] = useState<{ id: string; message: string } | null>(null);
  const { productsQuery, inventoryQuery, updateStatus } = useStoreProducts();
  const products = productsQuery.data ?? [];
  const inventoryByListing = new Map((inventoryQuery.data ?? []).map((item) => [item.listingId, item]));
  const term = search.trim().toLocaleLowerCase("tr-TR");
  const visibleProducts = products.filter((product) => (filter === "ALL" || product.status === filter)
    && `${product.name} ${product.category}`.toLocaleLowerCase("tr-TR").includes(term));

  async function changeStatus(productId: string, status: ProductStatus) {
    if (updateStatus.isPending) return;
    setActionError(null);
    try {
      await updateStatus.mutateAsync({ productId, status });
      toast.success(`İlan durumu “${productStatusLabels[status]}” olarak güncellendi.`);
    } catch (error) {
      setActionError({ id: productId, message: error instanceof ApiError && error.status === 409
        ? "İlanın durumu değişmiş olabilir. Listeyi yenileyip tekrar dene."
        : error instanceof ApiError && error.status === 403 ? "Bu ilanın durumunu değiştirme iznin yok."
          : "İlan durumu güncellenemedi. Yeniden deneyebilirsin." });
    }
  }

  return <div className="mx-auto max-w-5xl space-y-7">
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="space-y-3"><p className="home-eyebrow">Mağaza yönetimi</p><h1 className="catalog-title">Ürünlerim</h1>
        <p className="text-sm leading-6 text-[var(--muted)]">İlanlarını, günlük fiyatlarını ve cihaz envanterini tek yerden yönet.</p></div>
      <LinkButton href="/store/products/new">Yeni ilan</LinkButton>
    </header>
    {productsQuery.isPending ? <p role="status" className="py-8 text-sm text-[var(--muted)]">İlanlar yükleniyor…</p> : null}
    {productsQuery.isError ? <section className="surface-card space-y-4 p-6" role="alert">
      <h2 className="text-lg font-semibold">İlanlar yüklenemedi</h2><p className="text-sm text-[var(--muted)]">Mağaza bilgilerine şu anda ulaşılamıyor.</p>
      <Button variant="secondary" onClick={() => void productsQuery.refetch()}>Yeniden dene</Button></section> : null}
    {productsQuery.isSuccess ? <>
      <section className="surface-card space-y-4 p-5" aria-label="Ürün filtreleri">
        <div className="max-w-md space-y-2"><label htmlFor="store-product-search" className="text-sm font-medium">Ürün veya kategori ara</label>
          <Input id="store-product-search" type="search" placeholder="Örneğin matkap" value={search} onChange={(event) => setSearch(event.target.value)} /></div>
        <div className="flex flex-wrap gap-2" role="group" aria-label="İlan durumuna göre filtrele">
          {filters.map((status) => <Button key={status} variant={filter === status ? "primary" : "secondary"} aria-pressed={filter === status} onClick={() => setFilter(status)}>
            {status === "ALL" ? "Tümü" : productStatusLabels[status]} ({status === "ALL" ? products.length : products.filter((item) => item.status === status).length})</Button>)}
        </div>
      </section>
      {inventoryQuery.isError ? <div className="notice-warning flex flex-wrap items-center justify-between gap-3 rounded-xl p-4">
        <p className="text-sm">Envanter özeti alınamadı. İlanlarını yönetmeye devam edebilirsin.</p>
        <Button variant="secondary" onClick={() => void inventoryQuery.refetch()}>Envanteri yeniden dene</Button></div> : null}
      <p role="status" className="text-xs text-[var(--muted)]">{visibleProducts.length} ilan gösteriliyor. Hazır cihaz sayısı, tarih bazlı kiralama uygunluğu değildir.</p>
      {visibleProducts.length ? <ul className="space-y-4">{visibleProducts.map((product) => <StoreProductRow key={product.productId} product={product}
        inventory={inventoryByListing.get(product.productId)} inventoryState={inventoryQuery.isError ? "error" : inventoryQuery.isPending ? "loading" : "ready"}
        busy={updateStatus.isPending} saving={updateStatus.isPending && updateStatus.variables?.productId === product.productId}
        error={actionError?.id === product.productId ? actionError.message : undefined} onStatus={(id, status) => void changeStatus(id, status)} />)}</ul>
        : <section className="surface-card space-y-4 p-7 text-center"><h2 className="text-xl font-semibold">{products.length ? "Aramana uygun ilan yok" : "İlk ilanını oluştur"}</h2>
          <p className="text-sm text-[var(--muted)]">{products.length ? "Aramanı veya durum filtresini değiştirebilirsin." : "Ürün bilgilerini taslak olarak kaydet, ardından cihazlarını ekle."}</p>
          {products.length ? <Button variant="secondary" onClick={() => { setSearch(""); setFilter("ALL"); }}>Filtreleri temizle</Button> : <LinkButton href="/store/products/new">Yeni ilan</LinkButton>}</section>}
    </> : null}
  </div>;
}
