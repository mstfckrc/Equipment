import type { AccountRole, AuthIdentity } from "@/types/auth";

const knownRoles: AccountRole[] = ["CUSTOMER", "STORE_OWNER", "PLATFORM_ADMIN"];

export function readIdentity(accessToken: string): AuthIdentity {
  try {
    const encodedPayload = accessToken.split(".")[1];
    const normalizedPayload = encodedPayload.replace(/-/g, "+").replace(/_/g, "/");
    const payload = JSON.parse(atob(normalizedPayload)) as {
      sub?: string;
      roles?: string[];
    };
    const role = payload.roles?.map((value) => value.replace(/^ROLE_/, ""))
      .find((value): value is AccountRole => knownRoles.includes(value as AccountRole));

    if (!payload.sub || !role) {
      throw new Error("Missing account claims");
    }

    // Claims are read only to select a screen. Gateway and services verify authorization.
    return { id: payload.sub, role };
  } catch {
    throw new Error("Oturum bilgisi okunamadı.");
  }
}

export function routeForRole(role: AccountRole) {
  switch (role) {
    case "CUSTOMER":
      return "/";
    case "STORE_OWNER":
      return "/store/profile";
    case "PLATFORM_ADMIN":
      return "/admin";
  }
}

export function customerReturnPath(value: string | null) {
  if (!value || !value.startsWith("/") || value.startsWith("//") || value.includes("\\")) return null;
  if (value === "/account" || value === "/cart" || value === "/rentals" || value.startsWith("/shops/")) return value;
  return null;
}

export function routeAfterLogin(role: AccountRole, returnPath: string | null) {
  if (role === "CUSTOMER") return customerReturnPath(returnPath) ?? routeForRole(role);
  return routeForRole(role);
}
