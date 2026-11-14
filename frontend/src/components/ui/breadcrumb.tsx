import Link from "next/link";
import { ChevronRight } from "lucide-react";

export interface BreadcrumbItem { label: string; href?: string }

export function Breadcrumb({ items }: { items: BreadcrumbItem[] }) {
  return (
    <nav aria-label="Sayfa yolu" className="min-w-0 text-xs text-[var(--muted)] sm:text-sm">
      <ol className="flex flex-wrap items-center gap-x-2 gap-y-1">
        {items.map((item, index) => (
          <li key={`${index}-${item.label}`} className="flex min-w-0 items-center gap-2">
            {index > 0 ? <ChevronRight aria-hidden="true" className="size-3 shrink-0" /> : null}
            {item.href ? <Link href={item.href} className="hover:text-[var(--accent-strong)]">{item.label}</Link> : <span aria-current="page" className="break-words text-[var(--foreground)]">{item.label}</span>}
          </li>
        ))}
      </ol>
    </nav>
  );
}
