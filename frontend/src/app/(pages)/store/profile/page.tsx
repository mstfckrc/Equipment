"use client";

import { useState } from "react";
import { toast } from "sonner";
import { StoreProfileForm } from "@/components/forms/store-profile-form";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { useStoreProfile } from "@/hooks/store/use-store-profile";
import type { StoreProfileFormValues } from "@/schemas/store-schemas";
import type { StoreStatus } from "@/types/store";

const statusLabels: Record<StoreStatus, string> = {
  ACTIVE: "Aktif",
  SUSPENDED: "Askıda",
};

export default function StoreProfilePage() {
  const { profileQuery, updateProfile } = useStoreProfile();
  const [saveError, setSaveError] = useState<unknown>(null);
  const profile = profileQuery.data;

  async function saveProfile(values: StoreProfileFormValues) {
    setSaveError(null);
    try {
      await updateProfile.mutateAsync(values);
      toast.success("Mağaza profili güncellendi.");
      return true;
    } catch (error) {
      setSaveError(error);
      return false;
    }
  }

  if (profileQuery.isPending) {
    return <p className="py-8 text-sm text-[var(--muted)]" role="status">Mağaza profili yükleniyor…</p>;
  }

  if (profileQuery.isError || !profile) {
    return (
      <section className="surface-card max-w-2xl p-6">
        <h1 className="text-xl font-semibold">Profil yüklenemedi</h1>
        <p className="mt-2 text-sm text-[var(--muted)]">Mağaza bilgilerine şu anda ulaşılamıyor.</p>
        <Button className="mt-5" variant="secondary" onClick={() => void profileQuery.refetch()}>Yeniden dene</Button>
      </section>
    );
  }

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <header className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-[var(--accent-strong)]">Mağaza yönetimi</p>
          <h1 className="catalog-title mt-2">Mağaza profilim</h1>
          <p className="mt-2 text-sm leading-6 text-[var(--muted)]">Mağazanızın müşterilere gösterilen temel bilgilerini düzenleyin.</p>
        </div>
        <span className={`rounded-full px-3 py-1.5 text-sm font-semibold ${profile.status === "ACTIVE" ? "status-pill-success" : "status-pill-warning"}`}>
          {statusLabels[profile.status]}
        </span>
      </header>

      <section className="surface-card flex flex-wrap items-center justify-between gap-4 p-5 sm:p-7">
        <div className="min-w-0 space-y-2"><h2 className="break-words text-xl font-semibold">{profile.storeName}</h2>
          <p className="text-sm text-[var(--muted)]">{profile.city} / {profile.district}</p></div>
        {profile.status === "ACTIVE" ? <LinkButton variant="secondary" href={`/shops/${encodeURIComponent(profile.storeId)}`}>Mağaza sayfamı gör</LinkButton> : null}
      </section>
      {profile.status === "SUSPENDED" ? <p className="notice-warning rounded-xl p-4 text-sm">Mağazan askıda olduğu için müşterilere gösterilmiyor. Durum değişikliği için platform yönetimiyle iletişime geçebilirsin.</p> : null}

      <StoreProfileForm
        key={profile.storeId}
        profile={profile}
        isSaving={updateProfile.isPending}
        saveError={saveError}
        onSave={saveProfile}
      />
    </div>
  );
}
