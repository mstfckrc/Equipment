"use client";

import { useMemo } from "react";
import { AdminEmptyState, AdminFilterButton, AdminLoadState, AdminPageHeader, AdminStatus } from "@/components/admin/admin-ui";
import { LinkButton } from "@/components/ui/link-button";
import { useUrlFilters } from "@/hooks/use-url-filters";
import { Input } from "@/components/ui/input";
import { useAdminAccounts } from "@/hooks/admin/use-admin-data";
import type { AccountRole, AccountStatus } from "@/types/admin";

type RoleFilter = AccountRole | "ALL";
type AccessFilter = "ALL" | "ENABLED" | "DISABLED";
const roleLabels: Record<AccountRole, string> = { CUSTOMER: "Müşteri", STORE_OWNER: "Mağaza sahibi" };
const accountLabels: Record<AccountStatus, string> = { ACTIVE: "Aktif hesap", PENDING: "İncelemede", REJECTED: "Reddedildi" };
const accountTones: Record<AccountStatus, "success" | "warning" | "danger"> = { ACTIVE: "success", PENDING: "warning", REJECTED: "danger" };
const emptyAccounts: NonNullable<ReturnType<typeof useAdminAccounts>["accountsQuery"]["data"]> = [];

export function AdminAccountsPage() {
  const { accountsQuery } = useAdminAccounts();
  const { searchParams, updateFilters, href } = useUrlFilters();
  const roleParam = searchParams.get("role");
  const role: RoleFilter = roleParam === "CUSTOMER" || roleParam === "STORE_OWNER" ? roleParam : "ALL";
  const accessParam = searchParams.get("access");
  const access: AccessFilter = accessParam === "ENABLED" || accessParam === "DISABLED" ? accessParam : "ALL";
  const search = (searchParams.get("q") ?? "").slice(0, 120);
  const setRole = (value: RoleFilter) => updateFilters({ role: value });
  const setAccess = (value: AccessFilter) => updateFilters({ access: value });
  const setSearch = (value: string) => updateFilters({ q: value });
  const accounts = accountsQuery.data ?? emptyAccounts;
  const visible = useMemo(() => {
    const needle = search.trim().toLocaleLowerCase("tr-TR");
    return accounts.filter((item) => (role === "ALL" || item.role === role)
      && (access === "ALL" || (access === "ENABLED" ? item.enabled : !item.enabled))
      && (!needle || `${item.email} ${item.firstName ?? ""} ${item.lastName ?? ""}`.toLocaleLowerCase("tr-TR").includes(needle)));
  }, [access, accounts, role, search]);

  return (
    <div className="space-y-6">
      <AdminPageHeader title="Hesaplar" description="Müşteri ve mağaza hesabı türlerini, kayıt durumlarını ve giriş erişimini ayrı ayrı görüntüleyin." />
      {accountsQuery.isPending ? <p role="status" className="py-8 text-sm text-[var(--muted)]">Hesaplar yükleniyor…</p> : null}
      {accountsQuery.isError ? <AdminLoadState title="Hesaplar yüklenemedi" message="Admin servisine şu anda ulaşılamıyor." onRetry={() => void accountsQuery.refetch()} /> : null}
      {accountsQuery.isSuccess ? <>
        <div className="flex flex-wrap gap-2" role="group" aria-label="Hesap türüne göre filtrele">
          {([{ value: "ALL", label: "Tüm roller" }, { value: "CUSTOMER", label: "Müşteriler" }, { value: "STORE_OWNER", label: "Mağaza sahipleri" }] as const).map((item) =>
            <AdminFilterButton key={item.value} active={role === item.value} onClick={() => setRole(item.value)}>{item.label}</AdminFilterButton>)}
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <Input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="E-posta veya isim ara" aria-label="Hesaplarda ara" className="sm:max-w-sm" />
          <label className="flex items-center gap-2 text-sm"><span className="text-[var(--muted)]">Giriş erişimi</span>
            <select value={access} onChange={(event) => setAccess(event.target.value as AccessFilter)} className="min-h-11 rounded-lg border border-[var(--border)] bg-white px-3 text-sm">
              <option value="ALL">Tümü</option><option value="ENABLED">Açık</option><option value="DISABLED">Kapalı</option>
            </select>
          </label>
        </div>
        {!visible.length ? <AdminEmptyState title={accounts.length ? "Eşleşen hesap yok" : "Hesap bulunamadı"} message="Arama ve filtreleri değiştirebilirsiniz." /> : (
          <div className="space-y-3">
            {visible.map((account) => <article key={account.id} className="surface-card space-y-4 p-5">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="min-w-0 space-y-2">
                  <div className="flex flex-wrap items-center gap-2"><h2 className="break-all font-semibold">{account.email}</h2><AdminStatus tone={accountTones[account.accountStatus]}>{accountLabels[account.accountStatus]}</AdminStatus></div>
                  <p className="text-sm text-[var(--muted)]">{[account.firstName, account.lastName].filter(Boolean).join(" ") || "İsim belirtilmemiş"} · {roleLabels[account.role]}</p>
                  <AdminStatus tone={account.enabled ? "success" : "danger"}>Giriş {account.enabled ? "açık" : "kapalı"}</AdminStatus>
                </div>
                <LinkButton variant="secondary" href={`/admin/accounts/${account.id}?returnTo=${encodeURIComponent(href)}`}>Detayı gör</LinkButton>
              </div>
            </article>)}
          </div>
        )}
      </> : null}
    </div>
  );
}
