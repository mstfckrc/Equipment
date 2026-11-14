import { Suspense } from "react";
import { AdminCatalogPage } from "@/components/admin/admin-catalog-page";

export default function AdminCatalogRoute() {
  return <Suspense fallback={<p role="status">Liste yükleniyor…</p>}><AdminCatalogPage /></Suspense>;
}
