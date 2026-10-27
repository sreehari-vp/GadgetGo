import Icon from '../components/Icon.jsx'
import SectionHeading from '../components/SectionHeading.jsx'
import { productPrinciples } from '../data/homepagePreview.js'

export default function TrustSection() {
  return (
    <section className="page-container pb-10 sm:pb-14">
      <div className="grid overflow-hidden rounded-[2rem] bg-[#e9eee7] lg:grid-cols-[0.9fr_1.1fr]">
        <div className="p-7 sm:p-10 lg:p-12">
          <SectionHeading
            eyebrow="Built around clear choices"
            title="Straight answers from the start."
            description="Service descriptions help you explore repair topics. They do not replace an assessment or confirm a price."
          />
          <div className="mt-7 inline-flex items-center gap-2 rounded-full bg-white px-4 py-2.5 text-xs font-semibold text-ink shadow-card">
            <Icon name="shield" size={17} className="text-teal" /> Clear scope, without unsupported promises
          </div>
        </div>
        <div className="grid gap-px bg-ink/10 lg:grid-rows-3">
          {productPrinciples.map((principle, index) => (
            <article key={principle.title} className="flex gap-4 bg-white/75 p-6 sm:p-8 lg:px-10">
              <span className={`mt-0.5 grid size-11 shrink-0 place-items-center rounded-xl ${index === 1 ? 'bg-[#edf5d4] text-ink' : 'bg-[#e0efeb] text-teal'}`}><Icon name={principle.icon} size={21} /></span>
              <div>
                <h3 className="font-semibold text-ink">{principle.title}</h3>
                <p className="mt-1.5 max-w-lg text-sm leading-6 text-secondary">{principle.description}</p>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  )
}
