import { Button } from "@/components/ui/button";
import type { EquipmentUnit, EquipmentUnitStatus } from "@/types/rental";

export const unitLabels: Record<EquipmentUnitStatus, string> = {
  READY: "Hazır", RENTED: "Kirada", RETURN_PENDING_INSPECTION: "Kontrol bekliyor",
  MAINTENANCE: "Bakımda", RETIRED: "Kullanımdan kaldırıldı",
};

export function StoreInventoryUnitRow({ unit, busy, saving, error, onChange }: {
  unit: EquipmentUnit; busy: boolean; saving: boolean; error?: string;
  onChange: (unit: EquipmentUnit, status: EquipmentUnitStatus, triggerId: string) => void;
}) {
  const action = unit.status === "READY" ? { status: "MAINTENANCE" as const, label: "Bakıma al" }
    : unit.status === "MAINTENANCE" ? { status: "READY" as const, label: "Hazır yap" } : null;
  const style = unit.status === "READY" ? "status-pill-success" : unit.status === "MAINTENANCE" ? "status-pill-warning"
    : unit.status === "RETIRED" ? "status-pill-neutral" : "status-pill-active";
  return <li className="space-y-3 px-5 py-4" data-unit-id={unit.unitId}>
    <div className="flex flex-wrap items-center justify-between gap-3">
      <div className="min-w-0 space-y-2"><p className="break-all font-mono text-sm font-semibold">{unit.unitCode}</p>
        <span className={`inline-block rounded-full px-3 py-1 text-xs font-semibold ${style}`}>{unitLabels[unit.status]}</span></div>
      <div className="flex flex-wrap gap-2">
        {action ? <Button id={`unit-status-${unit.unitId}`} variant="secondary" disabled={busy}
          onClick={() => onChange(unit, action.status, `unit-status-${unit.unitId}`)}>{saving ? "Güncelleniyor…" : action.label}</Button> : null}
        {action ? <Button id={`unit-retire-${unit.unitId}`} variant="ghost" disabled={busy} className="text-[var(--danger)]"
          onClick={() => onChange(unit, "RETIRED", `unit-retire-${unit.unitId}`)}>Kullanımdan kaldır</Button> : null}
      </div>
    </div>
    {error ? <p role="alert" className="notice-danger rounded-lg p-3 text-sm">{error}</p> : null}
  </li>;
}
