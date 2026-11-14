"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";
import { ProductListingForm } from "@/components/forms/product-listing-form";
import { PageNavigation } from "@/components/layout/page-navigation";
import { LinkButton } from "@/components/ui/link-button";
import { useCreateStoreProduct } from "@/hooks/catalog/use-store-products";
import { productListingSchema, type ProductListingFormValues } from "@/schemas/product-schemas";
import type { ProductListingRequest } from "@/types/catalog";

export default function NewStoreProductPage() {
  const router = useRouter();
  const createProduct = useCreateStoreProduct();
  const [saveError, setSaveError] = useState<unknown>(null);

  async function create(values: ProductListingFormValues) {
    const parsed = productListingSchema.safeParse(values);
    if (!parsed.success) return false;
    setSaveError(null);
    const request: ProductListingRequest = {
      name: parsed.data.name.trim(),
      category: parsed.data.category.trim(),
      description: parsed.data.description.trim(),
      imageUrl: parsed.data.imageUrl.trim() || null,
      dailyPrice: Number(parsed.data.dailyPrice.replace(",", ".")),
    };
    try {
      const product = await createProduct.mutateAsync(request);
      toast.success("Taslak ilan oluşturuldu.");
      router.replace(`/store/products/${encodeURIComponent(product.productId)}`);
      return true;
    } catch (error) {
      setSaveError(error);
      return false;
    }
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <header className="space-y-2">
        <PageNavigation href="/store/products" label="Ürünlerime dön" items={[{ label: "Ürünlerim", href: "/store/products" }, { label: "Yeni ilan" }]} />
        <p className="pt-2 text-sm font-medium text-[var(--accent-strong)]">Mağaza yönetimi</p>
        <h1 className="catalog-title">Yeni ilan</h1>
        <p className="text-sm leading-6 text-[var(--muted)]">İlan taslak olarak kaydedilir. Hazır olduğunda ürünler listesinden yayına alabilirsin.</p>
      </header>
      <ProductListingForm isSaving={createProduct.isPending} saveError={saveError} onSave={create} />
      <div><LinkButton variant="secondary" href="/store/products">Vazgeç ve ürünlerime dön</LinkButton></div>
    </div>
  );
}
