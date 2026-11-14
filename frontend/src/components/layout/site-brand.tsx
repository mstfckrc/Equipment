import Link from "next/link";
import { PackageCheck } from "lucide-react";

export function SiteBrand({ href = "/", label }: { href?: string; label?: string }) {
  return (
    <Link href={href} className="site-brand" aria-label={label ? `EquipmentHub ${label}` : "EquipmentHub ana sayfa"}>
      <PackageCheck aria-hidden="true" className="size-6 shrink-0 text-[var(--accent)]" />
      <span>EquipmentHub{label ? <span className="site-brand-label">{label}</span> : null}</span>
    </Link>
  );
}
