"use client";

import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";
import { customerReturnPath } from "@/lib/auth/identity";

export function AuthModeSwitch() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const isRegister = pathname === "/register";
  const returnPath = customerReturnPath(searchParams.get("returnTo"));
  const query = returnPath ? `?returnTo=${encodeURIComponent(returnPath)}` : "";

  return (
    <p className="text-sm text-[var(--muted)]">
      {isRegister ? "Hesabın var mı? " : "Hesabın yok mu? "}
      <Link
        className="font-semibold text-[var(--accent-strong)] underline-offset-4 hover:underline"
        href={isRegister ? `/login${query}` : `/register${query}`}
      >
        {isRegister ? "Giriş yap" : "Kayıt ol"}
      </Link>
    </p>
  );
}
