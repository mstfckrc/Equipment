import { z } from "zod";

const requiredText = (max: number, label: string) => z.string()
  .trim()
  .min(1, `${label} alanı zorunludur.`)
  .max(max, `${label} en fazla ${max} karakter olabilir.`);

export const storeProfileSchema = z.object({
  storeName: requiredText(200, "Mağaza adı"),
  phone: requiredText(32, "Telefon"),
  city: requiredText(100, "İl"),
  district: requiredText(100, "İlçe"),
  addressLine: requiredText(500, "Adres"),
});

export type StoreProfileFormValues = z.infer<typeof storeProfileSchema>;
