import Icon from '../components/Icon.jsx'
import SectionHeading from '../components/SectionHeading.jsx'
import { discoverySteps } from '../data/homepagePreview.js'

export default function HowItWorksSection() {
  return (
    <section id="how-it-works" className="section-space bg-white">
      <div className="page-container">
        <SectionHeading
          eyebrow="A straightforward starting point"
          title="A simpler path to understanding your options."
          description="Explore repair information at your own pace. Booking, diagnosis, and repair fulfilment are not part of this catalogue."
        />
        <ol className="mt-9 grid gap-4 md:grid-cols-3 lg:mt-12">
          {discoverySteps.map((step) => (
            <li key={step.number} className="relative rounded-[1.4rem] border border-border bg-page p-6 sm:p-7">
              <div className="flex items-start justify-between gap-4">
                <span className="grid size-12 place-items-center rounded-2xl bg-white text-teal shadow-card"><Icon name={step.icon} size={23} /></span>
                <span className="text-3xl font-semibold tracking-[-0.06em] text-ink/20">{step.number}</span>
              </div>
              <h3 className="mt-7 text-xl font-semibold tracking-[-0.025em] text-ink">{step.title}</h3>
              <p className="mt-2 text-sm leading-6 text-secondary">{step.description}</p>
            </li>
          ))}
        </ol>
        <p className="mt-5 text-xs text-muted">GadgetGo currently supports service discovery only. Booking, technician matching, diagnosis, and repair tracking are not available.</p>
      </div>
    </section>
  )
}
