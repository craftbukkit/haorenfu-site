import {
  DownloadCards,
  EngineeringShowcase,
  FaqAccordion,
  HeroSection,
  NewPlayerTimeline,
  RulesSection,
  Sel4TechPanel,
  ServerStatusCard,
  SiteFooter,
  SiteHeader
} from '@/components/sections';

export default function HomePage() {
  return (
    <main>
      <SiteHeader />
      <HeroSection />
      <section className='mx-auto max-w-6xl space-y-14 px-4 py-10'>
        <ServerStatusCard />
        <EngineeringShowcase />
        <NewPlayerTimeline />
        <DownloadCards />
        <Sel4TechPanel />
        <RulesSection />
        <FaqAccordion />
      </section>
      <SiteFooter />
    </main>
  );
}
