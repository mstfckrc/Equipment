"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { toast } from "sonner";
import { FormField } from "@/components/forms/form-field";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/http/api-client";
import { accountProfileSchema, type AccountProfileFormValues } from "@/schemas/account-schema";
import type { AccountProfile } from "@/types/auth";

export function AccountProfileForm({ profile, onSave }: {
  profile: AccountProfile;
  onSave: (values: AccountProfileFormValues) => Promise<AccountProfile>;
}) {
  const [saveError, setSaveError] = useState<string | null>(null);
  const { register, handleSubmit, reset, formState: { errors, isDirty, isSubmitting } } = useForm<AccountProfileFormValues>({
    resolver: zodResolver(accountProfileSchema),
    defaultValues: { firstName: profile.firstName ?? "", lastName: profile.lastName ?? "" },
  });

  async function submit(values: AccountProfileFormValues) {
    setSaveError(null);
    try {
      const saved = await onSave(values);
      reset({ firstName: saved.firstName ?? "", lastName: saved.lastName ?? "" });
      toast.success("Bilgileriniz güncellendi.");
    } catch (error) {
      setSaveError(error instanceof ApiError && error.status === 403
        ? "Bu işleme erişim izniniz yok."
        : error instanceof ApiError && error.status === 400
          ? "Ad ve soyad alanlarını kontrol edip yeniden deneyin."
          : "Bilgileriniz kaydedilemedi. Biraz sonra yeniden deneyin.");
    }
  }

  return (
    <form onSubmit={handleSubmit(submit)} noValidate className="space-y-6">
      {saveError ? <p role="alert" className="text-sm text-[var(--danger)]">{saveError}</p> : null}
      <FormField id="account-email" label="E-posta">
        <Input id="account-email" type="email" value={profile.email} readOnly autoComplete="email"
          aria-describedby="account-email-help" className="bg-[var(--background)]" />
        <p id="account-email-help" className="text-xs leading-5 text-[var(--muted)]">Giriş yaptığınız e-posta adresi bu ekrandan değiştirilemez.</p>
      </FormField>
      <fieldset disabled={isSubmitting} className="grid gap-5 sm:grid-cols-2">
        <FormField id="account-first-name" label="Ad" error={errors.firstName?.message}>
          <Input id="account-first-name" autoComplete="given-name" maxLength={100}
            aria-invalid={Boolean(errors.firstName)} aria-describedby={errors.firstName ? "account-first-name-error" : undefined}
            {...register("firstName")} />
        </FormField>
        <FormField id="account-last-name" label="Soyad" error={errors.lastName?.message}>
          <Input id="account-last-name" autoComplete="family-name" maxLength={100}
            aria-invalid={Boolean(errors.lastName)} aria-describedby={errors.lastName ? "account-last-name-error" : undefined}
            {...register("lastName")} />
        </FormField>
      </fieldset>
      <div className="flex flex-wrap items-center gap-3 border-t border-[var(--border)] pt-5">
        <Button type="submit" disabled={!isDirty || isSubmitting}>
          {isSubmitting ? "Kaydediliyor…" : "Değişiklikleri kaydet"}
        </Button>
        <p className="text-xs text-[var(--muted)]">Bilgileriniz yalnız kendi hesabınız için güncellenir.</p>
      </div>
    </form>
  );
}
