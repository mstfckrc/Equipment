"use client";

import { usePathname } from "next/navigation";
import type { ReactNode } from "react";
import { StorefrontHeader } from "@/components/layout/storefront-header";
import { SiteFooter } from "@/components/layout/site-footer";
import { BackToTop } from "@/components/layout/back-to-top";
import { PageTransition } from "@/components/layout/page-transition";

export function SiteShell({ children }: { children: ReactNode }) {
  const pathname = usePathname();
  const isManagement = /^\/(admin|store)(\/|$)/.test(pathname);
  const isAuth = pathname === "/login" || pathname === "/register";

  return (
    <div className="site-shell">
      <a className="skip-link" href="#main-content">İçeriğe geç</a>
      {!isManagement && !isAuth ? <StorefrontHeader /> : null}
      {isManagement || isAuth ? children : <PageTransition>{children}</PageTransition>}
      <SiteFooter compact={isManagement || isAuth} />
      <BackToTop />
    </div>
  );
}
