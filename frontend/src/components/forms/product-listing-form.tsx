"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { ApiError } from "@/lib/http/api-client";
import { productListingSchema, type ProductListingFormValues } from "@/schemas/product-schemas";
import type { StoreProduct } from "@/types/catalog";
import { FormField } from "@/components/forms/form-field";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

function errorText(error: unknown) {
  if (!(error instanceof ApiError)) return error ? "İşlem tamamlanamadı. Yeniden deneyin." : null;
  if (error.status === 401 || error.status === 403) return "Bu işlem için mağaza hesabı yetkisi gerekiyor.";
  if (error.status === 409) return "İlanın durumu değişmiş olabilir. Sayfayı yenileyip tekrar deneyin.";
  return error.message;
}

export function ProductListingForm({
  product,
  isSaving,
  saveError,
  onSave,
}: {
  product?: StoreProduct;
  isSaving: boolean;
  saveError: unknown;
  onSave: (values: ProductListingFormValues) => Promise<boolean>;
}) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty, isSubmitting },
  } = useForm<ProductListingFormValues>({
    resolver: zodResolver(productListingSchema),
    defaultValues: {
      name: product?.name ?? "",
      category: product?.category ?? "",
      description: product?.description ?? "",
      imageUrl: product?.imageUrl ?? "",
      dailyPrice: product?.dailyPrice.toFixed(2) ?? "",
    },
  });

  const message = errorText(saveError);

  return (
    <form className="surface-card space-y-6 p-5 sm:p-7" onSubmit={handleSubmit(async (values) => {
      if (await onSave(values)) reset(values);
    })} noValidate>
      {message ? <div className="notice-danger rounded-lg px-4 py-3 text-sm" role="alert">{message}</div> : null}
      <fieldset disabled={isSaving || isSubmitting} className="space-y-5">
      <legend className="mb-2 text-lg font-semibold">Ürün bilgileri</legend>
      <div className="grid gap-5 sm:grid-cols-2">
        <FormField id="name" label="Ürün adı" error={errors.name?.message}>
          <Input id="name" maxLength={200} aria-invalid={Boolean(errors.name)} {...register("name")} />
        </FormField>
        <FormField id="category" label="Kategori" error={errors.category?.message}>
          <Input id="category" maxLength={100} aria-invalid={Boolean(errors.category)} {...register("category")} />
        </FormField>
        <FormField id="dailyPrice" label="Günlük kira bedeli (₺)" error={errors.dailyPrice?.message}>
          <Input id="dailyPrice" inputMode="decimal" placeholder="250,00" aria-invalid={Boolean(errors.dailyPrice)} {...register("dailyPrice")} />
        </FormField>
        <FormField id="imageUrl" label="Görsel URL’si (isteğe bağlı)" error={errors.imageUrl?.message}>
          <Input id="imageUrl" type="url" maxLength={1000} placeholder="https://…" aria-invalid={Boolean(errors.imageUrl)} {...register("imageUrl")} />
        </FormField>
        <div className="sm:col-span-2">
          <FormField id="description" label="Açıklama" error={errors.description?.message}>
            <textarea
              id="description"
              rows={5}
              maxLength={2000}
              aria-invalid={Boolean(errors.description)}
              className="min-h-32 w-full rounded-lg border border-[var(--border)] bg-white px-3 py-2.5 text-sm text-[var(--foreground)] focus:border-[var(--focus)] focus:outline-none"
              {...register("description")}
            />
          </FormField>
        </div>
      </div>
      </fieldset>
      <div className="flex flex-wrap items-center justify-between gap-3 border-t border-[var(--border)] pt-5">
        <p className="text-sm text-[var(--muted)]" aria-live="polite">
          {product ? isDirty ? "Kaydedilmemiş değişiklikler var." : "İlan bilgileri güncel." : "Yeni ilan önce taslak olarak oluşturulur."}
        </p>
        <Button type="submit" disabled={(product ? !isDirty : false) || isSubmitting || isSaving}>
          {isSubmitting || isSaving ? "Kaydediliyor…" : product ? "Değişiklikleri kaydet" : "Taslak oluştur"}
        </Button>
      </div>
    </form>
  );
}
