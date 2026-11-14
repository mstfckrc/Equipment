"use client";

import { createContext, Suspense, useCallback, useContext, useEffect, useRef, type ReactNode } from "react";
import { usePathname, useSearchParams } from "next/navigation";

const NavigationContext = createContext<(href: string) => boolean>(() => false);

function locationPath() {
  return `${window.location.pathname}${window.location.search}${window.location.hash}`;
}

function NavigationObserver({ record }: { record: (path: string, fromHistory?: boolean) => void }) {
  const pathname = usePathname();
  const searchParams = useSearchParams();

  useEffect(() => { record(locationPath()); }, [pathname, searchParams, record]);
  useEffect(() => {
    const onPopState = () => record(locationPath(), true);
    window.addEventListener("popstate", onPopState);
    return () => window.removeEventListener("popstate", onPopState);
  }, [record]);
  return null;
}

export function NavigationProvider({ children }: { children: ReactNode }) {
  const history = useRef<string[]>([]);
  const record = useCallback((path: string, fromHistory = false) => {
    if (fromHistory) {
      const index = history.current.lastIndexOf(path);
      history.current = index >= 0 ? history.current.slice(0, index + 1) : [path];
    } else if (history.current.at(-1) !== path) {
      history.current = [...history.current.slice(-39), path];
    }
  }, []);

  // Use native back/scroll restoration only for a known, immediate in-app origin.
  const canGoBackTo = useCallback((href: string) => {
    const current = history.current.at(-1);
    const previous = history.current.at(-2);
    if (!previous || current !== locationPath()) return false;
    const target = new URL(href, window.location.origin);
    const origin = new URL(previous, window.location.origin);
    target.searchParams.delete("returnTo");
    origin.searchParams.delete("returnTo");
    target.searchParams.sort();
    origin.searchParams.sort();
    return target.origin === window.location.origin && target.pathname === origin.pathname && target.search === origin.search;
  }, []);

  return (
    <NavigationContext.Provider value={canGoBackTo}>
      <Suspense fallback={null}><NavigationObserver record={record} /></Suspense>
      {children}
    </NavigationContext.Provider>
  );
}

export function useNavigationHistory() {
  return useContext(NavigationContext);
}
