import Link from "next/link";
import { ArrowRight, ClipboardList, PackageSearch, Store, Users } from "lucide-react";
import { AdminPageHeader } from "@/components/admin/admin-ui";

const sections = [
  { href: "/admin/store-registrations", title: "Mağaza başvuruları", description: "Yeni başvuruları inceleyin ve kararları yönetin.", icon: ClipboardList },
  { href: "/admin/accounts", title: "Hesaplar", description: "Müşteri ve mağaza hesabı erişimlerini yönetin.", icon: Users },
  { href: "/admin/stores", title: "Mağazalar", description: "Mağaza bilgilerini ve faaliyet durumunu görüntüleyin.", icon: Store },
  { href: "/admin/catalog", title: "Ürün moderasyonu", description: "İlanları inceleyin ve gerekli durum kararlarını verin.", icon: PackageSearch },
];

export default function AdminPage() {
  return (
    <div className="space-y-6">
      <AdminPageHeader title="Yönetim merkezi" description="Platformdaki başvurulara ve mevcut yönetim alanlarına buradan ulaşın." />
      <section className="grid gap-3 sm:grid-cols-2" aria-label="Yönetim alanları">
        {sections.map(({ href, title, description, icon: Icon }) => (
          <Link key={href} href={href} className="surface-card group flex min-h-36 items-start justify-between gap-4 p-5 transition-colors hover:border-[var(--accent-border)] hover:bg-[var(--accent-soft)]">
            <div>
              <span className="mb-3 inline-flex size-9 items-center justify-center rounded-lg bg-[var(--accent-soft)] text-[var(--accent-strong)]">
                <Icon aria-hidden="true" className="size-5" />
              </span>
              <h2 className="font-semibold">{title}</h2>
              <p className="mt-1 text-sm leading-5 text-[var(--muted)]">{description}</p>
            </div>
            <ArrowRight aria-hidden="true" className="mt-1 size-4 shrink-0 text-[var(--muted)] transition-transform group-hover:translate-x-1" />
          </Link>
        ))}
      </section>
      <p className="text-xs leading-5 text-[var(--muted)]">Sistem logları ve servis sağlık ekranı henüz bu panelin kapsamına bağlı değil.</p>
    </div>
  );
}
