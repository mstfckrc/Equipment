import { RentalConfirmationPage } from "@/components/rental/rental-confirmation-page";

export default async function ConfirmationPage({ params }: { params: Promise<{ checkoutId: string }> }) {
  const { checkoutId } = await params;
  return <RentalConfirmationPage checkoutId={checkoutId} />;
}
