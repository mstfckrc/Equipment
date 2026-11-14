"use client";

import { useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { AuthMessage } from "@/components/auth/auth-message";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { FormField } from "@/components/forms/form-field";
import { ApiError } from "@/lib/http/api-client";
import { useSession } from "@/providers/session-provider";
import { loginSchema, type LoginFormValues } from "@/schemas/auth-schemas";

function loginErrorMessage(error: unknown) {
  if (!(error instanceof ApiError)) return "Giriş yapılamadı. Bağlantınızı kontrol edip yeniden deneyin.";
  if (error.status === 401) return "E-posta veya şifre hatalı.";
  if (error.status === 403 && error.message.includes("awaiting review")) {
    return "Mağaza başvurunuz incelemede. Onaylandığında giriş yapabilirsiniz.";
  }
  if (error.status === 403 && error.message.includes("not approved")) {
    return "Mağaza başvurusu onaylanmadı. Destek ekibiyle iletişime geçebilirsiniz.";
  }
  if (error.status === 403) return "Hesaba erişim şu anda kapalı.";
  return "Giriş şu anda tamamlanamadı. Biraz sonra yeniden deneyin.";
}

export function LoginForm() {
  const { signIn } = useSession();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });

  async function onSubmit(values: LoginFormValues) {
    setSubmitError(null);
    try {
      await signIn({ email: values.email.trim(), password: values.password });
    } catch (error) {
      setSubmitError(loginErrorMessage(error));
    }
  }

  return (
    <form className="space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
      {submitError ? <AuthMessage>{submitError}</AuthMessage> : null}
      <FormField id="email" label="E-posta" error={errors.email?.message}>
        <Input
          id="email"
          type="email"
          autoComplete="email"
          autoCapitalize="none"
          aria-invalid={Boolean(errors.email)}
          aria-describedby={errors.email ? "email-error" : undefined}
          {...register("email")}
        />
      </FormField>
      <FormField id="password" label="Şifre" error={errors.password?.message}>
        <Input
          id="password"
          type="password"
          autoComplete="current-password"
          aria-invalid={Boolean(errors.password)}
          aria-describedby={errors.password ? "password-error" : undefined}
          {...register("password")}
        />
      </FormField>
      <Button className="w-full" type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Giriş yapılıyor…" : "Giriş yap"}
      </Button>
    </form>
  );
}
