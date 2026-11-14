"use client";

import { useState } from "react";
import { ToggleGroup } from "@base-ui/react/toggle-group";
import { Toggle } from "@base-ui/react/toggle";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { AuthMessage } from "@/components/auth/auth-message";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FormField } from "@/components/forms/form-field";
import { ApiError } from "@/lib/http/api-client";
import { authService } from "@/services/auth/auth-service";
import { registrationSchema, type RegistrationFormValues } from "@/schemas/auth-schemas";
import type { RegisterRequest, RegisterResponse, RegistrationType } from "@/types/auth";

function registrationErrorMessage(error: unknown) {
  if (!(error instanceof ApiError)) return "Kayıt tamamlanamadı. Bağlantınızı kontrol edip yeniden deneyin.";
  if (error.status === 400) return "Bilgileri kontrol edip yeniden deneyin.";
  if (error.status === 409) return "Bu e-posta ile kayıt oluşturulamadı. Giriş yapmayı deneyin.";
  if (error.status === 503) return "Mağaza kaydı şu an tamamlanamadı. Bilgileriniz duruyor; yeniden deneyebilirsiniz.";
  return "Kayıt şu anda tamamlanamadı. Biraz sonra yeniden deneyin.";
}

function registrationRequest(values: RegistrationFormValues): RegisterRequest {
  const common = {
    accountType: values.accountType,
    email: values.email.trim(),
    password: values.password,
  };

  if (values.accountType === "CUSTOMER") {
    return { ...common, firstName: values.firstName.trim(), lastName: values.lastName.trim() };
  }

  return {
    ...common,
    storeName: values.storeName.trim(),
    phone: values.phone.trim(),
    city: values.city.trim(),
    district: values.district.trim(),
    addressLine: values.addressLine.trim(),
  };
}

export function RegistrationForm({ initialAccountType = "CUSTOMER" }: { initialAccountType?: RegistrationType }) {
  const [accountType, setAccountType] = useState<RegistrationType>(initialAccountType);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [result, setResult] = useState<RegisterResponse | null>(null);
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<RegistrationFormValues>({
    resolver: zodResolver(registrationSchema),
    defaultValues: {
      accountType: initialAccountType,
      email: "",
      password: "",
      firstName: "",
      lastName: "",
      storeName: "",
      phone: "",
      city: "",
      district: "",
      addressLine: "",
    },
  });

  function changeAccountType(values: string[]) {
    const value = values[0];
    if (value !== "CUSTOMER" && value !== "STORE_OWNER") return;
    setAccountType(value);
    setValue("accountType", value, { shouldValidate: true });
    setSubmitError(null);
  }

  async function onSubmit(values: RegistrationFormValues) {
    setSubmitError(null);
    try {
      const response = await authService.register(registrationRequest(values));
      setResult(response);
    } catch (error) {
      setSubmitError(registrationErrorMessage(error));
    }
  }

  if (result) {
    const storePending = result.accountType === "STORE_OWNER" && result.status === "PENDING";
    return (
      <div className="space-y-5" role="status">
        <div className={`rounded-xl p-4 ${storePending ? "notice-warning" : "notice-success"}`}>
          <h2 className="font-semibold">{storePending ? "Başvurun incelemede" : "Hesabın oluşturuldu"}</h2>
          <p className="mt-2 text-sm leading-6 text-[var(--muted)]">
            {storePending
              ? "Mağaza hesabın admin onayından sonra kullanılabilir. Sonuçlandığında bu e-posta adresiyle giriş yapabilirsin."
              : "Şimdi e-posta adresin ve şifrenle giriş yapabilirsin."}
          </p>
        </div>
      </div>
    );
  }

  return (
    <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
      {submitError ? <AuthMessage>{submitError}</AuthMessage> : null}
      <div className="auth-mode-switch account-type-switch">
        <span
          aria-hidden="true"
          className={`auth-mode-indicator ${accountType === "STORE_OWNER" ? "auth-mode-indicator-register" : ""}`}
        />
        <ToggleGroup
          value={[accountType]}
          onValueChange={changeAccountType}
          aria-label="Hesap türü"
          className="contents"
        >
          <Toggle
            value="CUSTOMER"
            className="auth-mode-option"
          >
            Bireysel hesap
          </Toggle>
          <Toggle
            value="STORE_OWNER"
            className="auth-mode-option"
          >
            Mağaza hesabı
          </Toggle>
        </ToggleGroup>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        {accountType === "CUSTOMER" ? (
          <>
            <FormField id="firstName" label="Ad" error={errors.firstName?.message}>
              <Input id="firstName" autoComplete="given-name" aria-invalid={Boolean(errors.firstName)} aria-describedby={errors.firstName ? "firstName-error" : undefined} {...register("firstName")} />
            </FormField>
            <FormField id="lastName" label="Soyad" error={errors.lastName?.message}>
              <Input id="lastName" autoComplete="family-name" aria-invalid={Boolean(errors.lastName)} aria-describedby={errors.lastName ? "lastName-error" : undefined} {...register("lastName")} />
            </FormField>
          </>
        ) : (
          <>
            <FormField id="storeName" label="Kurum adı" error={errors.storeName?.message}>
              <Input id="storeName" autoComplete="organization" aria-invalid={Boolean(errors.storeName)} aria-describedby={errors.storeName ? "storeName-error" : undefined} {...register("storeName")} />
            </FormField>
            <FormField id="phone" label="Telefon" error={errors.phone?.message}>
              <Input id="phone" type="tel" autoComplete="tel" aria-invalid={Boolean(errors.phone)} aria-describedby={errors.phone ? "phone-error" : undefined} {...register("phone")} />
            </FormField>
            <FormField id="city" label="İl" error={errors.city?.message}>
              <Input id="city" autoComplete="address-level1" aria-invalid={Boolean(errors.city)} aria-describedby={errors.city ? "city-error" : undefined} {...register("city")} />
            </FormField>
            <FormField id="district" label="İlçe" error={errors.district?.message}>
              <Input id="district" autoComplete="address-level2" aria-invalid={Boolean(errors.district)} aria-describedby={errors.district ? "district-error" : undefined} {...register("district")} />
            </FormField>
            <div className="sm:col-span-2">
              <FormField id="addressLine" label="Adres" error={errors.addressLine?.message}>
                <Input id="addressLine" autoComplete="street-address" aria-invalid={Boolean(errors.addressLine)} aria-describedby={errors.addressLine ? "addressLine-error" : undefined} {...register("addressLine")} />
              </FormField>
            </div>
          </>
        )}
        <div className="sm:col-span-2">
          <FormField id="email" label="E-posta" error={errors.email?.message}>
            <Input id="email" type="email" autoComplete="email" autoCapitalize="none" aria-invalid={Boolean(errors.email)} aria-describedby={errors.email ? "email-error" : undefined} {...register("email")} />
          </FormField>
        </div>
        <div className="sm:col-span-2">
          <FormField id="password" label="Şifre" error={errors.password?.message}>
            <Input id="password" type="password" autoComplete="new-password" aria-invalid={Boolean(errors.password)} aria-describedby={errors.password ? "password-error" : undefined} {...register("password")} />
            <span className="block text-xs leading-5 text-[var(--muted)]">En az 12 karakter.</span>
          </FormField>
        </div>
      </div>
      <Button className="w-full" type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Kaydediliyor…" : accountType === "CUSTOMER" ? "Hesap oluştur" : "Mağaza başvurusu gönder"}
      </Button>
    </form>
  );
}
