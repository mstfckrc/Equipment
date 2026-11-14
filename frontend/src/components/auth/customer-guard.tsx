"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { routeForRole } from "@/lib/auth/identity";
import { useSession } from "@/providers/session-provider";

export function CustomerGuard({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { status, identity, error, retry } = useSession();

  useEffect(() => {
    if (status === "anonymous") {
      router.replace(`/login?returnTo=${encodeURIComponent(window.location.pathname)}`);
    }
    if (status === "authenticated" && identity && identity.role !== "CUSTOMER") {
      router.replace(routeForRole(identity.role));
    }
  }, [identity, router, status]);

  if (status === "error") {
    return (
      <>
        <main id="main-content" tabIndex={-1} className="mx-auto max-w-lg px-5 py-16 text-center">
          <p className="text-sm text-[var(--muted)]">{error}</p>
          <Button className="mt-5" variant="secondary" onClick={retry}>Yeniden dene</Button>
        </main>
      </>
    );
  }

  if (status !== "authenticated" || identity?.role !== "CUSTOMER") {
    return <main id="main-content" tabIndex={-1} className="flex items-center justify-center px-5 py-20"><p className="text-sm text-[var(--muted)]" role="status">Oturum kontrol ediliyor…</p></main>;
  }

  return children;
}
