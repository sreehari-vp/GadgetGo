import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuth } from '../auth/AuthContext.jsx'

export function roleHome(role) {
  if (role === 'TECHNICIAN') return '/technician/dashboard'
  if (role === 'SUPPLIER') return '/supplier/dashboard'
  return '/account/gadgets'
}

export default function ProtectedRoute({ requiredRole = 'CUSTOMER' }) {
  const { identity, status } = useAuth()
  const location = useLocation()
  if (status === 'loading') return <main className="page-container py-20" aria-live="polite">Checking your session…</main>
  if (status !== 'authenticated' || !identity) {
    const login = requiredRole === 'TECHNICIAN' ? '/technician/login' : requiredRole === 'SUPPLIER' ? '/supplier/login' : '/login'
    return <Navigate to={login} replace state={{ from: location.pathname }} />
  }
  if (identity.role !== requiredRole) return <Navigate to={roleHome(identity.role)} replace />
  return <Outlet />
}
