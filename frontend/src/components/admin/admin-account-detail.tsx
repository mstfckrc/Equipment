"use client";

import { useState } from "react";
import { toast } from "sonner";
import { AdminDetailError, AdminDetailField, AdminDetailNavigation } from "@/components/admin/admin-detail-ui";
import { AdminPageHeader, AdminStatus } from "@/components/admin/admin-ui";
import { Button } from "@/components/ui/button";
import { useAdminAccounts } from "@/hooks/admin/use-admin-data";

export function AdminAccountDetail({ id, returnTo }: { id: string; returnTo?: string }) {
  const { accountsQuery, updateAccount } = useAdminAccounts();
  const account = accountsQuery.data?.find((item) => item.id === id);
  const [confirming, setConfirming] = useState(false);

  async function changeAccess() {
    if (!account || updateAccount.isPending) return;
    try {
      await updateAccount.mutateAsync({ id, request: { enabled: !account.enabled } });
      toast.success(account.enabled ? "Hesap erişimi kapatıldı." : "Hesap erişimi açıldı.");
      setConfirming(false);
    } catch {
      toast.error("Hesap erişimi güncellenemedi. Yeniden deneyin.");
    }
  }

  return <div className="space-y-6">
    <AdminDetailNavigation listPath="/admin/accounts" listLabel="Hesaplar" title="Hesap ayrıntısı" returnTo={returnTo} />
    {accountsQuery.isPending ? <p role="status">Hesap bilgileri yükleniyor…</p> : accountsQuery.isError || !account
      ? <AdminDetailError error={accountsQuery.error} onRetry={() => void accountsQuery.refetch()} />
      : <>
        <AdminPageHeader title={[account.firstName, account.lastName].filter(Boolean).join(" ") || "Hesap ayrıntısı"}
          description="Kişisel bilgileri, kayıt durumunu ve giriş erişimini ayrı ayrı inceleyin." />
        <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(260px,0.8fr)]">
          <section className="surface-card space-y-5 p-6" aria-labelledby="account-detail-title">
            <h2 id="account-detail-title" className="text-lg font-semibold">Hesap bilgileri</h2>
            <dl className="grid gap-5 sm:grid-cols-2">
              <AdminDetailField label="Ad">{account.firstName}</AdminDetailField>
              <AdminDetailField label="Soyad">{account.lastName}</AdminDetailField>
              <AdminDetailField label="E-posta">{account.email}</AdminDetailField>
              <AdminDetailField label="Hesap türü">{account.role === "CUSTOMER" ? "Müşteri" : "Mağaza sahibi"}</AdminDetailField>
              <AdminDetailField label="Kayıt durumu"><AdminStatus tone={account.accountStatus === "ACTIVE" ? "success" : account.accountStatus === "REJECTED" ? "danger" : "warning"}>
                {{ ACTIVE: "Aktif hesap", PENDING: "İncelemede", REJECTED: "Reddedildi" }[account.accountStatus]}</AdminStatus></AdminDetailField>
              <AdminDetailField label="Giriş erişimi"><AdminStatus tone={account.enabled ? "success" : "danger"}>{account.enabled ? "Açık" : "Kapalı"}</AdminStatus></AdminDetailField>
              <AdminDetailField label="Hesap kimliği">{account.id}</AdminDetailField>
            </dl>
          </section>
          <section className="surface-card space-y-4 p-6" aria-labelledby="account-access-title">
            <h2 id="account-access-title" className="text-lg font-semibold">Giriş erişimi</h2>
            <p className="text-sm leading-6 text-[var(--muted)]">Bu işlem kayıt onayını veya mağazanın faaliyet durumunu değiştirmez.</p>
            <Button variant={account.enabled ? "danger" : "secondary"} disabled={updateAccount.isPending} onClick={() => setConfirming(true)}>
              {account.enabled ? "Erişimi kapat" : "Erişimi aç"}</Button>
            {confirming ? <div className="space-y-4 border-t border-[var(--border)] pt-4">
              <p className="text-sm leading-6">{account.enabled
                ? "Yeni giriş ve oturum yenileme engellenecek. Mevcut access token süresi dolana kadar bazı işlemler devam edebilir."
                : "Giriş engeli kaldırılacak. Başvuru incelemesi gibi diğer erişim koşulları korunur."}</p>
              <div className="flex flex-wrap gap-2">
                <Button disabled={updateAccount.isPending} onClick={() => void changeAccess()}>{updateAccount.isPending ? "Kaydediliyor…" : "Değişikliği onayla"}</Button>
                <Button variant="secondary" disabled={updateAccount.isPending} onClick={() => setConfirming(false)}>Vazgeç</Button>
              </div>
            </div> : null}
          </section>
        </div>
      </>}
  </div>;
}
