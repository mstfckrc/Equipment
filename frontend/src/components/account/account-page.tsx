"use client";

import { UserRound } from "lucide-react";
import { CustomerGuard } from "@/components/auth/customer-guard";
import { AccountProfileForm } from "@/components/forms/account-profile-form";
import { PageNavigation } from "@/components/layout/page-navigation";
import { Button } from "@/components/ui/button";
import { useAccountProfile } from "@/hooks/auth/use-account-profile";
import { ApiError } from "@/lib/http/api-client";

function AccountContent() {
  const { profileQuery, updateProfile } = useAccountProfile();

  return (
    <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-3xl space-y-8 px-5 pb-16 pt-8 sm:px-8 sm:pt-12">
      <PageNavigation href="/" label="Ana sayfaya dön" items={[{ label: "Ana sayfa", href: "/" }, { label: "Bilgilerim" }]} />
      <header className="space-y-3">
        <p className="home-eyebrow">Hesabım</p>
        <h1 className="catalog-title">Bilgilerim</h1>
        <p className="text-sm leading-6 text-[var(--muted)]">Hesap bilgilerini görüntüle, adını ve soyadını güncel tut.</p>
      </header>
      <section className="surface-card space-y-6 p-5 sm:p-8" aria-labelledby="account-details-title">
        <div className="flex items-center gap-3">
          <UserRound aria-hidden="true" className="size-5 text-[var(--accent)]" />
          <h2 id="account-details-title" className="text-lg font-semibold">Kişisel bilgiler</h2>
        </div>
        {profileQuery.isPending ? <p role="status" className="text-sm text-[var(--muted)]">Bilgileriniz yükleniyor…</p> : null}
        {profileQuery.isError ? <div role="alert" className="space-y-4">
          <p className="text-sm text-[var(--muted)]">{profileQuery.error instanceof ApiError && profileQuery.error.status === 403
            ? "Bu işleme erişim izniniz yok." : "Bilgileriniz şu anda alınamadı. Yeniden deneyebilirsiniz."}</p>
          <Button variant="secondary" onClick={() => void profileQuery.refetch()}>Yeniden dene</Button>
        </div> : null}
        {profileQuery.isSuccess ? <AccountProfileForm profile={profileQuery.data} onSave={(values) => updateProfile.mutateAsync(values)} /> : null}
      </section>
    </main>
  );
}

export function AccountPage() {
  return <CustomerGuard><AccountContent /></CustomerGuard>;
}
