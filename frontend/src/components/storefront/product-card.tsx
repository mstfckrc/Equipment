import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { ProductImage } from "@/components/storefront/product-image";
import { withReturnTo } from "@/lib/utils/navigation";
import type { PublicProduct } from "@/types/catalog";

const priceFormatter = new Intl.NumberFormat("tr-TR", { style: "currency", currency: "TRY" });

export function ProductCard({ product, returnTo }: { product: PublicProduct; returnTo: string }) {
  return (
    <Link href={withReturnTo(`/shops/${encodeURIComponent(product.storeId)}/products/${encodeURIComponent(product.productId)}`, returnTo)}
      className="product-card surface-card interactive-card">
      <ProductImage imageUrl={product.imageUrl} name={product.name} />
      <div className="product-card-body">
        <p className="product-card-category">{product.category}</p>
        <h3>{product.name}</h3>
        <p className="product-card-description">{product.description}</p>
        <div className="product-card-footer">
          <p><strong>{priceFormatter.format(product.dailyPrice)}</strong><span> / gün</span></p>
          <span className="product-card-action">İncele <ArrowRight aria-hidden="true" className="size-4" /></span>
        </div>
      </div>
    </Link>
  );
}
