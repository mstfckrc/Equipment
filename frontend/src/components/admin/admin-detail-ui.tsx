import type { ReactNode } from "react";
import { PageNavigation } from "@/components/layout/page-navigation";
import { AdminLoadState } from "@/components/admin/admin-ui";
import { ApiError } from "@/lib/http/api-client";

export function AdminDetailNavigation({ listPath, listLabel, title, returnTo }: {
  listPath: string; listLabel: string; title: string; returnTo?: string;
}) {
  let href = listPath;
  if (returnTo?.startsWith("/") && !returnTo.startsWith("//") && !returnTo.includes("\\")) {
    const url = new URL(returnTo, "http://local");
    if (url.origin === "http://local" && url.pathname === listPath) href = `${url.pathname}${url.search}${url.hash}`;
  }
  return <PageNavigation href={href} label={`${listLabel} listesine dön`}
    items={[{ label: "Yönetim", href: "/admin" }, { label: listLabel, href: listPath }, { label: title }]} />;
}

export function AdminDetailField({ label, children }: { label: string; children: ReactNode }) {
  return <div className="min-w-0 space-y-1"><dt className="text-sm text-[var(--muted)]">{label}</dt>
    <dd className="break-words whitespace-pre-wrap text-sm font-medium [overflow-wrap:anywhere]">{children || "Belirtilmemiş"}</dd></div>;
}

export function AdminDetailError({ error, onRetry }: { error?: unknown; onRetry?: () => void }) {
  const missing = !error || error instanceof ApiError && error.status === 404;
  const denied = error instanceof ApiError && (error.status === 401 || error.status === 403);
  if (missing || denied) return <section role="alert" className="surface-card space-y-2 p-6">
    <h1 className="text-xl font-semibold">{missing ? "Kayıt bulunamadı" : "Erişim sağlanamadı"}</h1>
    <p className="text-sm text-[var(--muted)]">{missing ? "Kayıt mevcut değil veya artık erişilebilir değil. Listeye dönebilirsiniz."
      : "Bu kaydı görüntülemek için oturumunuzun ve yetkinizin geçerli olması gerekiyor."}</p>
  </section>;
  return <AdminLoadState title="Ayrıntılar yüklenemedi" message="Bilgiler şu anda alınamıyor. Yeniden deneyebilirsiniz." onRetry={() => onRetry?.()} />;
}

export const adminDateTime = (value: string | null) => value
  ? new Intl.DateTimeFormat("tr-TR", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value))
  : "Henüz değerlendirilmedi";
