"use client";

import { useQuery } from "@tanstack/react-query";
import { storeService } from "@/services/store/store-service";

export function usePublicStores() {
  return useQuery({ queryKey: ["public-stores"], queryFn: storeService.listPublicStores });
}
