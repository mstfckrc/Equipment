import { CustomerRentalsPage } from "@/components/rental/customer-rentals-page";

export default async function RentalsPage({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | string[] | undefined }>;
}) {
  const params = await searchParams;
  const checkoutParam = params.checkoutId;
  const checkoutId = Array.isArray(checkoutParam) ? checkoutParam[0] : checkoutParam;
  return <CustomerRentalsPage checkoutId={checkoutId} />;
}
