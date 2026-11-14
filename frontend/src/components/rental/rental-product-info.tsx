import { MapPin, Store } from "lucide-react";
import { ProductImage } from "@/components/storefront/product-image";
import { LinkButton } from "@/components/ui/link-button";
import type { PublicProduct } from "@/types/catalog";
import type { PublicStore } from "@/types/store";

export function RentalProductInfo({ product, store }: { product: PublicProduct; store: PublicStore }) {
  return <section className="surface-card min-w-0 overflow-hidden">
    <ProductImage imageUrl={product.imageUrl} name={product.name} />
    <div className="space-y-6 p-5 sm:p-8">
      <header className="min-w-0 space-y-3">
        <p className="home-eyebrow break-words">{product.category}</p>
        <h1 className="break-words text-2xl font-semibold leading-tight tracking-tight sm:text-3xl">{product.name}</h1>
        <p className="text-2xl font-semibold text-[var(--accent-strong)]">
          {new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" }).format(product.dailyPrice)}
          <span className="ml-2 text-sm font-normal text-[var(--muted)]">/ gün</span>
        </p>
      </header>
      <div className="space-y-2 border-t border-[var(--border)] pt-5">
        <h2 className="text-sm font-semibold">Ekipman hakkında</h2>
        <p className="break-words whitespace-pre-line text-sm leading-7 text-[var(--muted)]">{product.description}</p>
      </div>
      <div className="space-y-4 rounded-xl bg-[var(--page)] p-4 sm:p-5">
        <div className="flex items-start gap-3">
          <Store aria-hidden="true" className="mt-1 size-5 shrink-0 text-[var(--accent-strong)]" />
          <div className="min-w-0 space-y-1"><p className="text-xs text-[var(--muted)]">Kiralayacağın mağaza</p>
            <p className="break-words text-sm font-semibold">{store.storeName}</p></div>
        </div>
        <div className="flex items-start gap-3">
          <MapPin aria-hidden="true" className="mt-1 size-5 shrink-0 text-[var(--muted)]" />
          <div className="min-w-0 space-y-1"><p className="text-xs text-[var(--muted)]">Mağazadan teslim alma ve geri teslim</p>
            <p className="break-words text-sm leading-6">{store.addressLine}<br />{store.district}, {store.city}</p></div>
        </div>
        <LinkButton variant="secondary" href={`/shops/${encodeURIComponent(store.storeId)}`}>Mağazayı gör</LinkButton>
      </div>
    </div>
  </section>;
}
