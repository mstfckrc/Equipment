export default function HomePage() {
  return (
    <main className="mx-auto flex min-h-screen w-full max-w-6xl items-center px-6 py-16">
      <section aria-labelledby="page-title">
        <p className="mb-3 text-sm font-medium uppercase tracking-[0.2em] text-slate-500">
          EquipmentHub
        </p>
        <h1 id="page-title" className="text-4xl font-semibold tracking-tight text-slate-950">
          Ekipman kiralamaya hazır.
        </h1>
        <p className="mt-4 max-w-xl text-base leading-7 text-slate-600">
          Uygulama arayüzünü bu temel üzerine adım adım kuracağız.
        </p>
      </section>
    </main>
  );
}
