"use client";

import {
  createContext,
  startTransition,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";
import { useRouter } from "next/navigation";
import { ApiError } from "@/lib/http/api-client";
import { readIdentity } from "@/lib/auth/identity";
import { clearRoleHint, setRoleHint } from "@/lib/auth/role-hint";
import { authService } from "@/services/auth/auth-service";
import type { AccountRole, AuthIdentity, LoginRequest } from "@/types/auth";
import { useQueryClient } from "@tanstack/react-query";

type SessionStatus = "checking" | "anonymous" | "authenticated" | "error";

interface SessionContextValue {
  status: SessionStatus;
  identity: AuthIdentity | null;
  accessToken: string | null;
  error: string | null;
  signIn: (request: LoginRequest) => Promise<AccountRole>;
  signOut: () => Promise<void>;
  retry: () => void;
  withAccessToken: <T>(request: (accessToken: string) => Promise<T>) => Promise<T>;
}

const SessionContext = createContext<SessionContextValue | null>(null);
let refreshPromise: Promise<string> | null = null;

async function refreshAcrossTabs(): Promise<string> {
  if (refreshPromise) {
    return refreshPromise;
  }

  const refresh = () => authService.refresh().then((response) => response.accessToken);
  const locks = typeof navigator !== "undefined" ? navigator.locks : undefined;
  const pending: Promise<string> = locks
    ? locks.request<Promise<string>>("equipmenthub-auth-refresh", { mode: "exclusive" }, refresh)
      .then((tokenPromise) => tokenPromise)
    : refresh();
  refreshPromise = pending;

  try {
    return await pending;
  } finally {
    refreshPromise = null;
  }
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [status, setStatus] = useState<SessionStatus>("checking");
  const [identity, setIdentity] = useState<AuthIdentity | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [attempt, setAttempt] = useState(0);
  const previousIdentityId = useRef<string | null>(null);

  const clearSession = useCallback(() => {
    const hadIdentity = previousIdentityId.current !== null;
    clearRoleHint();
    setIdentity(null);
    setAccessToken(null);
    setError(null);
    setStatus("anonymous");
    previousIdentityId.current = null;
    // An ordinary anonymous visit must not cancel the public store/catalog queries.
    if (hadIdentity) queryClient.clear();
  }, [queryClient]);

  const acceptToken = useCallback((token: string) => {
    const account = readIdentity(token);
    if (previousIdentityId.current && previousIdentityId.current !== account.id) {
      queryClient.clear();
    }
    previousIdentityId.current = account.id;
    setRoleHint(account.role);
    setAccessToken(token);
    setIdentity(account);
    setError(null);
    setStatus("authenticated");
    return account;
  }, [queryClient]);

  const withAccessToken = useCallback(async <T,>(request: (accessToken: string) => Promise<T>) => {
    if (!accessToken) throw new ApiError(401, "Oturum açmanız gerekiyor.");
    try {
      return await request(accessToken);
    } catch (error) {
      if (!(error instanceof ApiError) || error.status !== 401) throw error;
      try {
        const refreshedToken = await refreshAcrossTabs();
        acceptToken(refreshedToken);
        return await request(refreshedToken);
      } catch (refreshError) {
        if (refreshError instanceof ApiError && refreshError.status === 401) clearSession();
        throw refreshError;
      }
    }
  }, [accessToken, acceptToken, clearSession]);

  useEffect(() => {
    let active = true;

    async function restoreSession() {
      setStatus("checking");
      try {
        await authService.getCsrfToken();
        const token = await refreshAcrossTabs();
        if (active) acceptToken(token);
      } catch (cause) {
        if (!active) return;
        if (cause instanceof ApiError && cause.status === 401) {
          clearSession();
        } else {
          setStatus("error");
          setError("Oturum bilgisi alınamadı. Yeniden deneyin.");
        }
      }
    }

    void restoreSession();
    return () => {
      active = false;
    };
  }, [attempt, acceptToken, clearSession]);

  const signIn = useCallback(async (request: LoginRequest) => {
    const response = await authService.login(request);
    return acceptToken(response.accessToken).role;
  }, [acceptToken]);

  const signOut = useCallback(async () => {
    try {
      await authService.logout();
    } finally {
      // Commit the anonymous session with the public route, without an intermediate guard redirect.
      startTransition(() => {
        clearSession();
        router.replace("/");
      });
    }
  }, [clearSession, router]);

  const value = useMemo(() => ({
    status,
    identity,
    accessToken,
    error,
    signIn,
    signOut,
    retry: () => setAttempt((current) => current + 1),
    withAccessToken,
  }), [status, identity, accessToken, error, signIn, signOut, withAccessToken]);

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>;
}

export function useSession() {
  const session = useContext(SessionContext);
  if (!session) {
    throw new Error("useSession must be used within SessionProvider");
  }
  return session;
}
