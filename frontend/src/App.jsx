import { useEffect } from 'react'
import { BrowserRouter, Route, Routes, useLocation } from 'react-router'
import { AuthProvider } from './auth/AuthContext.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import HealthPage from './pages/HealthPage.jsx'
import HomePage from './pages/HomePage.jsx'
import CataloguePage from './pages/CataloguePage.jsx'
import ServiceDetailPage from './pages/ServiceDetailPage.jsx'
import BookingPage from './pages/BookingPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'
import GadgetsPage from './pages/GadgetsPage.jsx'
import GadgetFormPage from './pages/GadgetFormPage.jsx'
import CustomerBookingsPage from './pages/CustomerBookingsPage.jsx'
import TechnicianDashboardPage from './pages/TechnicianDashboardPage.jsx'
import TechnicianProfilePage from './pages/TechnicianProfilePage.jsx'
import SupplierDashboardPage from './pages/SupplierDashboardPage.jsx'
import SupplierProfilePage from './pages/SupplierProfilePage.jsx'

function RouteTitle() {
  const { pathname } = useLocation()
  useEffect(() => {
    const page = pathname.includes('/book') ? 'Book repair'
      : pathname.startsWith('/services/') ? 'Service details'
      : pathname === '/services' ? 'Repair services'
      : pathname === '/health' ? 'System status'
            : pathname.startsWith('/technician/login') ? 'Technician sign in'
              : pathname.startsWith('/supplier/login') ? 'Supplier sign in'
                : pathname.startsWith('/technician') ? 'Technician portal'
                  : pathname.startsWith('/supplier') ? 'Supplier portal'
            : pathname === '/login' ? 'Log in'
          : pathname === '/register' ? 'Create account'
            : pathname === '/account/profile' ? 'Your profile'
              : pathname === '/account/bookings' ? 'My Bookings'
              : pathname.startsWith('/account/gadgets') ? 'My Gadgets' : 'Your gadget, fixed. Simple.'
    document.title = `GadgetGo | ${page}`
  }, [pathname])
  return null
}

export default function App() {
  return (
    <BrowserRouter><AuthProvider>
      <RouteTitle />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/health" element={<HealthPage />} />
        <Route path="/services" element={<CataloguePage />} />
        <Route path="/services/:slug" element={<ServiceDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/technician/login" element={<LoginPage expectedRole="TECHNICIAN" />} />
        <Route path="/supplier/login" element={<LoginPage expectedRole="SUPPLIER" />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route element={<ProtectedRoute requiredRole="CUSTOMER" />}>
          <Route path="/services/:slug/book" element={<BookingPage />} />
          <Route path="/account/bookings" element={<CustomerBookingsPage />} />
          <Route path="/account/profile" element={<ProfilePage />} />
          <Route path="/account/gadgets" element={<GadgetsPage />} />
          <Route path="/account/gadgets/new" element={<GadgetFormPage />} />
          <Route path="/account/gadgets/:id/edit" element={<GadgetFormPage />} />
        </Route>
        <Route element={<ProtectedRoute requiredRole="TECHNICIAN" />}>
          <Route path="/technician/dashboard" element={<TechnicianDashboardPage />} />
          <Route path="/technician/profile" element={<TechnicianProfilePage />} />
        </Route>
        <Route element={<ProtectedRoute requiredRole="SUPPLIER" />}>
          <Route path="/supplier/dashboard" element={<SupplierDashboardPage />} />
          <Route path="/supplier/profile" element={<SupplierProfilePage />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </AuthProvider></BrowserRouter>
  )
}
