import type { Metadata } from "next";
import { Toaster } from "sonner";
import { SessionProvider } from "@/providers/session-provider";
import { QueryProvider } from "@/providers/query-provider";
import { CartProvider } from "@/providers/cart-provider";
import { NavigationProvider } from "@/providers/navigation-provider";
import { SiteShell } from "@/components/layout/site-shell";
import "react-day-picker/style.css";
import "./globals.css";

export const metadata: Metadata = {
  title: "EquipmentHub",
  description: "Ekipman kiralama platformu",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="tr">
      <body>
        <QueryProvider>
          <SessionProvider>
            <CartProvider>
              <NavigationProvider><SiteShell>{children}</SiteShell></NavigationProvider>
            </CartProvider>
            <Toaster position="top-right" richColors />
          </SessionProvider>
        </QueryProvider>
      </body>
    </html>
  );
}
