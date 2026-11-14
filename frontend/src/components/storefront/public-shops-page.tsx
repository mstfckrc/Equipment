import { Suspense } from "react";
import { StoreDirectory } from "@/components/storefront/store-directory";
import { PageNavigation } from "@/components/layout/page-navigation";

export function PublicShopsPage() {
  return (
      <main id="main-content" tabIndex={-1} className="mx-auto w-full max-w-6xl space-y-8 px-5 pb-16 pt-8 sm:px-8 sm:pt-12">
        <PageNavigation href="/" label="Ana sayfaya dön" items={[{ label: "Ana sayfa", href: "/" }, { label: "Mağazalar" }]} />
        <Suspense fallback={<p className="catalog-message" role="status">Mağazalar yükleniyor…</p>}>
          <StoreDirectory title="Mağazalar" description="Mağazaları keşfet, sana uygun ekipmanın bulunduğu vitrine göz at." />
        </Suspense>
      </main>
  );
}
