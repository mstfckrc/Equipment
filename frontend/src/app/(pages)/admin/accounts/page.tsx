import { Suspense } from "react";
import { AdminAccountsPage } from "@/components/admin/admin-accounts-page";

export default function AdminAccountsRoute() {
  return <Suspense fallback={<p role="status">Liste yükleniyor…</p>}><AdminAccountsPage /></Suspense>;
}
