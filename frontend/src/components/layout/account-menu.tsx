"use client";

import { useState } from "react";
import { usePathname } from "next/navigation";
import { ChevronDown, ClipboardList, LayoutDashboard, LogOut, Store, UserRound } from "lucide-react";
import { toast } from "sonner";
import { Menu, MenuLink, MenuPopup } from "@/components/ui/menu";
import { useSession } from "@/providers/session-provider";

export function AccountMenu() {
  const { identity, signOut } = useSession();
  const pathname = usePathname();
  const [isSigningOut, setIsSigningOut] = useState(false);

  async function handleSignOut() {
    if (isSigningOut) return;
    setIsSigningOut(true);
    try {
      await signOut();
    } catch {
      toast.error("Oturum kapatılırken sunucuya ulaşılamadı.");
    } finally {
      setIsSigningOut(false);
    }
  }

  if (!identity) return null;
  const roleLabel = { CUSTOMER: "Müşteri hesabı", STORE_OWNER: "Mağaza hesabı", PLATFORM_ADMIN: "Platform yönetimi" }[identity.role];

  return (
    <Menu.Root>
      <Menu.Trigger className="eh-account-link account-menu-trigger" aria-label="Hesap menüsü" disabled={isSigningOut}>
        <UserRound aria-hidden="true" className="size-4" />
        <span className="hidden sm:inline">{isSigningOut ? "Çıkılıyor…" : "Hesabım"}</span>
        <ChevronDown aria-hidden="true" className="hidden size-3.5 sm:block" />
      </Menu.Trigger>
      <MenuPopup>
        <Menu.Group>
          <Menu.GroupLabel className="site-menu-label">{roleLabel}</Menu.GroupLabel>
          {identity.role === "CUSTOMER" ? (
            <>
              <MenuLink href="/account" current={pathname === "/account"}><UserRound aria-hidden="true" className="size-4" /> Bilgilerim</MenuLink>
              <MenuLink href="/rentals" current={pathname === "/rentals"}><ClipboardList aria-hidden="true" className="size-4" /> Kiralamalarım</MenuLink>
            </>
          ) : null}
          {identity.role === "STORE_OWNER" ? (
            <MenuLink href="/store/profile" current={pathname.startsWith("/store")}><Store aria-hidden="true" className="size-4" /> Mağaza yönetimi</MenuLink>
          ) : null}
          {identity.role === "PLATFORM_ADMIN" ? (
            <MenuLink href="/admin" current={pathname.startsWith("/admin")}><LayoutDashboard aria-hidden="true" className="size-4" /> Yönetim paneli</MenuLink>
          ) : null}
        </Menu.Group>
        <Menu.Separator className="site-menu-separator" />
        <Menu.Item className="site-menu-item site-menu-danger" disabled={isSigningOut} closeOnClick={false}
          onClick={() => void handleSignOut()}>
          <LogOut aria-hidden="true" className="size-4" /> {isSigningOut ? "Çıkılıyor…" : "Oturumu kapat"}
        </Menu.Item>
      </MenuPopup>
    </Menu.Root>
  );
}
