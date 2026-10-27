import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'
import Button from './Button.jsx'
import Icon from './Icon.jsx'
import { roleHome } from './ProtectedRoute.jsx'

const links = [
  { label: 'Home', href: '/' },
  { label: 'Explore services', href: '/services' },
  { label: 'How it works', href: '/#how-it-works' },
]

function Brand() {
  return (
    <a className="inline-flex items-center gap-2.5 rounded-lg text-ink" href="/" aria-label="GadgetGo home">
      <span className="grid size-9 place-items-center rounded-xl bg-ink text-accent"><Icon name="sparkle" size={20} /></span>
      <span className="text-[1.28rem] font-bold tracking-[-0.06em]">Gadget<span className="text-teal">Go</span></span>
    </a>
  )
}

export default function SiteHeader() {
  const { identity, customer, status, logout } = useAuth()
  const navigate = useNavigate()
  const accountPath = identity?.role === 'TECHNICIAN' ? '/technician/profile'
    : identity?.role === 'SUPPLIER' ? '/supplier/profile' : '/account/profile'
  const accountLabel = identity?.role === 'TECHNICIAN' ? 'Technician profile'
    : identity?.role === 'SUPPLIER' ? 'Supplier profile' : 'Profile'
  const [menuOpen, setMenuOpen] = useState(false)
  const [logoutError, setLogoutError] = useState('')
  const menuButtonRef = useRef(null)

  useEffect(() => {
    if (!menuOpen) return undefined
    const onKeyDown = (event) => {
      if (event.key === 'Escape') {
        setMenuOpen(false)
        menuButtonRef.current?.focus()
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [menuOpen])

  const signOut = async () => {
    setMenuOpen(false); setLogoutError('')
    try { await logout(); navigate('/', { replace: true }) }
    catch (error) { setLogoutError(error.message || 'Could not log out. Please try again.') }
  }

  return (
    <>
      <a href="#main-content" className="skip-link">Skip to content</a>
      <header className="relative z-30 border-b border-border/70 bg-white">
        <div className="page-container flex min-h-[76px] items-center justify-between gap-4">
          <Brand />

          <nav aria-label="Main navigation" className="hidden items-center gap-7 lg:flex">
            {links.map((link) => <a key={link.href} className="rounded-md text-sm font-medium text-secondary transition-colors hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-4" href={link.href}>{link.label}</a>)}
            {customer && (
              <>
                <Link className="rounded-md text-sm font-medium text-secondary transition-colors hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-4" to="/account/gadgets">My Gadgets</Link>
                <Link className="rounded-md text-sm font-medium text-secondary transition-colors hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-4" to="/account/bookings">My Bookings</Link>
              </>
            )}
            {identity && identity.role !== 'CUSTOMER' && <Link className="rounded-md text-sm font-medium text-secondary transition-colors hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal focus-visible:ring-offset-4" to={roleHome(identity.role)}>Dashboard</Link>}
          </nav>

          <div className="hidden items-center gap-2 lg:flex">
            {identity ? <><Link className="rounded-full px-4 py-3 text-sm font-semibold text-secondary hover:text-ink" to={accountPath}>{identity.fullName}</Link><Button variant="quiet" className="min-h-11 px-4" onClick={signOut}>Log out</Button></> : <><Link className="rounded-full px-4 py-3 text-sm font-semibold text-secondary hover:text-ink" to="/login">Log in</Link><Link className="inline-flex min-h-11 items-center rounded-full bg-accent px-5 font-bold text-ink" to="/register">Get started</Link></>}
          </div>

          <button
            ref={menuButtonRef}
            type="button"
            className="grid size-11 place-items-center rounded-full border border-border bg-white text-ink transition-colors hover:bg-surface-soft focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal lg:hidden"
            aria-label={menuOpen ? 'Close main menu' : 'Open main menu'}
            aria-expanded={menuOpen}
            aria-controls="mobile-navigation"
            onClick={() => setMenuOpen((open) => !open)}
          >
            <Icon name={menuOpen ? 'close' : 'menu'} size={21} />
          </button>
        </div>

        <div id="mobile-navigation" hidden={!menuOpen} className="mobile-menu-panel absolute inset-x-0 top-full border-b border-border bg-white shadow-soft lg:hidden">
            <nav aria-label="Mobile navigation" className="page-container flex flex-col gap-1 py-4">
              {links.map((link) => <a key={link.href} href={link.href} onClick={() => setMenuOpen(false)} className="min-h-12 rounded-xl px-4 py-3 text-sm font-semibold text-ink hover:bg-surface-soft focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal">{link.label}</a>)}
              {customer && <><Link to="/account/gadgets" onClick={()=>setMenuOpen(false)} className="min-h-12 rounded-xl px-4 py-3 text-sm font-semibold text-ink hover:bg-surface-soft">My Gadgets</Link><Link to="/account/bookings" onClick={()=>setMenuOpen(false)} className="min-h-12 rounded-xl px-4 py-3 text-sm font-semibold text-ink hover:bg-surface-soft">My Bookings</Link><Link to={accountPath} onClick={()=>setMenuOpen(false)} className="min-h-12 rounded-xl px-4 py-3 text-sm font-semibold text-ink hover:bg-surface-soft">{accountLabel}</Link></>}
              {identity && identity.role !== 'CUSTOMER' && <><Link to={roleHome(identity.role)} onClick={()=>setMenuOpen(false)} className="min-h-12 rounded-xl px-4 py-3 text-sm font-semibold text-ink hover:bg-surface-soft">Dashboard</Link><Link to={accountPath} onClick={()=>setMenuOpen(false)} className="min-h-12 rounded-xl px-4 py-3 text-sm font-semibold text-ink hover:bg-surface-soft">{accountLabel}</Link></>}
              <div className="mt-2 grid grid-cols-2 gap-2 border-t border-border pt-4">
                {identity ? <><Button variant="secondary" onClick={signOut}>Log out</Button><span className="self-center truncate px-2 text-sm text-secondary">{identity.fullName}</span></> : <><Link to="/login" onClick={()=>setMenuOpen(false)} className="inline-flex min-h-11 items-center justify-center rounded-full border border-border px-4 font-semibold">Log in</Link><Link to="/register" onClick={()=>setMenuOpen(false)} className="inline-flex min-h-11 items-center justify-center rounded-full bg-accent px-4 font-bold">Get started</Link></>}
              </div>
            </nav>
        </div>
      </header>
      {logoutError && <p role="alert" className="page-container py-2 text-sm text-error">{logoutError}</p>}
      {status === 'error' && !identity && <span className="sr-only">Account status could not be checked.</span>}
    </>
  )
}
