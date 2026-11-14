import { Button } from "@/components/ui/button";
import type { ReactNode } from "react";

export function AdminPageHeader({ eyebrow = "Yönetim", title, description, action }: {
  eyebrow?: string;
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <header className="flex flex-wrap items-end justify-between gap-4">
      <div className="min-w-0 flex-1 basis-64">
        <p className="text-sm font-medium text-[var(--accent-strong)]">{eyebrow}</p>
        <h1 className="mt-1 break-words text-2xl font-semibold tracking-tight [overflow-wrap:anywhere]">{title}</h1>
        <p className="mt-2 max-w-3xl text-sm leading-6 text-[var(--muted)]">{description}</p>
      </div>
      {action}
    </header>
  );
}

export function AdminLoadState({ title, message, onRetry }: {
  title: string;
  message: string;
  onRetry: () => void;
}) {
  return (
    <section className="surface-card space-y-4 p-6" role="alert">
      <div>
        <h2 className="font-semibold">{title}</h2>
        <p className="mt-1 text-sm text-[var(--muted)]">{message}</p>
      </div>
      <Button variant="secondary" onClick={onRetry}>Yeniden dene</Button>
    </section>
  );
}

export function AdminStatus({ children, tone = "neutral" }: {
  children: ReactNode;
  tone?: "neutral" | "success" | "warning" | "danger";
}) {
  const style = {
    neutral: "status-pill-neutral",
    success: "status-pill-success",
    warning: "status-pill-warning",
    danger: "status-pill-danger",
  }[tone];
  return <span className={`inline-flex rounded-full px-2.5 py-1 text-xs font-semibold ${style}`}>{children}</span>;
}

export function AdminFilterButton({ active, children, onClick }: {
  active: boolean;
  children: ReactNode;
  onClick: () => void;
}) {
  return (
    <Button type="button" variant={active ? "primary" : "secondary"} aria-pressed={active}
      className="min-h-10 px-3" onClick={onClick}>
      {children}
    </Button>
  );
}

export function AdminEmptyState({ title, message }: { title: string; message: string }) {
  return (
    <section className="surface-card p-6">
      <h2 className="font-semibold">{title}</h2>
      <p className="mt-1 text-sm text-[var(--muted)]">{message}</p>
    </section>
  );
}
