"use client";

import { useState } from "react";
import { toast } from "sonner";
import { AdminDetailError, AdminDetailField, AdminDetailNavigation } from "@/components/admin/admin-detail-ui";
import { AdminPageHeader, AdminStatus } from "@/components/admin/admin-ui";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { useAdminStore, useAdminStores } from "@/hooks/admin/use-admin-data";

export function AdminStoreDetail({ id, returnTo }: { id: string; returnTo?: string }) {
  const storeQuery = useAdminStore(id);
  const { updateStatus } = useAdminStores(false);
  const store = storeQuery.data;
  const [confirming, setConfirming] = useState(false);

  async function changeStatus() {
    if (!store || updateStatus.isPending) return;
    const status = store.status === "ACTIVE" ? "SUSPENDED" : "ACTIVE";
    try {
      await updateStatus.mutateAsync({ id, request: { status } });
      toast.success(status === "ACTIVE" ? "Mağaza faaliyete alındı." : "Mağaza askıya alındı.");
      setConfirming(false);
    } catch {
      toast.error("Mağaza durumu güncellenemedi. Yeniden deneyin.");
    }
  }

  return <div className="space-y-6">
    <AdminDetailNavigation listPath="/admin/stores" listLabel="Mağazalar" title="Mağaza ayrıntısı" returnTo={returnTo} />
    {storeQuery.isPending ? <p role="status">Mağaza bilgileri yükleniyor…</p> : storeQuery.isError || !store
      ? <AdminDetailError error={storeQuery.error} onRetry={() => void storeQuery.refetch()} />
      : <>
        <AdminPageHeader title={store.storeName} description="Operasyonel mağaza profilini ve faaliyet durumunu inceleyin."
          action={<AdminStatus tone={store.status === "ACTIVE" ? "success" : "warning"}>{store.status === "ACTIVE" ? "Faaliyette" : "Askıda"}</AdminStatus>} />
        <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(260px,0.8fr)]">
          <section className="surface-card space-y-5 p-6" aria-labelledby="store-detail-title">
            <h2 id="store-detail-title" className="text-lg font-semibold">Mağaza bilgileri</h2>
            <dl className="grid gap-5 sm:grid-cols-2">
              <AdminDetailField label="Telefon">{store.phone}</AdminDetailField>
              <AdminDetailField label="İl / ilçe">{store.city} / {store.district}</AdminDetailField>
              <AdminDetailField label="Adres">{store.addressLine}</AdminDetailField>
              <AdminDetailField label="Mağaza kimliği">{store.storeId}</AdminDetailField>
            </dl>
            <LinkButton variant="secondary" href={`/admin/accounts/${encodeURIComponent(store.accountId)}`}>Mağaza hesabını gör</LinkButton>
          </section>
          <section className="surface-card space-y-4 p-6" aria-labelledby="store-operation-title">
            <h2 id="store-operation-title" className="text-lg font-semibold">Faaliyet durumu</h2>
            <p className="text-sm leading-6 text-[var(--muted)]">Mağazanın faaliyet durumu, hesabın başvuru onayından ve giriş erişiminden ayrıdır.</p>
            <Button variant={store.status === "ACTIVE" ? "danger" : "secondary"} disabled={updateStatus.isPending} onClick={() => setConfirming(true)}>
              {store.status === "ACTIVE" ? "Mağazayı askıya al" : "Faaliyete al"}</Button>
            {confirming ? <div className="space-y-4 border-t border-[var(--border)] pt-4">
              <p className="text-sm leading-6">{store.status === "ACTIVE" ? "Mağaza ve ürünleri müşteri keşfinden kaldırılacak. Mevcut kayıtlar silinmez."
                : "Faaliyet engeli kaldırılacak. Hesap onayı ve ürün yayın durumu gibi diğer görünürlük koşulları korunur."}</p>
              <div className="flex flex-wrap gap-2">
                <Button disabled={updateStatus.isPending} onClick={() => void changeStatus()}>{updateStatus.isPending ? "Kaydediliyor…" : "Değişikliği onayla"}</Button>
                <Button variant="secondary" disabled={updateStatus.isPending} onClick={() => setConfirming(false)}>Vazgeç</Button>
              </div>
            </div> : null}
          </section>
        </div>
      </>}
  </div>;
}
