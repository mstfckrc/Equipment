"use client";

import { useRouter } from "next/navigation";
import { ArrowLeft } from "lucide-react";
import { Breadcrumb, type BreadcrumbItem } from "@/components/ui/breadcrumb";
import { LinkButton } from "@/components/ui/link-button";
import { useNavigationHistory } from "@/providers/navigation-provider";

export function PageNavigation({ href, label, items }: { href: string; label: string; items: BreadcrumbItem[] }) {
  const router = useRouter();
  const canGoBackTo = useNavigationHistory();

  return (
    <div className="flex flex-wrap items-center justify-between gap-x-6 gap-y-3">
      <LinkButton href={href} variant="secondary" onClick={(event) => {
        if (event.defaultPrevented || event.button !== 0 || event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
        if (canGoBackTo(href)) {
          event.preventDefault();
          router.back();
        }
      }}>
        <ArrowLeft aria-hidden="true" className="size-4" />{label}
      </LinkButton>
      <Breadcrumb items={items} />
    </div>
  );
}
