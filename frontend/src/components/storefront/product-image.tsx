"use client";

import { useState } from "react";
import Image from "next/image";
import { ImageOff } from "lucide-react";

function publicImageUrl(value: string | null) {
  if (!value) return null;
  try {
    const url = new URL(value);
    return ["http:", "https:"].includes(url.protocol) && !url.username && !url.password ? url.href : null;
  } catch {
    return null;
  }
}

export function ProductImage({ imageUrl, name }: { imageUrl: string | null; name: string }) {
  const [failedUrl, setFailedUrl] = useState<string | null>(null);
  const url = publicImageUrl(imageUrl);

  return (
    <div className="product-card-image">
      {url && url !== failedUrl ? (
        <Image src={url} alt={name} width={640} height={480} unoptimized loading="lazy"
          referrerPolicy="no-referrer" onError={() => setFailedUrl(url)} />
      ) : (
        <div className="product-image-placeholder" aria-hidden="true">
          <ImageOff className="size-8" strokeWidth={1.3} /><span>Görsel bulunmuyor</span>
        </div>
      )}
    </div>
  );
}
