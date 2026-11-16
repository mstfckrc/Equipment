"use client";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { conditionLabels, emptyInspectionDraft, type InspectionDraft } from "@/components/store-rentals/reservation-helpers";
import type { ReservationItem, ReturnUnitCondition } from "@/types/rental";

export function StoreInspectionForm({ item, drafts, busy, onDraft, onReview }: {
  item: ReservationItem; drafts: Record<string, InspectionDraft>; busy: boolean;
  onDraft: (unitId: string, changes: Partial<InspectionDraft>) => void; onReview: (triggerId: string) => void;
}) {
  const triggerId = `inspection-review-${item.itemId}`;
  return <form className="space-y-5" noValidate onSubmit={(event) => { event.preventDefault(); if (!busy) onReview(triggerId); }}>
    <p className="text-sm leading-6 text-[var(--muted)]">Her cihazın kontrol sonucunu, açıklamasını ve varsa ek tutarı ayrı belirt.</p>
    {item.assignedUnits.map((unit) => {
      const draft = drafts[unit.unitId] ?? emptyInspectionDraft;
      return <fieldset key={unit.unitId} disabled={busy} className="min-w-0 space-y-4 rounded-xl border border-[var(--border)] p-4">
        <legend className="max-w-full break-all px-1 font-mono text-sm font-semibold">{unit.unitCode}</legend>
        <div className="grid gap-4 sm:grid-cols-2">
          <div className="space-y-2 text-sm font-medium"><label htmlFor={`condition-${unit.unitId}`}>Kontrol sonucu</label>
            <select id={`condition-${unit.unitId}`} value={draft.condition} onChange={(event) => onDraft(unit.unitId, { condition: event.target.value as ReturnUnitCondition })}
              className="min-h-11 w-full min-w-0 rounded-lg border border-[var(--border)] bg-[var(--surface)] px-3 text-sm">
              {(Object.keys(conditionLabels) as ReturnUnitCondition[]).map((condition) => <option key={condition} value={condition}>{conditionLabels[condition]}</option>)}
            </select></div>
          <label className="block space-y-2 text-sm font-medium">Kaydedilecek ek tutar (₺)
            <Input inputMode="decimal" value={draft.manualChargeAmount} onChange={(event) => onDraft(unit.unitId, { manualChargeAmount: event.target.value })} placeholder="İsteğe bağlı" maxLength={13} /></label>
        </div>
        <div className="space-y-2 text-sm font-medium"><label htmlFor={`note-${unit.unitId}`}>Kontrol notu</label>
          <textarea id={`note-${unit.unitId}`} value={draft.damageNote} maxLength={500} rows={3} onChange={(event) => onDraft(unit.unitId, { damageNote: event.target.value })}
            className="w-full rounded-lg border border-[var(--border)] bg-[var(--surface)] px-3 py-2 text-sm" placeholder="Hasar veya kontrol notu (isteğe bağlı)" />
          <span className="block text-right text-xs font-normal text-[var(--muted)]">{draft.damageNote.length}/500</span></div>
      </fieldset>;
    })}
    <Button id={triggerId} type="submit" disabled={busy || item.assignedUnits.length !== item.quantity || !item.assignedUnits.length}>Kontrol sonuçlarını gözden geçir</Button>
  </form>;
}
