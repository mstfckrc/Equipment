"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { ArrowUpRight, ClipboardList, LayoutDashboard, PackageSearch, Store, Users } from "lucide-react";
import { RoleGuard } from "@/components/auth/role-guard";
import { SiteBrand } from "@/components/layout/site-brand";
import { AccountMenu } from "@/components/layout/account-menu";
import { PageTransition } from "@/components/layout/page-transition";
import type { ReactNode } from "react";

const links = [
  { href: "/admin", label: "Genel bakış", icon: LayoutDashboard },
  { href: "/admin/store-registrations", label: "Mağaza başvuruları", icon: ClipboardList },
  { href: "/admin/accounts", label: "Hesaplar", icon: Users },
  { href: "/admin/stores", label: "Mağazalar", icon: Store },
  { href: "/admin/catalog", label: "Ürün moderasyonu", icon: PackageSearch },
];

export function AdminManagementLayout({ children }: { children: ReactNode }) {
  const pathname = usePathname();

  return (
    <RoleGuard role="PLATFORM_ADMIN">
      <div className="management-shell">
        <header className="site-header">
          <div className="site-header-inner site-header-wide">
            <SiteBrand href="/admin" label="Yönetim" />
            <div className="flex items-center gap-2">
              <Link href="/" className="eh-nav-link" aria-label="Siteyi gör"><ArrowUpRight aria-hidden="true" className="size-4" /><span className="hidden sm:inline">Siteyi gör</span></Link>
              <AccountMenu />
            </div>
          </div>
        </header>

        <div className="mx-auto grid w-full max-w-7xl gap-5 px-5 py-6 sm:px-8 lg:grid-cols-[230px_minmax(0,1fr)] lg:py-9">
          <aside className="surface-card h-fit min-w-0 w-full p-3">
            <p className="px-3 pb-2 pt-1 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">Yönetim alanları</p>
            <nav aria-label="Yönetim alanları" className="flex min-w-0 max-w-full gap-1 overflow-x-auto lg:flex-col">
              {links.map(({ href, label, icon: Icon }) => {
                const active = href === "/admin" ? pathname === href : pathname.startsWith(href);
                return (
                  <Link key={href} href={href} aria-current={active ? "page" : undefined}
                    className={`eh-nav-link shrink-0 justify-start lg:w-full ${active ? "bg-[var(--accent-soft)] text-[var(--accent-strong)]" : ""}`}>
                    <Icon aria-hidden="true" className="size-4" /> {label}
                  </Link>
                );
              })}
            </nav>
          </aside>
          <PageTransition key={pathname} animateUpdates={false}><main id="main-content" tabIndex={-1} className="min-w-0">{children}</main></PageTransition>
        </div>
      </div>
    </RoleGuard>
  );
}
