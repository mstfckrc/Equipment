"use client";

import { useQuery } from "@tanstack/react-query";
import { StorefrontSummary } from "@/components/storefront/storefront-summary";
import { StorefrontProducts } from "@/components/storefront/storefront-products";
import { Button } from "@/components/ui/button";
import { catalogService } from "@/services/catalog/catalog-service";
import { storeService } from "@/services/store/store-service";
import { PageNavigation } from "@/components/layout/page-navigation";
import { ApiError } from "@/lib/http/api-client";

export function StorefrontDetail({ storeId, returnTo }: { storeId: string; returnTo: string }) {
  const storeQuery = useQuery({ queryKey: ["public-store", storeId], queryFn: () => storeService.getPublicStore(storeId) });
  const productsQuery = useQuery({ queryKey: ["public-store-products", storeId], queryFn: () => catalogService.listStoreProducts(storeId) });
  const store = storeQuery.data;
  const notFound = storeQuery.error instanceof ApiError && storeQuery.error.status === 404;

  return (
      <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-6xl space-y-10 px-5 pb-16 pt-8 sm:px-8 sm:pt-12">
        <PageNavigation href={returnTo} label={returnTo.startsWith("/shops") ? "Mağazalara dön" : "Ana sayfaya dön"}
          items={[{ label: "Ana sayfa", href: "/" }, { label: "Mağazalar", href: "/shops" }, { label: store?.storeName ?? "Mağaza" }]} />

        {storeQuery.isPending ? <p className="catalog-message" role="status">Mağaza yükleniyor…</p> : null}
        {storeQuery.isError ? (
          <div className="catalog-message" role="alert">
            <h1 className="text-xl font-semibold text-[var(--foreground)]">{notFound ? "Mağaza bulunamadı" : "Mağaza yüklenemedi"}</h1>
            <p>{notFound ? "Bu mağaza şu anda erişilebilir değil. Diğer mağazalara göz atabilirsin."
              : "Mağaza bilgileri şu anda alınamadı. Biraz sonra yeniden deneyebilirsin."}</p>
            {!notFound ? <Button variant="secondary" onClick={() => void storeQuery.refetch()}>Yeniden dene</Button> : null}
          </div>
        ) : null}
        {!storeQuery.isError && store ? (
          <>
            <StorefrontSummary store={store} />
            {productsQuery.isPending ? <p className="catalog-message" role="status">Ürünler yükleniyor…</p> : null}
            {productsQuery.isError ? <div className="catalog-message" role="alert">
              <p>Ürünler şu anda yüklenemedi. Mağaza bilgilerini inceleyebilir veya yeniden deneyebilirsin.</p>
              <Button variant="secondary" onClick={() => void productsQuery.refetch()}>Ürünleri yeniden yükle</Button>
            </div> : null}
            {productsQuery.isSuccess ? <StorefrontProducts products={productsQuery.data} /> : null}
          </>
        ) : null}
      </main>
  );
}
