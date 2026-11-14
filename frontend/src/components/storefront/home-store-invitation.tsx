"use client";

import { ArrowRight, Store } from "lucide-react";
import { LinkButton } from "@/components/ui/link-button";
import { useSession } from "@/providers/session-provider";

export function HomeStoreInvitation() {
  const { status } = useSession();
  if (status !== "anonymous") return null;

  return (
    <section className="home-store-invitation" aria-labelledby="store-invitation-title">
      <div className="home-invitation-copy">
        <Store aria-hidden="true" className="size-8 shrink-0" strokeWidth={1.5} />
        <div>
          <h2 id="store-invitation-title">Ekipmanların yeni planlara eşlik etsin.</h2>
          <p>Mağaza hesabına başvur. Onaylandıktan sonra ürünlerini ve kiralamalarını yönet.</p>
        </div>
      </div>
      <LinkButton href="/register?accountType=STORE_OWNER" variant="secondary">Mağaza hesabı aç <ArrowRight aria-hidden="true" className="size-4" /></LinkButton>
    </section>
  );
}
