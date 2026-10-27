import Button from '../components/Button.jsx'
import Icon from '../components/Icon.jsx'

export default function ClosingCtaSection() {
  return (
    <section className="page-container pb-16 pt-4 sm:pb-20">
      <div className="relative overflow-hidden rounded-[2rem] bg-accent px-6 py-10 sm:px-10 sm:py-12 lg:flex lg:items-center lg:justify-between lg:gap-10 lg:px-14">
        <div aria-hidden="true" className="absolute -right-8 -top-20 size-64 rounded-full border-[36px] border-white/25" />
        <div className="relative max-w-2xl">
          <p className="text-xs font-bold uppercase tracking-[0.17em] text-ink/65">Your next step is simple</p>
          <h2 className="mt-3 text-3xl font-semibold leading-tight tracking-[-0.04em] text-ink sm:text-4xl">Ready to explore a clearer repair journey?</h2>
          <p className="mt-3 max-w-xl leading-7 text-ink/75">Explore the current device categories and service descriptions. No account or booking is needed to browse.</p>
        </div>
        <Button href="/services" variant="primary" className="relative mt-7 min-h-[52px] w-full px-6 lg:mt-0 lg:w-auto">
          Browse repair services <Icon name="arrow" size={18} />
        </Button>
      </div>
    </section>
  )
}
