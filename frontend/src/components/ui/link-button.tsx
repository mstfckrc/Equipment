import Link from "next/link";
import type { ComponentProps } from "react";

type LinkButtonProps = ComponentProps<typeof Link> & {
  variant?: "primary" | "secondary";
};

export function LinkButton({ variant = "primary", className = "", ...props }: LinkButtonProps) {
  return <Link className={`${variant === "primary" ? "eh-nav-primary" : "eh-nav-secondary"} ${className}`} {...props} />;
}
