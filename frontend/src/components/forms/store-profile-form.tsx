"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { ApiError } from "@/lib/http/api-client";
import { storeProfileSchema, type StoreProfileFormValues } from "@/schemas/store-schemas";
import type { StoreProfile } from "@/types/store";
import { FormField } from "@/components/forms/form-field";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

const fields = [
  { name: "storeName", label: "Mağaza adı", autoComplete: "organization", maxLength: 200 },
  { name: "phone", label: "Telefon", autoComplete: "tel", maxLength: 32, type: "tel" },
  { name: "city", label: "İl", autoComplete: "address-level1", maxLength: 100 },
  { name: "district", label: "İlçe", autoComplete: "address-level2", maxLength: 100 },
  { name: "addressLine", label: "Adres", autoComplete: "street-address", maxLength: 500 },
] as const;

export function StoreProfileForm({
  profile,
  isSaving,
  saveError,
  onSave,
}: {
  profile: StoreProfile;
  isSaving: boolean;
  saveError: unknown;
  onSave: (values: StoreProfileFormValues) => Promise<boolean>;
}) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty, isSubmitting },
  } = useForm<StoreProfileFormValues>({
    resolver: zodResolver(storeProfileSchema),
    defaultValues: {
      storeName: profile.storeName,
      phone: profile.phone,
      city: profile.city,
      district: profile.district,
      addressLine: profile.addressLine,
    },
  });

  const errorMessage = saveError instanceof ApiError
    ? saveError.message
    : saveError ? "Profil kaydedilemedi. Bağlantınızı kontrol edip yeniden deneyin." : null;

  return (
    <form className="surface-card space-y-6 p-5 sm:p-7" onSubmit={handleSubmit(async (values) => {
      if (await onSave(values)) reset(values);
    })} noValidate>
      {errorMessage ? <div className="notice-danger rounded-lg px-4 py-3 text-sm" role="alert">{errorMessage}</div> : null}

      <fieldset disabled={isSaving || isSubmitting} className="space-y-5">
      <legend className="mb-2 text-lg font-semibold">İletişim ve adres</legend>
      <p className="text-sm leading-6 text-[var(--muted)]">Bu bilgiler mağaza sayfanda ve müşterilerin teslim noktasında görünür.</p>
      <div className="grid gap-5 sm:grid-cols-2">
        {fields.map((field) => (
          <div key={field.name} className={field.name === "addressLine" ? "sm:col-span-2" : undefined}>
            <FormField id={field.name} label={field.label} error={errors[field.name]?.message}>
              <Input
                id={field.name}
                autoComplete={field.autoComplete}
                maxLength={field.maxLength}
                type={"type" in field ? field.type : "text"}
                aria-invalid={Boolean(errors[field.name])}
                aria-describedby={errors[field.name] ? `${field.name}-error` : undefined}
                {...register(field.name)}
              />
            </FormField>
          </div>
        ))}
      </div>
      </fieldset>

      <div className="flex flex-wrap items-center justify-between gap-3 border-t border-[var(--border)] pt-5">
        <p className="text-sm text-[var(--muted)]" aria-live="polite">
          {isDirty ? "Kaydedilmemiş değişiklikler var." : "Profil bilgileri güncel."}
        </p>
        <Button type="submit" disabled={!isDirty || isSubmitting || isSaving}>
          {isSubmitting || isSaving ? "Kaydediliyor…" : "Değişiklikleri kaydet"}
        </Button>
      </div>
    </form>
  );
}
