import { HomeStoreCarousel } from "@/components/storefront/home-store-carousel";
import { HomeIntro } from "@/components/storefront/home-intro";
import { HomeRentalSteps } from "@/components/storefront/home-rental-steps";
import { HomeFaq } from "@/components/storefront/home-faq";
import { HomeStoreInvitation } from "@/components/storefront/home-store-invitation";

export function PublicHome() {
  return (
    <main id="main-content" tabIndex={-1} className="home-page mx-auto w-full max-w-6xl px-5 pb-16 pt-8 sm:px-8 sm:pt-12">
      <HomeIntro />
      <HomeStoreCarousel />
      <HomeRentalSteps />
      <HomeFaq />
      <HomeStoreInvitation />
    </main>
  );
}
