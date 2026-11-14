"use client";

import Link from "next/link";
import { SiteBrand } from "@/components/layout/site-brand";
import { routeForRole } from "@/lib/auth/identity";
import { useSession } from "@/providers/session-provider";

export function SiteFooter({ compact = false }: { compact?: boolean }) {
  const { status, identity, retry } = useSession();
  const year = new Date().getFullYear();
  let accountLinks = [{ href: "/register", label: "Hesap oluştur" }, { href: "/login", label: "Giriş yap" }];
  if (status === "authenticated" && identity) {
    accountLinks = identity.role === "CUSTOMER"
      ? [{ href: "/rentals", label: "Kiralamalarım" }, { href: "/cart", label: "Sepetim" }]
      : [{ href: routeForRole(identity.role), label: identity.role === "STORE_OWNER" ? "Mağaza yönetimi" : "Yönetim paneli" }];
  }

  if (compact) {
    return (
      <footer className="site-footer site-footer-compact">
        <div className="site-footer-inner flex flex-wrap items-center justify-between gap-3 py-5">
          <Link href="/" className="font-semibold text-[var(--foreground)]">EquipmentHub</Link>
          <span>© {year} EquipmentHub</span>
        </div>
      </footer>
    );
  }

  return (
    <footer className="site-footer">
      <div className="site-footer-inner">
        <div className="grid gap-8 py-10 sm:grid-cols-[minmax(0,2fr)_1fr_1fr] sm:py-12">
          <div className="space-y-3">
            <SiteBrand />
            <p className="max-w-xs text-sm leading-6 text-[var(--muted)]">Ekipmanını bul, tarihini seç ve mağazadan teslim al.</p>
          </div>
          <nav aria-label="Footer keşif bağlantıları" className="space-y-3">
            <h2 className="site-footer-title">Keşfet</h2>
            <ul className="space-y-2">
              <li><Link className="site-footer-link" href="/">Ana sayfa</Link></li>
              <li><Link className="site-footer-link" href="/shops">Mağazalar</Link></li>
            </ul>
          </nav>
          <nav aria-label="Footer hesap bağlantıları" className="space-y-3">
            <h2 className="site-footer-title">Hesap</h2>
            <ul className="space-y-2">
              {status === "checking" ? <li className="text-sm text-[var(--muted)]">Oturum kontrol ediliyor…</li>
                : status === "error" ? <li className="space-y-1 text-sm text-[var(--muted)]">
                  <p>Oturum doğrulanamadı.</p>
                  <button type="button" className="site-footer-link" onClick={retry}>Yeniden dene</button>
                </li>
                : accountLinks.map(({ href, label }) => (
                  <li key={href}><Link className="site-footer-link" href={href}>{label}</Link></li>
                ))}
            </ul>
          </nav>
        </div>
        <div className="border-t border-[var(--border)] py-5 text-xs text-[var(--muted)]">© {year} EquipmentHub</div>
      </div>
    </footer>
  );
}
