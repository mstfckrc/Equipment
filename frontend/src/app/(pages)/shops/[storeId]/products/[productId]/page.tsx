import { RentalProductDetail } from "@/components/rental/rental-product-detail";
import { readReturnTo } from "@/lib/utils/navigation";

export default async function ProductPage({
  params,
  searchParams,
}: {
  params: Promise<{ storeId: string; productId: string }>;
  searchParams: Promise<{ returnTo?: string | string[] }>;
}) {
  const [{ storeId, productId }, query] = await Promise.all([params, searchParams]);
  const storeHref = `/shops/${encodeURIComponent(storeId)}`;
  return <RentalProductDetail storeId={storeId} productId={productId} returnTo={readReturnTo(query.returnTo, [storeHref, "/cart"], storeHref)} />;
}
