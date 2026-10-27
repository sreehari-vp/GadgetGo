import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react'
import { getCurrentCustomer, loginCustomer, loginSupplier, loginTechnician, logoutCustomer, registerCustomer } from '../api.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [identity, setIdentity] = useState(null)
  const [status, setStatus] = useState('loading')
  const initialized = useRef(false)

  const refresh = useCallback(async () => {
    setStatus('loading')
    try {
      setIdentity(await getCurrentCustomer())
      setStatus('authenticated')
    } catch (error) {
      setIdentity(null)
      setStatus(error.status === 401 ? 'unauthenticated' : 'error')
    }
  }, [])

  useEffect(() => {
    if (initialized.current) return
    initialized.current = true
    refresh()
  }, [refresh])

  useEffect(() => {
    const expireSession = () => { setIdentity(null); setStatus('unauthenticated') }
    window.addEventListener('gadgetgo:unauthorized', expireSession)
    return () => window.removeEventListener('gadgetgo:unauthorized', expireSession)
  }, [])

  const login = useCallback(async (credentials, role = 'CUSTOMER') => {
    const loginForRole = role === 'TECHNICIAN' ? loginTechnician
      : role === 'SUPPLIER' ? loginSupplier : loginCustomer
    const result = await loginForRole(credentials)
    if (result.role !== role) throw new Error('The account is not available for this sign-in page.')
    setIdentity(result)
    setStatus('authenticated')
    return result
  }, [])

  const register = useCallback(async (details) => registerCustomer(details), [])

  const logout = useCallback(async () => {
    await logoutCustomer()
    setIdentity(null)
    setStatus('unauthenticated')
  }, [])

  const customer = identity?.role === 'CUSTOMER' ? identity : null
  const value = useMemo(() => ({ identity, customer, status, login, register, logout, refresh }), [identity, customer, status, login, register, logout, refresh])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider.')
  return context
}
