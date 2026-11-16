import { NextResponse, type NextRequest } from "next/server";
import { routeForRole } from "@/lib/auth/identity";
import { isAccountRole, ROLE_HINT_COOKIE } from "@/lib/auth/role-hint";

export function proxy(request: NextRequest) {
  const requiredRole = request.nextUrl.pathname.startsWith("/admin")
    ? "PLATFORM_ADMIN"
    : request.nextUrl.pathname.startsWith("/store") ? "STORE_OWNER" : "CUSTOMER";
  const roleHint = request.cookies.get(ROLE_HINT_COOKIE)?.value;

  if (!isAccountRole(roleHint)) {
    const loginUrl = new URL("/login", request.url);
    if (request.nextUrl.pathname === "/account" || request.nextUrl.pathname.startsWith("/rentals/confirmations/")) {
      loginUrl.searchParams.set("returnTo", request.nextUrl.pathname);
    }
    return NextResponse.redirect(loginUrl);
  }

  if (roleHint !== requiredRole) {
    return NextResponse.redirect(new URL(routeForRole(roleHint), request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/admin/:path*", "/store/:path*", "/account", "/rentals/confirmations/:path*"],
};
