import { useEffect, useState } from 'react'
import { getApplicationHealth } from '../api.js'
import Button from '../components/Button.jsx'

export default function HealthPage() {
  const [health, setHealth] = useState({ state: 'loading' })

  useEffect(() => {
    const controller = new AbortController()
    getApplicationHealth(controller.signal)
      .then((result) => setHealth({ state: 'ready', result }))
      .catch((error) => {
        if (error.name !== 'AbortError') setHealth({ state: 'error', message: error.message })
      })
    return () => controller.abort()
  }, [])

  return (
    <main id="main-content" className="min-h-screen bg-page px-5 py-16 text-ink sm:py-24">
      <section className="mx-auto max-w-2xl rounded-[1.5rem] border border-border bg-white p-7 shadow-card sm:p-10">
        <p className="text-xs font-bold uppercase tracking-[0.17em] text-teal">GadgetGo · System status</p>
        <h1 className="mt-3 text-3xl font-semibold tracking-[-0.04em]">Application health</h1>
        <p className="mt-3 leading-7 text-secondary">This page checks the application endpoint only; it does not report database health.</p>
        <div className="mt-7 rounded-2xl bg-page p-5" aria-live="polite">
          {health.state === 'loading' && <p className="text-secondary">Checking the application API…</p>}
          {health.state === 'error' && <p className="text-error">Could not reach the backend: {health.message}</p>}
          {health.state === 'ready' && <p className="font-semibold text-success">{health.result.application}: {health.result.status}</p>}
        </div>
        <Button href="/" variant="secondary" className="mt-7">Return to GadgetGo</Button>
      </section>
    </main>
  )
}
