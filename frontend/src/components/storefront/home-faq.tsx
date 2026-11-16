"use client";

import { Accordion } from "@base-ui/react/accordion";
import { Plus } from "lucide-react";

const questions = [
  { question: "Ekipmanı nasıl teslim alacağım?", answer: "Ekipmanı rezervasyonunda belirtilen tarihte ilgili mağazadan teslim alır, kiralama sonunda aynı mağazaya geri teslim edersin. Mağazanın adresini ve iletişim bilgisini mağaza sayfasında görebilirsin." },
  { question: "Tarih seçmek ekipmanı benim için ayırır mı?", answer: "Tarih seçmek veya sepete eklemek tek başına ekipmanı ayırmaz. Rezervasyonunu onaylarken güncel uygunluk ve fiyat tekrar kontrol edilir. Değişiklik varsa onaylamadan önce sana gösterilir." },
  { question: "Birden fazla mağazadan kiralayabilir miyim?", answer: "Evet. Farklı mağazalardan ekipmanları aynı sepete ekleyebilirsin. Onayladığında her mağaza için ayrı rezervasyon oluşur; hepsini Kiralamalarım ekranından takip edebilirsin." },
  { question: "Kiralama bilgilerimi nereden takip edebilirim?", answer: "Kiralamalarım ekranında rezervasyonlarını, teslim alma ve geri teslim tarihlerini takip edebilirsin. Her rezervasyon grubunun onay belgesine de aynı ekrandan ulaşabilirsin." },
  { question: "Ekipmanı ne zaman geri teslim etmeliyim?", answer: "Seçtiğin geri teslim tarihinde saat 09.00'a kadar ekipmanı ilgili mağazaya ulaştırmalısın. Teslim alma saati 12.00'dir; kesin tarih ve saatleri rezervasyonunda görebilirsin." },
];

export function HomeFaq() {
  return (
    <section className="home-faq" aria-labelledby="home-faq-title">
      <div className="home-section-heading">
        <p className="home-eyebrow">Aklındaki sorular</p>
        <h2 id="home-faq-title">Başlamadan önce.</h2>
        <p>Kiralama süreciyle ilgili birkaç kısa yanıt.</p>
      </div>
      <Accordion.Root className="home-faq-list">
        {questions.map(({ question, answer }) => (
          <Accordion.Item key={question} className="home-faq-item">
            <Accordion.Header>
              <Accordion.Trigger className="home-faq-trigger">
                {question}<Plus aria-hidden="true" className="size-5 shrink-0" />
              </Accordion.Trigger>
            </Accordion.Header>
            <Accordion.Panel className="home-faq-panel">
              <p>{answer}</p>
            </Accordion.Panel>
          </Accordion.Item>
        ))}
      </Accordion.Root>
    </section>
  );
}
