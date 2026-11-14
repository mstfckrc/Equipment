"use client";

import { ProductImage } from "@/components/storefront/product-image";
import { LinkButton } from "@/components/ui/link-button";
import { Button } from "@/components/ui/button";
import type { StoreProduct, ProductStatus } from "@/types/catalog";
import type { RentalInventorySummary } from "@/types/rental";

export const productStatusLabels: Record<ProductStatus, string> = {
  DRAFT: "Taslak", PUBLISHED: "Yayında", SUSPENDED: "İncelemede", ARCHIVED: "Arşivde",
};
const money = new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" });

export function StoreProductRow({ product, inventory, inventoryState, busy, saving, error, onStatus }: {
  product: StoreProduct; inventory?: RentalInventorySummary; inventoryState: "loading" | "error" | "ready";
  busy: boolean; saving: boolean; error?: string; onStatus: (productId: string, status: ProductStatus) => void;
}) {
  const target = product.status === "DRAFT" || product.status === "ARCHIVED" ? "PUBLISHED"
    : product.status === "PUBLISHED" ? "ARCHIVED" : null;
  const style = product.status === "PUBLISHED" ? "status-pill-success" : product.status === "SUSPENDED" ? "status-pill-warning" : "status-pill-neutral";
  return <li className="surface-card min-w-0 overflow-hidden" data-product-id={product.productId}>
    <div className="grid gap-5 p-5 sm:grid-cols-[112px_minmax(0,1fr)]">
      <div className="w-28 self-start overflow-hidden rounded-xl border border-[var(--border)]"><ProductImage imageUrl={product.imageUrl} name={product.name} /></div>
      <div className="min-w-0 space-y-4">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="min-w-0 space-y-1"><p className="break-words text-xs text-[var(--muted)]">{product.category}</p>
            <h2 className="break-words text-lg font-semibold">{product.name}</h2></div>
          <span className={`rounded-full px-3 py-1 text-xs font-semibold ${style}`}>{productStatusLabels[product.status]}</span>
        </div>
        <div className="flex flex-wrap gap-x-8 gap-y-2 text-sm">
          <p><strong>{money.format(product.dailyPrice)}</strong><span className="text-[var(--muted)]"> / gün</span></p>
          <p className="text-[var(--muted)]">{inventoryState === "loading" ? "Envanter yükleniyor…" : inventoryState === "error" ? "Envanter bilgisi alınamadı" : `${inventory?.totalUnits ?? 0} aktif cihaz · ${inventory?.readyUnits ?? 0} hazır`}</p>
        </div>
        {product.status === "SUSPENDED" ? <p className="notice-warning break-words rounded-lg p-3 text-sm">Durumunu yalnız platform yönetimi değiştirebilir.{product.moderationReason ? ` İnceleme notu: ${product.moderationReason}` : ""}</p> : null}
        {error ? <p role="alert" className="notice-danger rounded-lg p-3 text-sm">{error}</p> : null}
        <div className="flex flex-wrap gap-2 border-t border-[var(--border)] pt-4">
          <LinkButton variant="secondary" href={`/store/products/${encodeURIComponent(product.productId)}`}>İlanı yönet</LinkButton>
          {target ? <Button variant="ghost" disabled={busy} onClick={() => onStatus(product.productId, target)}>
            {saving ? "Güncelleniyor…" : target === "PUBLISHED" ? "Yayına al" : "Arşivle"}</Button> : null}
        </div>
      </div>
    </div>
  </li>;
}
