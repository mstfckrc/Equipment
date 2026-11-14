"use client";

import { useEffect, useState } from "react";
import { ArrowUp } from "lucide-react";

export function BackToTop() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    const onScroll = () => setVisible(window.scrollY > 480);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  function goToTop() {
    document.getElementById("main-content")?.focus({ preventScroll: true });
    window.scrollTo({ top: 0, behavior: window.matchMedia("(prefers-reduced-motion: reduce)").matches ? "instant" : "smooth" });
  }

  return (
    <button type="button" className={`back-to-top ${visible ? "is-visible" : ""}`}
      aria-label="Yukarı çık" aria-hidden={!visible} tabIndex={visible ? 0 : -1} onClick={goToTop}>
      <ArrowUp aria-hidden="true" className="size-5" />
    </button>
  );
}
