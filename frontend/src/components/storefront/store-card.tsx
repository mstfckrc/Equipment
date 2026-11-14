import Link from "next/link";
import { ArrowRight, MapPin, Store } from "lucide-react";
import { withReturnTo } from "@/lib/utils/navigation";
import type { PublicStoreSummary } from "@/types/store";

export function StoreCard({ store, returnTo }: { store: PublicStoreSummary; returnTo: string }) {
  return (
    <Link href={withReturnTo(`/shops/${encodeURIComponent(store.storeId)}`, returnTo)}
      className="store-card group surface-card interactive-card">
      <div>
        <Store aria-hidden="true" className="store-card-icon" strokeWidth={1.6} />
        <h3>{store.storeName}</h3>
        <p className="store-card-location"><MapPin aria-hidden="true" className="size-4 shrink-0" />{store.district}, {store.city}</p>
      </div>
      <span className="store-card-action-row">Mağazayı gör <ArrowRight aria-hidden="true" className="size-4" /></span>
    </Link>
  );
}
