import type { ReactNode } from "react";
import { AuthHeader } from "@/components/layout/auth-header";
import { GuestGuard } from "@/components/auth/guest-guard";

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <>
      <AuthHeader />
      <main id="main-content" tabIndex={-1} className="mx-auto flex w-full items-center justify-center px-4 py-12 sm:px-6 sm:py-16">
        <div className="w-full"><GuestGuard>{children}</GuestGuard></div>
      </main>
    </>
  );
}
