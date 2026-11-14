import type { InputHTMLAttributes } from "react";

export function Input({ className = "", ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return (
    <input
      className={`min-h-11 w-full rounded-lg border border-[var(--border)] bg-white px-3 text-sm text-[var(--foreground)] placeholder:text-[#89929b] focus:border-[var(--focus)] focus:outline-none ${className}`}
      {...props}
    />
  );
}
