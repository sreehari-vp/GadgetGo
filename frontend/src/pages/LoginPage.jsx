import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'
import AccountLayout from '../components/AccountLayout.jsx'
import { roleHome } from '../components/ProtectedRoute.jsx'

const portalLabels = {
  CUSTOMER: { title: 'Log in to GadgetGo', eyebrow: 'Welcome back', description: 'Manage your account and registered gadgets.' },
  TECHNICIAN: { title: 'Technician sign in', eyebrow: 'Technician portal', description: 'View your profile and configured repair capabilities.' },
  SUPPLIER: { title: 'Supplier sign in', eyebrow: 'Supplier portal', description: 'View your supplier profile and account status.' },
}

export default function LoginPage({ expectedRole = 'CUSTOMER' }) {
  const { identity, login, status } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  if (status === 'authenticated' && identity) return <Navigate to={roleHome(identity.role)} replace />
  const portal = portalLabels[expectedRole]

  async function submit(event) {
    event.preventDefault(); setError('')
    if (!form.email.trim() || !form.password) { setError('Enter your email and password.'); return }
    setBusy(true)
    try { await login({ email: form.email, password: form.password }, expectedRole); navigate(location.state?.from || roleHome(expectedRole), { replace: true }) }
    catch (cause) { setError(cause.status === 401 ? 'Email or password is incorrect for this sign-in page.' : cause.message || 'Unable to sign in. Please try again.') }
    finally { setBusy(false) }
  }

  return <AccountLayout><section className="mx-auto max-w-md rounded-3xl border border-border bg-white p-6 shadow-card sm:p-9">
    <p className="text-sm font-bold uppercase tracking-[.14em] text-teal">{portal.eyebrow}</p><h1 className="mt-2 text-3xl font-bold tracking-tight">{portal.title}</h1><p className="mt-2 text-secondary">{portal.description}</p>
    <form className="mt-7 space-y-5" onSubmit={submit} noValidate>
      <label className="block text-sm font-semibold">Email address<input autoComplete="email" type="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} aria-describedby={error ? 'login-error' : undefined} className="mt-2 min-h-12 w-full rounded-xl border border-border bg-white px-4" /></label>
      <label className="block text-sm font-semibold">Password<input autoComplete="current-password" type="password" value={form.password} onChange={e=>setForm({...form,password:e.target.value})} aria-describedby={error ? 'login-error' : undefined} className="mt-2 min-h-12 w-full rounded-xl border border-border bg-white px-4" /></label>
      {error && <p id="login-error" role="alert" className="rounded-xl bg-red-50 p-3 text-sm text-error">{error}</p>}
      <button disabled={busy} className="min-h-12 w-full rounded-full bg-accent px-5 font-bold text-ink disabled:opacity-60">{busy ? 'Signing in…' : 'Log in'}</button>
    </form>{expectedRole === 'CUSTOMER' && <p className="mt-6 text-center text-sm text-secondary">New to GadgetGo? <Link className="font-bold text-teal underline" to="/register">Create an account</Link></p>}
    <p className="mt-3 text-center text-sm"><Link className="font-semibold text-teal underline" to="/services">Browse repair services</Link></p>
  </section></AccountLayout>
}
