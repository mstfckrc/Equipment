import type { ButtonHTMLAttributes } from "react";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "ghost" | "link" | "danger";
};

export function Button({ className = "", variant = "primary", ...props }: ButtonProps) {
  const styles = {
    primary: "bg-[var(--accent)] text-white shadow-sm hover:bg-[var(--accent-strong)]",
    secondary: "border border-[var(--border)] bg-white text-[var(--foreground)] hover:border-[var(--accent-border)] hover:bg-[var(--accent-soft)]",
    ghost: "bg-transparent text-[var(--muted)] hover:bg-[#f0f3f2] hover:text-[var(--foreground)]",
    link: "text-[var(--accent-strong)] underline-offset-4 hover:underline",
    danger: "border border-[#f0c7c3] bg-[#fff7f6] text-[var(--danger)] hover:bg-[#fff0ee]",
  };

  return (
    <button
      className={`inline-flex min-h-11 items-center justify-center gap-2 whitespace-nowrap rounded-lg px-4 py-2 text-sm font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-55 ${styles[variant]} ${className}`}
      {...props}
    />
  );
}
