"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { AuthHeader } from "@/components/layout/auth-header";
import { routeForRole } from "@/lib/auth/identity";
import { useSession } from "@/providers/session-provider";
import type { AccountRole } from "@/types/auth";

export function RoleGuard({ role, children }: { role: AccountRole; children: ReactNode }) {
  const router = useRouter();
  const { status, identity, error, retry } = useSession();

  useEffect(() => {
    if (status === "anonymous") router.replace("/login");
    if (status === "authenticated" && identity && identity.role !== role) {
      router.replace(routeForRole(identity.role));
    }
  }, [status, identity, role, router]);

  if (status === "error") {
    return (
      <>
        <AuthHeader />
        <main id="main-content" tabIndex={-1} className="mx-auto max-w-md px-5 py-16 text-center">
          <p className="text-sm text-[var(--muted)]">{error}</p>
          <Button className="mt-5" variant="secondary" onClick={retry}>Yeniden dene</Button>
        </main>
      </>
    );
  }

  if (status !== "authenticated" || identity?.role !== role) {
    return (
      <main id="main-content" tabIndex={-1} className="flex items-center justify-center px-5 py-20">
        <p className="text-sm text-[var(--muted)]" role="status">Oturum kontrol ediliyor…</p>
      </main>
    );
  }

  return children;
}
