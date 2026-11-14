"use client";

import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { LinkButton } from "@/components/ui/link-button";
import { StoreCard } from "@/components/storefront/store-card";
import { usePublicStores } from "@/hooks/store/use-public-stores";

export function HomeStoreCarousel() {
  const { data: stores = [], isPending: isLoading, isError: hasError, refetch } = usePublicStores();
  const visibleStores = stores.slice(0, 5);

  return (
    <section id="home-stores" className="space-y-5 scroll-mt-8" aria-labelledby="home-stores-title">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="home-section-heading">
          <h2 id="home-stores-title" className="text-2xl font-semibold tracking-tight">Mağazalar</h2>
          <p>Keşfetmeye bu mağazalardan başla. Daha fazlası mağaza listesinde.</p>
        </div>
        <LinkButton href="/shops" variant="secondary" className="shrink-0">
          Tüm mağazalar <ArrowRight aria-hidden="true" className="size-4" />
        </LinkButton>
      </div>

      {isLoading ? <p className="home-stores-message" role="status">Mağazalar yükleniyor…</p> : null}
      {!isLoading && hasError ? (
        <div className="home-stores-message" role="alert">
          <p>Mağazalar şu anda yüklenemedi. Biraz sonra yeniden deneyebilirsin.</p>
          <Button variant="secondary" onClick={() => void refetch()}>Yeniden dene</Button>
        </div>
      ) : null}
      {!isLoading && !hasError && stores.length === 0 ? (
        <p className="home-stores-message">
          Şu anda listelenecek aktif mağaza yok.
        </p>
      ) : null}
      {!isLoading && !hasError && stores.length > 0 ? (
        <ul className="home-store-track store-carousel-track" aria-label="Mağazalar">
          {visibleStores.map((store) => (
            <li key={store.storeId}>
              <StoreCard store={store} returnTo="/#home-stores" />
            </li>
          ))}
          <li>
            <Link href="/shops" className="store-card home-store-all group surface-card interactive-card">
              <div>
                <ArrowRight aria-hidden="true" className="store-card-icon" strokeWidth={1.6} />
                <h3>Aradığın ekipman<br />başka bir mağazada olabilir.</h3>
                <p className="mt-3 text-sm leading-6 text-[var(--muted)]">Tüm mağazalara göz at, sana uygun ekipmanı keşfet.</p>
              </div>
              <span className="store-card-action-row">Tüm mağazaları gör <ArrowRight aria-hidden="true" className="size-4" /></span>
            </Link>
          </li>
        </ul>
      ) : null}
    </section>
  );
}
