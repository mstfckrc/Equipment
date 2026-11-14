"use client";

import { useState } from "react";
import { toast } from "sonner";
import { adminDateTime, AdminDetailError, AdminDetailField, AdminDetailNavigation } from "@/components/admin/admin-detail-ui";
import { AdminPageHeader, AdminStatus } from "@/components/admin/admin-ui";
import { ProductImage } from "@/components/storefront/product-image";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { useAdminProduct, useAdminProducts } from "@/hooks/admin/use-admin-data";

export function AdminProductDetail({ id, returnTo }: { id: string; returnTo?: string }) {
  const productQuery = useAdminProduct(id);
  const { moderateProduct } = useAdminProducts(false);
  const product = productQuery.data;
  const [decision, setDecision] = useState<"DRAFT" | "SUSPENDED" | null>(null);
  const [reason, setReason] = useState("");

  async function submitModeration() {
    if (!product || !decision || moderateProduct.isPending || (decision === "SUSPENDED" && !reason.trim())) return;
    try {
      await moderateProduct.mutateAsync({ id, request: { status: decision, ...(decision === "SUSPENDED" ? { reason: reason.trim() } : {}) } });
      toast.success(decision === "SUSPENDED" ? "İlan askıya alındı." : "İlan taslağa döndürüldü.");
      setDecision(null);
      setReason("");
    } catch {
      toast.error("İlan durumu güncellenemedi. Gerekçeniz korunuyor; yeniden deneyebilirsiniz.");
    }
  }

  return <div className="space-y-6">
    <AdminDetailNavigation listPath="/admin/catalog" listLabel="Ürün moderasyonu" title="Ürün ayrıntısı" returnTo={returnTo} />
    {productQuery.isPending ? <p role="status">Ürün bilgileri yükleniyor…</p> : productQuery.isError || !product
      ? <AdminDetailError error={productQuery.error} onRetry={() => void productQuery.refetch()} />
      : <>
        <AdminPageHeader title={product.name} description="İlan içeriğini inceleyin ve gerektiğinde yayın durumunu yönetin."
          action={<AdminStatus tone={product.status === "PUBLISHED" ? "success" : product.status === "SUSPENDED" ? "danger" : "neutral"}>
            {{ DRAFT: "Taslak", PUBLISHED: "Yayında", SUSPENDED: "Askıda", ARCHIVED: "Arşivde" }[product.status]}</AdminStatus>} />
        <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(260px,0.8fr)]">
          <section className="surface-card min-w-0 space-y-5 overflow-hidden p-6" aria-labelledby="product-detail-title">
            <h2 id="product-detail-title" className="text-lg font-semibold">İlan bilgileri</h2>
            <ProductImage imageUrl={product.imageUrl} name={product.name} />
            <dl className="grid gap-5 sm:grid-cols-2">
              <AdminDetailField label="Kategori">{product.category}</AdminDetailField>
              <AdminDetailField label="Günlük ücret">{new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(product.dailyPrice)}</AdminDetailField>
              <div className="sm:col-span-2"><AdminDetailField label="Açıklama">{product.description}</AdminDetailField></div>
              <AdminDetailField label="Oluşturulma">{adminDateTime(product.createdAt)}</AdminDetailField>
              <AdminDetailField label="Son güncelleme">{adminDateTime(product.updatedAt)}</AdminDetailField>
              {product.moderationReason ? <AdminDetailField label="Moderasyon gerekçesi">{product.moderationReason}</AdminDetailField> : null}
              {product.moderatedBy ? <AdminDetailField label="İşlemi yapan hesap">{product.moderatedBy}</AdminDetailField> : null}
            </dl>
            <LinkButton variant="secondary" href={`/admin/stores/${encodeURIComponent(product.storeId)}`}>İlgili mağazayı gör</LinkButton>
          </section>
          <section className="surface-card space-y-4 p-6" aria-labelledby="moderation-title">
            <h2 id="moderation-title" className="text-lg font-semibold">Yayın yönetimi</h2>
            <p className="text-sm leading-6 text-[var(--muted)]">İlanı askıya alabilir veya mağazanın yeniden düzenlemesi için taslağa döndürebilirsiniz.</p>
            <div className="flex flex-wrap gap-2">
              {product.status !== "SUSPENDED" ? <Button variant="danger" disabled={moderateProduct.isPending} onClick={() => setDecision("SUSPENDED")}>Askıya al</Button> : null}
              {product.status !== "DRAFT" ? <Button variant="secondary" disabled={moderateProduct.isPending} onClick={() => setDecision("DRAFT")}>Taslağa döndür</Button> : null}
            </div>
            {decision ? <div className="space-y-4 border-t border-[var(--border)] pt-4">
              <p className="text-sm leading-6">{decision === "SUSPENDED" ? "İlan müşterilere gösterilmeyecek. En fazla 500 karakterlik gerekçe zorunludur."
                : "İlan taslağa dönecek. Mağaza sahibi düzenleyip yeniden yayınlayabilir."}</p>
              {decision === "SUSPENDED" ? <div className="space-y-2">
                <label htmlFor="moderation-reason" className="text-sm font-medium">Askıya alma gerekçesi</label>
                <textarea id="moderation-reason" maxLength={500} required disabled={moderateProduct.isPending} value={reason}
                  onChange={(event) => setReason(event.target.value)} className="min-h-28 w-full rounded-lg border border-[var(--border)] bg-white p-3 text-sm" />
              </div> : null}
              <div className="flex flex-wrap gap-2">
                <Button disabled={moderateProduct.isPending || decision === "SUSPENDED" && !reason.trim()} onClick={() => void submitModeration()}>
                  {moderateProduct.isPending ? "Kaydediliyor…" : "Değişikliği uygula"}</Button>
                <Button variant="secondary" disabled={moderateProduct.isPending} onClick={() => setDecision(null)}>Vazgeç</Button>
              </div>
            </div> : null}
          </section>
        </div>
      </>}
  </div>;
}
