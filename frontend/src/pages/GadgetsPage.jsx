import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router'
import { deleteCustomerGadget, getCustomerGadgets } from '../api.js'
import AccountLayout from '../components/AccountLayout.jsx'

export default function GadgetsPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [deleting, setDeleting] = useState(null)
  const load = useCallback(async () => { setLoading(true); setError(''); try { setItems(await getCustomerGadgets()) } catch (e) { setError(e.message || 'Could not load your gadgets.') } finally { setLoading(false) } }, [])
  useEffect(() => { load() }, [load])
  async function remove(item) { if (!window.confirm(`Delete ${item.brand} ${item.model}?`)) return; setDeleting(item.id); setError(''); try { await deleteCustomerGadget(item.id); setItems(current => current.filter(g => g.id !== item.id)) } catch (e) { setError(e.message || 'The gadget could not be deleted.') } finally { setDeleting(null) } }
  return <AccountLayout><div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between"><div><p className="text-sm font-bold uppercase tracking-[.14em] text-teal">Your account</p><h1 className="mt-2 text-3xl font-bold">My Gadgets</h1><p className="mt-2 text-secondary">Keep the devices you own in one place.</p></div><Link to="/account/gadgets/new" className="inline-flex min-h-12 items-center justify-center rounded-full bg-accent px-6 font-bold text-ink">Register a gadget</Link></div>
    {error && <p role="alert" className="mt-6 rounded-xl bg-red-50 p-4 text-sm text-error">{error}</p>}{loading && <p className="mt-8" role="status">Loading your gadgets…</p>}
    {!loading && !error && items.length === 0 && <section className="mt-9 rounded-3xl border border-dashed border-border bg-white px-6 py-12 text-center"><h2 className="text-xl font-bold">No gadgets registered yet</h2><p className="mx-auto mt-2 max-w-md text-secondary">Add a device to keep its details handy when you explore repair services.</p><Link to="/account/gadgets/new" className="mt-6 inline-flex min-h-12 items-center justify-center rounded-full bg-ink px-6 font-bold text-white">Register your first gadget</Link></section>}
    {!loading && items.length > 0 && <ul className="mt-8 grid gap-4 md:grid-cols-2">{items.map(item => <li key={item.id} className="rounded-3xl border border-border bg-white p-5 shadow-card sm:p-6"><p className="text-sm font-semibold text-teal">{item.category.name}</p><h2 className="mt-2 break-words text-xl font-bold">{item.brand} {item.model}</h2><dl className="mt-4 grid gap-2 text-sm text-secondary">{item.purchaseDate && <div className="flex justify-between gap-3"><dt>Purchased</dt><dd>{item.purchaseDate}</dd></div>}{item.warrantyExpiryDate && <div className="flex justify-between gap-3"><dt>Warranty ends</dt><dd>{item.warrantyExpiryDate}</dd></div>}</dl><div className="mt-5 flex gap-3 border-t border-border pt-4"><Link to={`/account/gadgets/${item.id}/edit`} className="min-h-11 rounded-full border border-border px-5 py-2.5 text-sm font-bold">Edit</Link><button disabled={deleting === item.id} onClick={()=>remove(item)} className="min-h-11 rounded-full px-4 text-sm font-bold text-error disabled:opacity-50">{deleting === item.id ? 'Deleting…' : 'Delete'}</button></div></li>)}</ul>}
  </AccountLayout>
}
