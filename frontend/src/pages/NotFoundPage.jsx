import { Link } from 'react-router'
import SiteFooter from '../components/SiteFooter.jsx'
import SiteHeader from '../components/SiteHeader.jsx'

export default function NotFoundPage() {
  return <><SiteHeader /><main id="main-content" className="page-container min-h-[60vh] py-20"><p className="text-xs font-bold uppercase tracking-[0.15em] text-teal">404 · Page not found</p><h1 className="mt-3 text-3xl font-semibold text-ink">We couldn’t find that page.</h1><Link to="/" className="mt-6 inline-flex min-h-11 items-center rounded-full bg-ink px-5 text-sm font-semibold text-white">Return home</Link></main><SiteFooter /></>
}
