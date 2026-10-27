import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { createCustomerGadget, getCatalogCategories, getCustomerGadget, updateCustomerGadget } from '../api.js'
import AccountLayout from '../components/AccountLayout.jsx'

const empty = { categoryId: '', brand: '', model: '', purchaseDate: '', warrantyExpiryDate: '' }
export default function GadgetFormPage() {
  const { id } = useParams()
  const editing = Boolean(id)
  const navigate = useNavigate()
  const [form, setForm] = useState(empty)
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  useEffect(() => { let active = true; Promise.all([getCatalogCategories(), editing ? getCustomerGadget(id) : Promise.resolve(null)]).then(([values, gadget]) => { if (!active) return; setCategories(values); if (gadget) setForm({ categoryId: String(gadget.category.id), brand: gadget.brand, model: gadget.model, purchaseDate: gadget.purchaseDate || '', warrantyExpiryDate: gadget.warrantyExpiryDate || '' }) }).catch(e => { if (active) setError(e.message || 'Could not load the gadget form.') }).finally(() => { if (active) setLoading(false) }); return () => { active = false } }, [id, editing])
  async function submit(event) { event.preventDefault(); setError(''); if (!form.categoryId || !form.brand.trim() || !form.model.trim()) { setError('Choose a device category and enter its brand and model.'); return } if (form.purchaseDate && form.purchaseDate > new Date().toISOString().slice(0, 10)) { setError('Purchase date cannot be in the future.'); return } if (form.purchaseDate && form.warrantyExpiryDate && form.warrantyExpiryDate < form.purchaseDate) { setError('Warranty expiry must be on or after purchase date.'); return } const payload = { categoryId: Number(form.categoryId), brand: form.brand, model: form.model, purchaseDate: form.purchaseDate || null, warrantyExpiryDate: form.warrantyExpiryDate || null }; setBusy(true); try { if (editing) await updateCustomerGadget(id, payload); else await createCustomerGadget(payload); navigate('/account/gadgets', { replace: true }) } catch (e) { setError(e.message || 'Could not save this gadget.') } finally { setBusy(false) } }
  if (loading) return <AccountLayout><p role="status">Loading gadget details…</p></AccountLayout>
  return <AccountLayout><div className="mx-auto max-w-2xl"><Link to="/account/gadgets" className="text-sm font-bold text-teal underline">← My Gadgets</Link><h1 className="mt-4 text-3xl font-bold">{editing ? 'Edit gadget' : 'Register a gadget'}</h1><p className="mt-2 text-secondary">Add the details you want to keep with your account.</p><form onSubmit={submit} className="mt-7 space-y-5 rounded-3xl border border-border bg-white p-6 shadow-card sm:p-8" noValidate>
    <label className="block text-sm font-semibold">Device category<select required value={form.categoryId} onChange={e=>setForm({...form,categoryId:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border bg-white px-4"><option value="">Choose a category</option>{categories.map(c=><option key={c.id} value={c.id}>{c.name}</option>)}</select></label>
    <label className="block text-sm font-semibold">Brand / manufacturer<input required maxLength="100" value={form.brand} onChange={e=>setForm({...form,brand:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /></label>
    <label className="block text-sm font-semibold">Model<input required maxLength="160" value={form.model} onChange={e=>setForm({...form,model:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /></label>
    <div className="grid gap-5 sm:grid-cols-2"><label className="block text-sm font-semibold">Purchase date <span className="font-normal text-muted">(optional)</span><input type="date" value={form.purchaseDate} onChange={e=>setForm({...form,purchaseDate:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /></label><label className="block text-sm font-semibold">Warranty expiry <span className="font-normal text-muted">(optional)</span><input type="date" value={form.warrantyExpiryDate} onChange={e=>setForm({...form,warrantyExpiryDate:e.target.value})} className="mt-2 min-h-12 w-full rounded-xl border border-border px-4" /></label></div>
    {error && <p id="gadget-form-error" role="alert" className="rounded-xl bg-red-50 p-3 text-sm text-error">{error}</p>}<div className="flex flex-wrap gap-3"><button disabled={busy} className="min-h-12 rounded-full bg-ink px-6 font-bold text-white disabled:opacity-60">{busy ? 'Saving…' : editing ? 'Save changes' : 'Register gadget'}</button><Link to="/account/gadgets" className="inline-flex min-h-12 items-center rounded-full border border-border px-6 font-bold">Cancel</Link></div>
  </form></div></AccountLayout>
}
