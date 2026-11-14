"use client";

import { StoreCard } from "@/components/storefront/store-card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useUrlFilters } from "@/hooks/use-url-filters";
import { usePublicStores } from "@/hooks/store/use-public-stores";

export function StoreDirectory({ title, description }: { title: string; description: string }) {
  const { data: stores = [], isPending: isLoading, isError: hasError, refetch } = usePublicStores();
  const { searchParams, updateFilters, href } = useUrlFilters();
  const search = (searchParams.get("q") ?? "").slice(0, 120);
  const cities = [...new Set(stores.map((store) => store.city))].sort((a, b) => a.localeCompare(b, "tr"));
  const cityParam = searchParams.get("city") ?? "";
  const city = cities.includes(cityParam) ? cityParam : "";
  const term = search.trim().toLocaleLowerCase("tr");
  const visibleStores = stores.filter((store) => (!city || store.city === city) &&
    `${store.storeName} ${store.city} ${store.district}`.toLocaleLowerCase("tr").includes(term));
  const filtered = Boolean(search || city);

  return (
    <section className="space-y-6" aria-labelledby="store-directory-title">
      <div className="space-y-3">
        <p className="home-eyebrow">Ekipmanını bulmaya buradan başla.</p>
        <h1 id="store-directory-title" className="catalog-title">{title}</h1>
        <p className="text-sm leading-6 text-[var(--muted)]">{description}</p>
      </div>

      {!isLoading && !hasError && stores.length > 0 ? <>
        <div className="catalog-filters">
          <label className="catalog-filter-field" htmlFor="store-search">Mağaza ara
            <Input id="store-search" type="search" value={search} maxLength={120} placeholder="Mağaza adı, il veya ilçe"
              onChange={(event) => updateFilters({ q: event.target.value })} />
          </label>
          <label className="catalog-filter-field" htmlFor="store-city">İl
            <select id="store-city" value={city} onChange={(event) => updateFilters({ city: event.target.value })}>
              <option value="">Tüm iller</option>
              {cities.map((value) => <option key={value} value={value}>{value}</option>)}
            </select>
          </label>
          {filtered ? <Button variant="ghost" onClick={() => updateFilters({ q: "", city: "" })}>Filtreleri temizle</Button> : null}
        </div>
        <p role="status" className="catalog-result-count">{visibleStores.length} mağaza{filtered ? ` · Toplam ${stores.length} mağaza` : ""}</p>
      </> : null}

      {isLoading ? <p className="catalog-message" role="status">Mağazalar yükleniyor…</p> : null}
      {!isLoading && hasError ? (
        <div className="catalog-message" role="alert">
          <p>Mağazalar şu anda yüklenemedi. Biraz sonra yeniden deneyebilirsin.</p>
          <Button variant="secondary" onClick={() => void refetch()}>Yeniden dene</Button>
        </div>
      ) : null}
      {!isLoading && !hasError && stores.length === 0 ? (
        <p className="catalog-message">
          Şu anda listelenecek aktif mağaza yok.
        </p>
      ) : null}
      {!isLoading && !hasError && stores.length > 0 && visibleStores.length === 0 ? (
        <p className="catalog-message">Aramana uygun mağaza bulunamadı. Filtrelerini değiştirebilirsin.</p>
      ) : null}
      {!isLoading && !hasError && visibleStores.length > 0 ? (
        <ul className="catalog-grid">
          {visibleStores.map((store) => (
            <li id={`store-${store.storeId}`} key={store.storeId} className="min-w-0 scroll-mt-8">
              <StoreCard store={store} returnTo={`${href}#store-${store.storeId}`} />
            </li>
          ))}
        </ul>
      ) : null}
    </section>
  );
}
