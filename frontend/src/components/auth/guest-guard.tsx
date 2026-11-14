"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { customerReturnPath, routeAfterLogin } from "@/lib/auth/identity";
import { useSession } from "@/providers/session-provider";

export function GuestGuard({ children }: { children: ReactNode }) {
  const router = useRouter();
  const { status, identity, error, retry } = useSession();

  useEffect(() => {
    if (status !== "authenticated" || !identity) return;
    const returnPath = customerReturnPath(new URLSearchParams(window.location.search).get("returnTo"));
    router.replace(routeAfterLogin(identity.role, returnPath));
  }, [status, identity, router]);

  if (status === "error") {
    return (
      <div className="mx-auto max-w-lg space-y-5 text-center">
        <p role="alert" className="text-sm text-[var(--muted)]">{error}</p>
        <Button variant="secondary" onClick={retry}>Yeniden dene</Button>
      </div>
    );
  }

  if (status !== "anonymous") {
    return <p role="status" className="text-center text-sm text-[var(--muted)]">
      {status === "authenticated" ? "Yönlendiriliyorsun…" : "Oturum kontrol ediliyor…"}
    </p>;
  }

  return children;
}
