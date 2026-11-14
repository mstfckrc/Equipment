import { AuthCard } from "@/components/auth/auth-card";
import { RegistrationForm } from "@/components/auth/registration-form";

export default async function RegisterPage({ searchParams }: {
  searchParams: Promise<{ accountType?: string | string[] }>;
}) {
  const { accountType } = await searchParams;
  const initialAccountType = accountType === "STORE_OWNER" ? "STORE_OWNER" : "CUSTOMER";
  return (
    <AuthCard title="Hesap oluştur" description="Bireysel hesap aç veya mağazan için başvuru gönder.">
      <RegistrationForm key={initialAccountType} initialAccountType={initialAccountType} />
    </AuthCard>
  );
}
