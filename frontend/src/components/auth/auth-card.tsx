import { Suspense, type ReactNode } from "react";
import { AuthModeSwitch } from "@/components/auth/auth-mode-switch";

export function AuthCard({
  title,
  description,
  children,
}: {
  title: string;
  description: string;
  children: ReactNode;
}) {
  return (
    <section className="auth-card-enter surface-card mx-auto w-full max-w-lg p-6 shadow-[0_12px_36px_rgba(22,31,40,0.045)] sm:p-8">
      <div className="mb-6 space-y-2">
        <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
        <p className="text-sm leading-6 text-[var(--muted)]">{description}</p>
        <Suspense fallback={<div aria-hidden="true" className="h-5" />}>
          <AuthModeSwitch />
        </Suspense>
      </div>
      {children}
    </section>
  );
}
