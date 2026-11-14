import { Suspense } from "react";
import { AdminRegistrationsPage } from "@/components/admin/admin-registrations-page";

export default function AdminRegistrationsRoute() {
  return <Suspense fallback={<p role="status">Liste yükleniyor…</p>}><AdminRegistrationsPage /></Suspense>;
}
