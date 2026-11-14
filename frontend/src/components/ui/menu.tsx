"use client";

import Link from "next/link";
import { Menu } from "@base-ui/react/menu";
import type { ReactNode } from "react";

export { Menu };

export function MenuPopup({ children }: { children: ReactNode }) {
  return (
    <Menu.Portal>
      <Menu.Positioner align="end" sideOffset={8} className="z-50">
        <Menu.Popup className="site-menu-popup">{children}</Menu.Popup>
      </Menu.Positioner>
    </Menu.Portal>
  );
}

export function MenuLink({ href, children, current = false }: {
  href: string;
  children: ReactNode;
  current?: boolean;
}) {
  return (
    <Menu.LinkItem render={<Link href={href} />} closeOnClick
      aria-current={current ? "page" : undefined} className="site-menu-item">
      {children}
    </Menu.LinkItem>
  );
}
