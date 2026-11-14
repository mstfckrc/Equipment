"use client";

import { usePathname, useSearchParams } from "next/navigation";
import { useSyncExternalStore } from "react";

const filtersChanged = "equipmenthub:filters-changed";

function subscribeToFilters(onChange: () => void) {
  window.addEventListener(filtersChanged, onChange);
  window.addEventListener("popstate", onChange);
  return () => {
    window.removeEventListener(filtersChanged, onChange);
    window.removeEventListener("popstate", onChange);
  };
}

function currentQuery() {
  return window.location.search.slice(1);
}

export function useUrlFilters() {
  const pathname = usePathname();
  const serverQuery = useSearchParams().toString();
  // Controlled inputs need the current URL immediately, before a router transition commits.
  const query = useSyncExternalStore(subscribeToFilters, currentQuery, () => serverQuery);
  const searchParams = new URLSearchParams(query);

  function updateFilters(values: Record<string, string>) {
    const url = new URL(window.location.href);
    for (const [key, value] of Object.entries(values)) {
      if (value) url.searchParams.set(key, value);
      else url.searchParams.delete(key);
    }
    // Replace the current filters without a route request or a history entry per keystroke.
    window.history.replaceState(null, "", `${url.pathname}${url.search}`);
    window.dispatchEvent(new Event(filtersChanged));
  }

  return { searchParams, updateFilters, href: `${pathname}${query ? `?${query}` : ""}` };
}
