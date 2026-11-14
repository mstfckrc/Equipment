import type { ReactNode } from "react";
import { AdminManagementLayout } from "@/components/layout/admin-management-layout";

export default function AdminLayout({ children }: { children: ReactNode }) {
  return <AdminManagementLayout>{children}</AdminManagementLayout>;
}
