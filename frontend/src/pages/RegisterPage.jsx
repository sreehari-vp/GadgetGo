import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'
import AccountLayout from '../components/AccountLayout.jsx'

export default function RegisterPage() {
  const { register, status } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ fullName: '', email: '', password: '', confirmPassword: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  if (status === 'authenticated') return <Navigate to="/account/gadgets" replace />

  async function submit(event) {
    event.preventDefault(); setError('')
    if (form.fullName.trim().length < 2) { setError('Enter your name (at least 2 characters).'); return }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) { setError('Enter a valid email address.'); return }
    if (form.password.length < 10) { setError('Use a password with at least 10 characters.'); return }
    if (new TextEncoder().encode(form.password).length > 72) { setError('Password must be 72 UTF-8 bytes or fewer.'); return }
    if (form.password !== form.confirmPassword) { setError('Passwords do not match.'); return }
    setBusy(true)
    try { await register({ fullName: form.fullName, email: form.email, password: form.password }); navigate('/login', { replace: true, state: { registered: true } }) }
    catch (cause) { setError(cause.status === 409 ? 'An account with this email already exists.' : cause.message || 'Unable to create your account. Please try again.') }
    finally { setBusy(false) }
  }

  return <AccountLayout><section className="mx-auto max-w-lg rounded-3xl border border-border bg-white p-6 shadow-card sm:p-9">
    <p className="text-sm font-bold uppercase tracking-[.14em] text-teal">Get started</p><h1 className="mt-2 text-3xl font-bold tracking-tight">Create your account</h1><p className="mt-2 text-secondary">One account for your details and your gadgets.</p>
    <form className="mt-7 space-y-4" onSubmit={submit} noValidate>
      <label className="block text-sm font-semibold">Full name<input autoComplete="name" maxLength="120" value={form.fullName} onChange={e=>setForm({...form,fullName:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /></label>
      <label className="block text-sm font-semibold">Email address<input autoComplete="email" type="email" maxLength="254" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /></label>
      <label className="block text-sm font-semibold">Password<input autoComplete="new-password" type="password" value={form.password} onChange={e=>setForm({...form,password:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /><span className="mt-1 block text-xs font-normal text-muted">At least 10 characters. Password recovery is not available yet.</span></label>
      <label className="block text-sm font-semibold">Confirm password<input autoComplete="new-password" type="password" value={form.confirmPassword} onChange={e=>setForm({...form,confirmPassword:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /></label>
      {error && <p id="registration-error" role="alert" className="rounded-xl bg-red-50 p-3 text-sm text-error">{error}</p>}
      <button disabled={busy} className="min-h-12 w-full rounded-full bg-accent px-5 font-bold text-ink disabled:opacity-60">{busy ? 'Creating account…' : 'Create account'}</button>
    </form><p className="mt-6 text-center text-sm text-secondary">Already have an account? <Link className="font-bold text-teal underline" to="/login">Log in</Link></p><p className="mt-3 text-center text-sm"><Link className="font-semibold text-teal underline" to="/services">Explore services</Link></p>
  </section></AccountLayout>
}
