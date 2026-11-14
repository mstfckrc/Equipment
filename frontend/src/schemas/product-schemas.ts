import { z } from "zod";

const requiredText = (max: number, label: string) => z.string()
  .trim()
  .min(1, `${label} alanı zorunludur.`)
  .max(max, `${label} en fazla ${max} karakter olabilir.`);

export const productListingSchema = z.object({
  name: requiredText(200, "Ürün adı"),
  category: requiredText(100, "Kategori"),
  description: requiredText(2000, "Açıklama"),
  imageUrl: z.string().trim().max(1000, "Görsel adresi en fazla 1000 karakter olabilir.")
    .refine((value) => !value || z.url({ protocol: /^https?$/ }).safeParse(value).success, "Geçerli bir HTTP veya HTTPS görsel URL'si girin."),
  dailyPrice: z.string().trim()
    .min(1, "Günlük kira bedeli alanı zorunludur.")
    .regex(/^\d{1,8}([.,]\d{1,2})?$/, "Fiyatı en fazla iki ondalık basamakla girin.")
    .refine((value) => Number(value.replace(",", ".")) >= 0.01, "Günlük fiyat en az 0,01 TL olmalıdır."),
});

export type ProductListingFormValues = z.infer<typeof productListingSchema>;
