"use client";

import { useEffect, useRef } from "react";
import { CalendarDays, ClipboardCheck, PackageCheck, RotateCcw, Store } from "lucide-react";

const steps = [
  { title: "Mağazanı bul", description: "Mağazaları gez, ihtiyacına uygun ekipmanı keşfet.", icon: Store },
  { title: "Tarihlerini seç", description: "Uygun günleri ve ihtiyacın olan adedi belirle.", icon: CalendarDays },
  { title: "Rezervasyonunu onayla", description: "Sepetini kontrol et, kiralamanı oluştur.", icon: ClipboardCheck },
  { title: "Teslim al", description: "Rezervasyonundaki tarihte mağazadan teslim al.", icon: PackageCheck },
  { title: "Geri teslim et", description: "Kiralamanın sonunda ekipmanı mağazaya geri getir.", icon: RotateCcw },
];

export function HomeRentalSteps() {
  const sectionRef = useRef<HTMLElement>(null);

  useEffect(() => {
    const section = sectionRef.current;
    if (!section || window.matchMedia("(prefers-reduced-motion: reduce)").matches) return;
    const observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        section.classList.add("home-steps-entered");
        observer.disconnect();
      }
    }, { threshold: 0.15 });
    observer.observe(section);
    return () => observer.disconnect();
  }, []);

  return (
    <section ref={sectionRef} id="how-it-works" className="home-rental-steps scroll-mt-8" aria-labelledby="rental-steps-title">
      <div className="home-section-heading">
        <p className="home-eyebrow">İlk kiralaman mı?</p>
        <h2 id="rental-steps-title">Planından teslim almaya, adım adım.</h2>
        <p>Ekipmanını seçtikten sonra nasıl ilerleyeceğini birlikte netleştirelim.</p>
      </div>
      <ol className="home-steps-list">
        {steps.map(({ title, description, icon: Icon }, index) => (
          <li key={title} className="home-step">
            <div className="home-step-marker" aria-hidden="true">
              <Icon className="size-6" strokeWidth={1.6} />
              <span>{index + 1}</span>
            </div>
            <div>
              <h3>{title}</h3>
              <p>{description}</p>
            </div>
          </li>
        ))}
      </ol>
    </section>
  );
}
