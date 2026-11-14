"use client";

import { Copy, MapPin, Phone, Store } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import type { PublicStore } from "@/types/store";

export function StorefrontSummary({ store }: { store: PublicStore }) {
  async function copyAddress() {
    try {
      await navigator.clipboard.writeText(`${store.addressLine}, ${store.district}, ${store.city}`);
      toast.success("Mağaza adresi kopyalandı.");
    } catch {
      toast.error("Adres kopyalanamadı. Adres metnini seçerek kopyalayabilirsin.");
    }
  }

  return (
    <section className="storefront-summary surface-card" aria-labelledby="storefront-title">
      <div className="storefront-identity">
        <Store aria-hidden="true" className="storefront-store-icon" strokeWidth={1.4} />
        <p className="home-eyebrow">Mağaza vitrini</p>
        <h1 id="storefront-title">{store.storeName}</h1>
        <p className="storefront-location"><MapPin aria-hidden="true" className="size-4" />{store.district}, {store.city}</p>
        <span className="storefront-pickup-label">Mağazadan teslim alma</span>
      </div>
      <div className="storefront-contact">
        <h2>Adres ve iletişim</h2>
        <p>{store.addressLine}</p>
        <p className="text-sm text-[var(--muted)]">{store.district}, {store.city}</p>
        <div className="flex flex-wrap gap-2 pt-3">
          <Button variant="secondary" onClick={() => void copyAddress()}><Copy aria-hidden="true" className="size-4" />Adresi kopyala</Button>
          <a className="eh-nav-secondary" href={`tel:${store.phone.replace(/[^\d+]/g, "")}`}>
            <Phone aria-hidden="true" className="size-4 shrink-0" /><span className="min-w-0 [overflow-wrap:anywhere]">{store.phone}</span>
          </a>
        </div>
      </div>
    </section>
  );
}
