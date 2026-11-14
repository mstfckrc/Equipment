"use client";

import { useState } from "react";
import { toast } from "sonner";
import { adminDateTime, AdminDetailError, AdminDetailField, AdminDetailNavigation } from "@/components/admin/admin-detail-ui";
import { AdminPageHeader, AdminStatus } from "@/components/admin/admin-ui";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { ApiError } from "@/lib/http/api-client";
import { useAdminRegistration, useAdminRegistrations } from "@/hooks/admin/use-admin-data";
import type { StoreRegistrationDecision } from "@/types/admin";

export function AdminRegistrationDetail({ id, returnTo }: { id: string; returnTo?: string }) {
  const registrationQuery = useAdminRegistration(id);
  const { reviewRegistration } = useAdminRegistrations(false);
  const registration = registrationQuery.data;
  const [decision, setDecision] = useState<StoreRegistrationDecision | null>(null);
  const [reason, setReason] = useState("");

  async function submitDecision() {
    if (!registration || !decision || reviewRegistration.isPending || (decision === "REJECTED" && !reason.trim())) return;
    try {
      await reviewRegistration.mutateAsync({ id, request: { status: decision, ...(decision === "REJECTED" ? { reason: reason.trim() } : {}) } });
      toast.success(decision === "APPROVED" ? "Mağaza başvurusu onaylandı." : "Mağaza başvurusu reddedildi.");
      setDecision(null);
      setReason("");
    } catch (error) {
      toast.error(error instanceof ApiError && error.status === 409 ? "Başvuru başka bir işlemle sonuçlanmış. Güncel durumu yenileyin."
        : "Başvuru güncellenemedi. Gerekçeniz korunuyor; yeniden deneyebilirsiniz.");
      if (error instanceof ApiError && error.status === 409) void registrationQuery.refetch();
    }
  }

  return <div className="space-y-6">
    <AdminDetailNavigation listPath="/admin/store-registrations" listLabel="Başvurular" title="Başvuru ayrıntısı" returnTo={returnTo} />
    {registrationQuery.isPending ? <p role="status">Başvuru bilgileri yükleniyor…</p> : registrationQuery.isError || !registration
      ? <AdminDetailError error={registrationQuery.error} onRetry={() => void registrationQuery.refetch()} />
      : <>
        <AdminPageHeader title={registration.storeName} description="Kayıt sırasında gönderilen başvuru bilgileri; güncel mağaza profili ayrı kayıttır."
          action={<AdminStatus tone={registration.status === "PENDING" ? "warning" : registration.status === "APPROVED" ? "success" : "danger"}>
            {{ PENDING: "Bekliyor", APPROVED: "Onaylandı", REJECTED: "Reddedildi" }[registration.status]}</AdminStatus>} />
        <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(260px,0.8fr)]">
          <section className="surface-card space-y-5 p-6" aria-labelledby="registration-detail-title">
            <h2 id="registration-detail-title" className="text-lg font-semibold">Başvuru bilgileri</h2>
            <dl className="grid gap-5 sm:grid-cols-2">
              <AdminDetailField label="E-posta">{registration.email}</AdminDetailField>
              <AdminDetailField label="Telefon">{registration.phone}</AdminDetailField>
              <AdminDetailField label="İl / ilçe">{registration.city} / {registration.district}</AdminDetailField>
              <AdminDetailField label="Adres">{registration.addressLine}</AdminDetailField>
              <AdminDetailField label="Başvuru tarihi">{adminDateTime(registration.submittedAt)}</AdminDetailField>
              <AdminDetailField label="Değerlendirme tarihi">{adminDateTime(registration.reviewedAt)}</AdminDetailField>
              {registration.reviewedBy ? <AdminDetailField label="Değerlendiren hesap">{registration.reviewedBy}</AdminDetailField> : null}
              {registration.rejectionReason ? <AdminDetailField label="Ret gerekçesi">{registration.rejectionReason}</AdminDetailField> : null}
            </dl>
            <div className="flex flex-wrap gap-2">
              <LinkButton variant="secondary" href={`/admin/accounts/${encodeURIComponent(registration.accountId)}`}>Hesabı gör</LinkButton>
              <LinkButton variant="secondary" href={`/admin/stores/${encodeURIComponent(registration.storeId)}`}>Mağaza profilini gör</LinkButton>
            </div>
          </section>
          <section className="surface-card space-y-4 p-6" aria-labelledby="review-title">
            <h2 id="review-title" className="text-lg font-semibold">Başvuru kararı</h2>
            {registration.status !== "PENDING" ? <p className="text-sm leading-6 text-[var(--muted)]">Başvuru sonuçlanmış. Aynı başvuru yeniden değerlendirilemez.</p>
              : <>
                <p className="text-sm leading-6 text-[var(--muted)]">Onay veya ret kararı mağaza hesabının inceleme durumunu günceller.</p>
                <div className="flex flex-wrap gap-2">
                  <Button disabled={reviewRegistration.isPending} onClick={() => setDecision("APPROVED")}>Onayla</Button>
                  <Button variant="danger" disabled={reviewRegistration.isPending} onClick={() => setDecision("REJECTED")}>Reddet</Button>
                </div>
                {decision ? <div className="space-y-4 border-t border-[var(--border)] pt-4">
                  <p className="text-sm leading-6">{decision === "APPROVED" ? "Başvuru onaylanacak. Diğer erişim koşulları uygunsa mağaza hesabı giriş yapabilecek."
                    : "Başvuru reddedilecek. En fazla 500 karakterlik ret gerekçesi zorunludur."}</p>
                  {decision === "REJECTED" ? <div className="space-y-2">
                    <label htmlFor="rejection-reason" className="text-sm font-medium">Ret gerekçesi</label>
                    <textarea id="rejection-reason" maxLength={500} required disabled={reviewRegistration.isPending} value={reason}
                      onChange={(event) => setReason(event.target.value)} className="min-h-28 w-full rounded-lg border border-[var(--border)] bg-white p-3 text-sm" />
                  </div> : null}
                  <div className="flex flex-wrap gap-2">
                    <Button disabled={reviewRegistration.isPending || decision === "REJECTED" && !reason.trim()} onClick={() => void submitDecision()}>
                      {reviewRegistration.isPending ? "Kaydediliyor…" : "Kararı uygula"}</Button>
                    <Button variant="secondary" disabled={reviewRegistration.isPending} onClick={() => setDecision(null)}>Vazgeç</Button>
                  </div>
                </div> : null}
              </>}
          </section>
        </div>
      </>}
  </div>;
}
