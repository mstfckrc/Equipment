"use client";

import { ProductCard } from "@/components/storefront/product-card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useUrlFilters } from "@/hooks/use-url-filters";
import type { PublicProduct } from "@/types/catalog";

export function StorefrontProducts({ products }: { products: PublicProduct[] }) {
  const { searchParams, updateFilters, href } = useUrlFilters();
  const search = (searchParams.get("q") ?? "").slice(0, 120);
  const categories = [...new Set(products.map((product) => product.category))].sort((a, b) => a.localeCompare(b, "tr"));
  const categoryParam = searchParams.get("category") ?? "";
  const category = categories.includes(categoryParam) ? categoryParam : "";
  const term = search.trim().toLocaleLowerCase("tr");
  const visibleProducts = products.filter((product) =>
    (!category || product.category === category) && product.name.toLocaleLowerCase("tr").includes(term));
  const filtered = Boolean(search || category);

  return (
    <section className="space-y-6" aria-labelledby="products-title">
      <div className="home-section-heading">
        <h2 id="products-title">Kiralayabileceğin ekipmanlar</h2>
        <p>Ürünü incele, uygun tarihleri ve ihtiyacın olan adedi seç.</p>
      </div>
      {products.length > 0 ? <>
        <div className="catalog-filters">
          <label className="catalog-filter-field" htmlFor="product-search">Ürün ara
            <Input id="product-search" type="search" value={search} maxLength={120} placeholder="Ürün adı"
              onChange={(event) => updateFilters({ q: event.target.value })} />
          </label>
          <label className="catalog-filter-field" htmlFor="product-category">Kategori
            <select id="product-category" value={category} onChange={(event) => updateFilters({ category: event.target.value })}>
              <option value="">Tüm kategoriler</option>
              {categories.map((value) => <option key={value} value={value}>{value}</option>)}
            </select>
          </label>
          {filtered ? <Button variant="ghost" onClick={() => updateFilters({ q: "", category: "" })}>Filtreleri temizle</Button> : null}
        </div>
        <p role="status" className="catalog-result-count">{visibleProducts.length} ürün{filtered ? ` · Toplam ${products.length} ürün` : ""}</p>
      </> : null}
      {products.length === 0 ? <p className="catalog-message">Bu mağazada şu anda yayında ürün yok.</p>
        : visibleProducts.length === 0 ? <p className="catalog-message">Bu arama ve kategoriye uygun ürün bulunamadı. Filtrelerini değiştirebilirsin.</p>
          : <ul className="catalog-grid">
            {visibleProducts.map((product) => (
              <li id={`product-${product.productId}`} key={product.productId} className="min-w-0 scroll-mt-8">
                <ProductCard product={product} returnTo={`${href}#product-${product.productId}`} />
              </li>
            ))}
          </ul>}
    </section>
  );
}
