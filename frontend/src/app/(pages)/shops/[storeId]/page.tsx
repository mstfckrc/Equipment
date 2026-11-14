import { StorefrontDetail } from "@/components/storefront/storefront-detail";
import { readReturnTo } from "@/lib/utils/navigation";

export default async function StorePage({ params, searchParams }: {
  params: Promise<{ storeId: string }>;
  searchParams: Promise<{ returnTo?: string | string[] }>;
}) {
  const [{ storeId }, query] = await Promise.all([params, searchParams]);
  return <StorefrontDetail storeId={storeId} returnTo={readReturnTo(query.returnTo, ["/", "/shops"], "/shops")} />;
}
