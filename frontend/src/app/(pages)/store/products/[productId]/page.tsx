import { StoreProductEditor } from "@/components/store-products/store-product-editor";

export default async function StoreProductRoute({ params }: { params: Promise<{ productId: string }> }) {
  const { productId } = await params;
  return <StoreProductEditor productId={productId} />;
}
