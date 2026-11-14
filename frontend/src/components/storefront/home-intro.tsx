import { ArrowDown, ArrowRight, CalendarDays, MapPin, PackageOpen } from "lucide-react";
import { LinkButton } from "@/components/ui/link-button";

export function HomeIntro() {
  return (
    <section className="home-intro" aria-labelledby="home-title">
      <div className="home-intro-copy">
        <p className="home-eyebrow">İhtiyacın kadar, ihtiyacın olduğunda.</p>
        <h1 id="home-title">Ekipmanını bul.<br />Planını hayata geçir.</h1>
        <p className="home-intro-description">
          Mağazaları keşfet, ekipmanını ve tarihlerini seç. Rezervasyonunu oluştur,
          mağazadan teslim al.
        </p>
        <div className="flex flex-wrap gap-3">
          <LinkButton href="/shops">Mağazaları keşfet <ArrowRight aria-hidden="true" className="size-4" /></LinkButton>
          <LinkButton href="#how-it-works" variant="secondary">Nasıl kiralanır? <ArrowDown aria-hidden="true" className="size-4" /></LinkButton>
        </div>
      </div>
      <aside className="home-intro-note" aria-label="Kiralama hakkında">
        <PackageOpen aria-hidden="true" className="home-intro-icon" strokeWidth={1.4} />
        <h2>Satın almak yerine,<br />ihtiyacın kadar kullan.</h2>
        <p>Bir ekipman, farklı planlar. Sana uygun mağazayı ve tarih aralığını seçmekle başla.</p>
        <div className="home-intro-note-footer">
          <span><CalendarDays aria-hidden="true" className="size-4" /> Tarihine göre kirala</span>
          <span><MapPin aria-hidden="true" className="size-4" /> Mağazadan teslim al</span>
        </div>
      </aside>
    </section>
  );
}
