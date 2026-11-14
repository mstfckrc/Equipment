"use client";

import { useState, type ReactNode } from "react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

export function QueryProvider({ children }: { children: ReactNode }) {
  const [queryClient] = useState(() => new QueryClient({
    defaultOptions: {
      queries: { retry: false, staleTime: 20_000, refetchOnWindowFocus: false },
      mutations: { retry: false },
    },
  }));

  return <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>;
}
