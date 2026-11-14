import { z } from "zod";

export const accountProfileSchema = z.object({
  firstName: z.string().trim().min(1, "Adınızı girin.").max(100, "Ad en fazla 100 karakter olabilir."),
  lastName: z.string().trim().min(1, "Soyadınızı girin.").max(100, "Soyad en fazla 100 karakter olabilir."),
});

export type AccountProfileFormValues = z.infer<typeof accountProfileSchema>;
