import type { ReactNode } from "react";
import { StoreManagementLayout } from "@/components/layout/store-management-layout";

export default function StoreLayout({ children }: { children: ReactNode }) {
  return <StoreManagementLayout>{children}</StoreManagementLayout>;
}
