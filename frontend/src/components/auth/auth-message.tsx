export function AuthMessage({ children }: { children: string }) {
  return (
    <p role="alert" className="notice-danger rounded-lg px-3 py-2.5 text-sm leading-5">
      {children}
    </p>
  );
}
