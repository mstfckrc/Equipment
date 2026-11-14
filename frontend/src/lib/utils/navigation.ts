// Parsing base only; these helpers do not make network requests.
const localOrigin = "https://equipmenthub.invalid";

export function readReturnTo(value: string | string[] | undefined, allowedPaths: readonly string[], fallback: string) {
  if (typeof value !== "string" || value.length > 2048 || !value.startsWith("/") || value.startsWith("//") || /[\\\u0000-\u001f]/.test(value)) return fallback;
  try {
    const url = new URL(value, localOrigin);
    if (url.origin !== localOrigin || !allowedPaths.includes(url.pathname)) return fallback;
    return `${url.pathname}${url.search}${url.hash}`;
  } catch {
    return fallback;
  }
}

export function withReturnTo(href: string, returnTo: string) {
  const url = new URL(href, localOrigin);
  url.searchParams.set("returnTo", returnTo);
  return `${url.pathname}${url.search}${url.hash}`;
}
