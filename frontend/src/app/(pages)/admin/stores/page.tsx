import { Suspense } from "react";
import { AdminStoresPage } from "@/components/admin/admin-stores-page";

export default function AdminStoresRoute() {
  return <Suspense fallback={<p role="status">Liste yükleniyor…</p>}><AdminStoresPage /></Suspense>;
}
