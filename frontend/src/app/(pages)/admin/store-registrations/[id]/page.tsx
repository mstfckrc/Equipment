import { AdminRegistrationDetail } from "@/components/admin/admin-registration-detail";

export default async function Page({ params, searchParams }: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ returnTo?: string | string[] }>;
}) {
  const [{ id }, query] = await Promise.all([params, searchParams]);
  return <AdminRegistrationDetail key={id} id={id} returnTo={typeof query.returnTo === "string" ? query.returnTo : undefined} />;
}
