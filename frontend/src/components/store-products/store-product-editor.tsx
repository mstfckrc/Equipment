"use client";

import { useState } from "react";
import { toast } from "sonner";
import { ProductListingForm } from "@/components/forms/product-listing-form";
import { StoreInventoryPanel } from "@/components/store-products/store-inventory-panel";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { PageNavigation } from "@/components/layout/page-navigation";
import { useStoreProduct } from "@/hooks/catalog/use-store-products";
import { productListingSchema, type ProductListingFormValues } from "@/schemas/product-schemas";

function requestFrom(values: ProductListingFormValues) {
  return {
    name: values.name.trim(),
    category: values.category.trim(),
    description: values.description.trim(),
    imageUrl: values.imageUrl.trim() || null,
    dailyPrice: Number(values.dailyPrice.replace(",", ".")),
  };
}

export function StoreProductEditor({ productId }: { productId: string }) {
  const { query, updateProduct } = useStoreProduct(productId);
  const [saveError, setSaveError] = useState<unknown>(null);
  const product = query.data;

  async function save(values: ProductListingFormValues) {
    setSaveError(null);
    const parsed = productListingSchema.safeParse(values);
    if (!parsed.success) return false;
    try {
      await updateProduct.mutateAsync(requestFrom(parsed.data));
      toast.success("İlan bilgileri güncellendi.");
      return true;
    } catch (error) {
      setSaveError(error);
      return false;
    }
  }

  if (query.isPending) return <p role="status" className="py-8 text-sm text-[var(--muted)]">İlan yükleniyor…</p>;
  if (query.isError || !product) {
    return <section className="surface-card max-w-2xl space-y-4 p-6" role="alert">
      <h1 className="text-xl font-semibold">İlan açılamadı</h1>
      <p className="text-sm text-[var(--muted)]">İlan bulunamadı veya mağaza hesabınızda görüntüleme yetkisi yok.</p>
      <div className="flex flex-wrap gap-2"><Button variant="secondary" onClick={() => void query.refetch()}>Yeniden dene</Button><LinkButton variant="secondary" href="/store/products">İlanlara dön</LinkButton></div>
    </section>;
  }

  return (
    <div className="mx-auto max-w-4xl space-y-7">
      <header className="space-y-2">
        <PageNavigation href="/store/products" label="Ürünlerime dön" items={[{ label: "Ürünlerim", href: "/store/products" }, { label: "İlan düzenleme" }]} />
        <p className="pt-2 text-sm font-medium text-[var(--accent-strong)]">İlan düzenleme</p>
        <h1 className="catalog-title break-words">{product.name}</h1>
      </header>
      {product.status === "SUSPENDED" ? <p className="notice-warning rounded-lg p-4 text-sm">Bu ilan yönetici incelemesinde. İlan durumunu yalnız yönetici değiştirebilir.{product.moderationReason ? ` Not: ${product.moderationReason}` : ""}</p> : null}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold tracking-tight">İlan bilgileri</h2>
        <ProductListingForm key={product.productId} product={product} isSaving={updateProduct.isPending} saveError={saveError} onSave={save} />
      </section>
      <StoreInventoryPanel listingId={product.productId} />
    </div>
  );
}
