import { AdminAccountDetail } from "@/components/admin/admin-account-detail";

export default async function Page({ params, searchParams }: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ returnTo?: string | string[] }>;
}) {
  const [{ id }, query] = await Promise.all([params, searchParams]);
  return <AdminAccountDetail key={id} id={id} returnTo={typeof query.returnTo === "string" ? query.returnTo : undefined} />;
}
