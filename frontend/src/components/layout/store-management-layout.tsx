"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { ArrowUpRight, ClipboardList, PackagePlus, Store } from "lucide-react";
import { RoleGuard } from "@/components/auth/role-guard";
import { SiteBrand } from "@/components/layout/site-brand";
import { AccountMenu } from "@/components/layout/account-menu";
import { PageTransition } from "@/components/layout/page-transition";
import type { ReactNode } from "react";

export function StoreManagementLayout({ children }: { children: ReactNode }) {
  const pathname = usePathname();

  return (
    <RoleGuard role="STORE_OWNER">
      <div className="management-shell">
        <header className="site-header">
          <div className="site-header-inner site-header-wide">
            <SiteBrand href="/store/profile" label="Mağaza" />
            <div className="flex shrink-0 items-center gap-1 sm:gap-2">
              <Link href="/" className="eh-nav-link" aria-label="Siteyi gör"><ArrowUpRight aria-hidden="true" className="size-4" /><span className="hidden sm:inline">Siteyi gör</span></Link>
              <AccountMenu />
            </div>
          </div>
        </header>

        <div className="mx-auto grid w-full max-w-7xl gap-6 px-5 py-6 sm:px-8 lg:grid-cols-[220px_minmax(0,1fr)] lg:py-9">
          <aside className="surface-card h-fit min-w-0 w-full p-3">
            <p className="px-3 pb-2 pt-1 text-xs font-semibold uppercase tracking-wide text-[var(--muted)]">Mağaza</p>
            <nav aria-label="Mağaza yönetimi" className="flex min-w-0 gap-1 overflow-x-auto lg:flex-col">
              <Link href="/store/profile" aria-current={pathname === "/store/profile" ? "page" : undefined}
                className={`eh-nav-link shrink-0 justify-start lg:w-full ${pathname === "/store/profile" ? "bg-[var(--accent-soft)] text-[var(--accent-strong)]" : ""}`}>
                <Store aria-hidden="true" className="size-4" /> Profilim
              </Link>
              <Link href="/store/products" aria-current={pathname.startsWith("/store/products") ? "page" : undefined}
                className={`eh-nav-link shrink-0 justify-start lg:w-full ${pathname.startsWith("/store/products") ? "bg-[var(--accent-soft)] text-[var(--accent-strong)]" : ""}`}>
                <PackagePlus aria-hidden="true" className="size-4" /> Ürünlerim
              </Link>
              <Link href="/store/rentals" aria-current={pathname.startsWith("/store/rentals") ? "page" : undefined}
                className={`eh-nav-link shrink-0 justify-start lg:w-full ${pathname.startsWith("/store/rentals") ? "bg-[var(--accent-soft)] text-[var(--accent-strong)]" : ""}`}>
                <ClipboardList aria-hidden="true" className="size-4" /> Kiralamalar
              </Link>
            </nav>
          </aside>
          <PageTransition key={pathname} animateUpdates={false}><main id="main-content" tabIndex={-1} className="min-w-0">{children}</main></PageTransition>
        </div>
      </div>
    </RoleGuard>
  );
}
