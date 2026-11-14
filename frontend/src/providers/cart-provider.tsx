"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useReducer,
  type ReactNode,
} from "react";
import { z } from "zod";
import { useSession } from "@/providers/session-provider";
import type { CartLine } from "@/types/cart";

const cartLineSchema = z.object({
  lineId: z.string(),
  storeId: z.string().uuid(),
  storeName: z.string(),
  listingId: z.string().uuid(),
  listingName: z.string(),
  category: z.string(),
  dailyPrice: z.number().positive(),
  quantity: z.number().int().min(1).max(1000),
  pickupDate: z.iso.date(),
  returnDate: z.iso.date(),
});

type CartState = { ownerId: string | null; lines: CartLine[] };
type CartAction =
  | { type: "load"; ownerId: string | null; lines: CartLine[] }
  | { type: "add"; line: CartLine }
  | { type: "quantity"; lineId: string; quantity: number }
  | { type: "remove"; lineId: string }
  | { type: "price"; lineId: string; dailyPrice: number }
  | { type: "clear" };

function cartReducer(state: CartState, action: CartAction): CartState {
  switch (action.type) {
    case "load":
      return { ownerId: action.ownerId, lines: action.lines };
    case "add": {
      const existing = state.lines.find((line) =>
        line.storeId === action.line.storeId
        && line.listingId === action.line.listingId
        && line.pickupDate === action.line.pickupDate
        && line.returnDate === action.line.returnDate,
      );
      if (!existing) return { ...state, lines: [...state.lines, action.line] };
      return {
        ...state,
        lines: state.lines.map((line) => line.lineId === existing.lineId
          ? { ...line, quantity: Math.min(1000, line.quantity + action.line.quantity) }
          : line),
      };
    }
    case "quantity":
      return { ...state, lines: state.lines.map((line) => line.lineId === action.lineId
        ? { ...line, quantity: Math.max(1, Math.min(1000, action.quantity)) }
        : line) };
    case "remove":
      return { ...state, lines: state.lines.filter((line) => line.lineId !== action.lineId) };
    case "price":
      return { ...state, lines: state.lines.map((line) => line.lineId === action.lineId
        ? { ...line, dailyPrice: action.dailyPrice }
        : line) };
    case "clear":
      return { ...state, lines: [] };
  }
}

interface CartContextValue {
  lines: CartLine[];
  isReady: boolean;
  addLine: (line: Omit<CartLine, "lineId">) => void;
  setQuantity: (lineId: string, quantity: number) => void;
  updatePrice: (lineId: string, dailyPrice: number) => void;
  removeLine: (lineId: string) => void;
  clear: () => void;
}

const CartContext = createContext<CartContextValue | null>(null);
const CART_KEY_PREFIX = "equipmenthub-cart:";

function readCart(key: string): CartLine[] {
  try {
    const value: unknown = JSON.parse(localStorage.getItem(key) ?? "[]");
    const result = z.array(cartLineSchema).safeParse(value);
    return result.success ? result.data : [];
  } catch {
    return [];
  }
}

export function CartProvider({ children }: { children: ReactNode }) {
  const { status, identity } = useSession();
  const customerId = status === "authenticated" && identity?.role === "CUSTOMER" ? identity.id : null;
  const storageKey = customerId ? `${CART_KEY_PREFIX}${customerId}` : null;
  const [state, dispatch] = useReducer(cartReducer, { ownerId: null, lines: [] });

  useEffect(() => {
    if (!storageKey) {
      dispatch({ type: "load", ownerId: null, lines: [] });
      return;
    }
    dispatch({ type: "load", ownerId: customerId, lines: readCart(storageKey) });
  }, [customerId, storageKey]);

  useEffect(() => {
    if (storageKey && state.ownerId === customerId) {
      try {
        localStorage.setItem(storageKey, JSON.stringify(state.lines));
      } catch {
        // The in-memory cart remains usable when browser storage is unavailable.
      }
    }
  }, [customerId, state, storageKey]);

  const addLine = useCallback((line: Omit<CartLine, "lineId">) => {
    dispatch({ type: "add", line: { ...line, lineId: crypto.randomUUID() } });
  }, []);
  const setQuantity = useCallback((lineId: string, quantity: number) => {
    dispatch({ type: "quantity", lineId, quantity });
  }, []);
  const updatePrice = useCallback((lineId: string, dailyPrice: number) => {
    dispatch({ type: "price", lineId, dailyPrice });
  }, []);
  const removeLine = useCallback((lineId: string) => dispatch({ type: "remove", lineId }), []);
  const clear = useCallback(() => dispatch({ type: "clear" }), []);

  const value = useMemo(() => ({
    lines: customerId && state.ownerId === customerId ? state.lines : [],
    isReady: !customerId || state.ownerId === customerId,
    addLine,
    setQuantity,
    updatePrice,
    removeLine,
    clear,
  }), [addLine, clear, customerId, removeLine, setQuantity, state, updatePrice]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const cart = useContext(CartContext);
  if (!cart) throw new Error("useCart must be used within CartProvider");
  return cart;
}
