import { z } from "zod";

const email = z.email("Geçerli bir e-posta adresi girin.").max(254, "E-posta adresi çok uzun.");

export const loginSchema = z.object({
  email,
  password: z.string().min(1, "Şifrenizi girin."),
});

export const registrationSchema = z.object({
  accountType: z.enum(["CUSTOMER", "STORE_OWNER"]),
  email,
  password: z.string()
    .min(12, "Şifre en az 12 karakter olmalı.")
    .max(64, "Şifre en fazla 64 karakter olabilir.")
    .refine((value) => new TextEncoder().encode(value).length <= 72, "Şifre çok uzun."),
  firstName: z.string().max(100, "Ad en fazla 100 karakter olabilir."),
  lastName: z.string().max(100, "Soyad en fazla 100 karakter olabilir."),
  storeName: z.string().max(200, "Kurum adı en fazla 200 karakter olabilir."),
  phone: z.string().max(32, "Telefon en fazla 32 karakter olabilir."),
  city: z.string().max(100, "İl en fazla 100 karakter olabilir."),
  district: z.string().max(100, "İlçe en fazla 100 karakter olabilir."),
  addressLine: z.string().max(500, "Adres en fazla 500 karakter olabilir."),
}).superRefine((values, context) => {
  const required = values.accountType === "CUSTOMER"
    ? ["firstName", "lastName"] as const
    : ["storeName", "phone", "city", "district", "addressLine"] as const;

  for (const field of required) {
    if (!values[field].trim()) {
      context.addIssue({
        code: "custom",
        path: [field],
        message: "Bu alan zorunludur.",
      });
    }
  }
});

export type LoginFormValues = z.infer<typeof loginSchema>;
export type RegistrationFormValues = z.infer<typeof registrationSchema>;
