import SiteFooter from '../components/SiteFooter.jsx'
import SiteHeader from '../components/SiteHeader.jsx'
import CategorySection from '../sections/CategorySection.jsx'
import ClosingCtaSection from '../sections/ClosingCtaSection.jsx'
import FeaturedServicesSection from '../sections/FeaturedServicesSection.jsx'
import HeroSection from '../sections/HeroSection.jsx'
import HowItWorksSection from '../sections/HowItWorksSection.jsx'
import TrustSection from '../sections/TrustSection.jsx'

export default function HomePage() {
  return (
    <>
      <SiteHeader />
      <main id="main-content">
        <HeroSection />
        <CategorySection />
        <FeaturedServicesSection />
        <HowItWorksSection />
        <TrustSection />
        <ClosingCtaSection />
      </main>
      <SiteFooter />
    </>
  )
}
