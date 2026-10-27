import { Link } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'
import Icon from './Icon.jsx'
import { roleHome } from './ProtectedRoute.jsx'

export default function SiteFooter() {
  const { customer, identity } = useAuth()
  const profilePath = identity?.role === 'TECHNICIAN' ? '/technician/profile'
    : identity?.role === 'SUPPLIER' ? '/supplier/profile' : '/account/profile'
  const dashboardPath = identity?.role === 'TECHNICIAN' || identity?.role === 'SUPPLIER' ? roleHome(identity.role) : '/account/gadgets'

  return (
    <>
      <footer className="bg-ink text-white">
        <div className="page-container grid gap-10 py-12 sm:grid-cols-2 lg:grid-cols-[1.4fr_1fr_1fr] lg:gap-16 lg:py-16">
          <div className="max-w-sm">
            <a href="/" className="inline-flex items-center gap-2.5 rounded-lg text-white" aria-label="GadgetGo home">
              <span className="grid size-9 place-items-center rounded-xl bg-white/10 text-accent"><Icon name="sparkle" size={20} /></span>
              <span className="text-xl font-bold tracking-[-0.06em]">Gadget<span className="text-accent">Go</span></span>
            </a>
            <p className="mt-4 max-w-xs text-sm leading-6 text-white/70">A clearer place to begin exploring electronics repair. Your Gadget. Fixed. Simple.</p>
          </div>
          <div>
            <h2 className="text-sm font-semibold text-white">Explore</h2>
            <ul className="mt-4 space-y-3 text-sm text-white/70">
              <li><a className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" href="/services">Repair services</a></li>
              <li><a className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" href="/#device-categories">Device categories</a></li>
              <li><a className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" href="/#how-it-works">How it works</a></li>
            </ul>
          </div>
          <div>
            <h2 className="text-sm font-semibold text-white">Your space</h2>
            <ul className="mt-4 space-y-3 text-sm text-white/70">
              <li><Link className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" to={identity ? profilePath : '/login'}>{identity ? 'Your profile' : 'Log in'}</Link></li>
              <li><Link className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" to={identity ? dashboardPath : '/register'}>{customer ? 'My Gadgets' : identity ? 'Dashboard' : 'Get started'}</Link></li>
              {customer && <li><Link className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" to="/account/bookings">My Bookings</Link></li>}
              {!identity && <><li><Link className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" to="/technician/login">Technician sign in</Link></li><li><Link className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" to="/supplier/login">Supplier sign in</Link></li></>}
              <li><a className="rounded-sm hover:text-white focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent" href="/health">System status</a></li>
            </ul>
          </div>
        </div>
        <div className="border-t border-white/15">
          <div className="page-container flex flex-col gap-2 py-5 text-xs text-white/55 sm:flex-row sm:items-center sm:justify-between">
            <p>© {new Date().getFullYear()} GadgetGo. A simpler start for device repair.</p>
            <p>Service catalogue & smart repair booking</p>
          </div>
        </div>
      </footer>
    </>
  )
}
