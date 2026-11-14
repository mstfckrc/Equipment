"use client";

import { ViewTransition, type ReactNode } from "react";

export function PageTransition({ children, animateUpdates = true }: { children: ReactNode; animateUpdates?: boolean }) {
  return <ViewTransition default="none" enter="page-fade" exit="page-fade" update={animateUpdates ? "page-fade" : "none"}>{children}</ViewTransition>;
}
