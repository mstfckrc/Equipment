"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Home, Menu as MenuIcon, RotateCw, ShoppingBag, Store } from "lucide-react";
import { Menu, MenuLink, MenuPopup } from "@/components/ui/menu";
import { AccountMenu } from "@/components/layout/account-menu";
import { SiteBrand } from "@/components/layout/site-brand";
import { useSession } from "@/providers/session-provider";
import { useCart } from "@/providers/cart-provider";

export function StorefrontHeader() {
  const pathname = usePathname();
  const { status, identity, retry } = useSession();
  const { lines } = useCart();
  const anonymous = status === "anonymous";
  const links = [{ href: "/", label: "Ana sayfa", icon: Home }, { href: "/shops", label: "Mağazalar", icon: Store }];

  return (
    <header className="site-header">
      <div className="site-header-inner">
        <SiteBrand />
        <nav className="hidden items-center gap-1 md:flex" aria-label="Ana gezinme">
          {links.map(({ href, label }) => <Link key={href} className="eh-nav-link" href={href}
            aria-current={(href === "/" ? pathname === "/" : pathname.startsWith(href)) ? "page" : undefined}>{label}</Link>)}
        </nav>
        <div className="flex shrink-0 items-center gap-1 sm:gap-2">
          {anonymous ? <>
            <Link className="eh-nav-link" href="/login">Giriş yap</Link>
            <Link className="eh-nav-primary hidden sm:inline-flex" href="/register">Kayıt ol</Link>
          </> : null}
          {status === "authenticated" && identity ? <>
            {identity.role === "CUSTOMER" ? <Link className="eh-nav-link cart-nav-link" href="/cart" aria-label={`Sepet, ${lines.length} ürün satırı`} aria-current={pathname === "/cart" ? "page" : undefined}>
              <ShoppingBag aria-hidden="true" className="size-4" /><span className="hidden lg:inline">Sepet</span>
              {lines.length ? <span className="eh-count-badge">{lines.length}</span> : null}
            </Link> : null}
            <AccountMenu />
          </> : null}
          {status === "checking" ? <span className="session-placeholder" role="status"><span className="sr-only">Oturum kontrol ediliyor…</span></span> : null}
          {status === "error" ? <button type="button" className="eh-nav-secondary" aria-label="Oturum kontrolünü yeniden dene" onClick={retry}>
            <RotateCw aria-hidden="true" className="size-4" /><span className="hidden sm:inline">Yeniden dene</span>
          </button> : null}
          <div className="md:hidden">
            <Menu.Root>
              <Menu.Trigger className="eh-nav-link mobile-menu-trigger" aria-label="Gezinme menüsü"><MenuIcon aria-hidden="true" className="size-5" /></Menu.Trigger>
              <MenuPopup>
                {links.map(({ href, label, icon: Icon }) => <MenuLink key={href} href={href} current={pathname === href}><Icon aria-hidden="true" className="size-4" />{label}</MenuLink>)}
                {anonymous ? <><Menu.Separator className="site-menu-separator" /><MenuLink href="/login">Giriş yap</MenuLink><MenuLink href="/register">Kayıt ol</MenuLink></> : null}
              </MenuPopup>
            </Menu.Root>
          </div>
        </div>
      </div>
    </header>
  );
}
