import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { SiteBrand } from "@/components/layout/site-brand";

export function AuthHeader() {
  return (
    <header className="site-header">
      <div className="site-header-inner site-header-simple">
        <SiteBrand />
        <Link href="/" className="eh-nav-link" aria-label="Ana sayfaya dön">
          <ArrowLeft aria-hidden="true" className="size-4" />
          <span className="hidden sm:inline">Ana sayfaya dön</span>
        </Link>
      </div>
    </header>
  );
}
