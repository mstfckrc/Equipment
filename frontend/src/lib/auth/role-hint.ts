import type { AccountRole } from "@/types/auth";

export const ROLE_HINT_COOKIE = "equipmenthub-role-hint";

const accountRoles: AccountRole[] = ["CUSTOMER", "STORE_OWNER", "PLATFORM_ADMIN"];

export function isAccountRole(value: string | undefined): value is AccountRole {
  return value !== undefined && accountRoles.includes(value as AccountRole);
}

// This cookie is only an optimistic UI-routing hint. The Gateway remains the authorization boundary.
export function setRoleHint(role: AccountRole) {
  if (typeof document === "undefined") return;
  const secure = window.location.protocol === "https:" ? "; Secure" : "";
  document.cookie = `${ROLE_HINT_COOKIE}=${role}; Path=/; SameSite=Lax${secure}`;
}

export function clearRoleHint() {
  if (typeof document === "undefined") return;
  const secure = window.location.protocol === "https:" ? "; Secure" : "";
  document.cookie = `${ROLE_HINT_COOKIE}=; Path=/; Max-Age=0; SameSite=Lax${secure}`;
}
