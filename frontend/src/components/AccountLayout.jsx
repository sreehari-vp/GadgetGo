import SiteHeader from './SiteHeader.jsx'
import SiteFooter from './SiteFooter.jsx'

export default function AccountLayout({ children }) {
  return <><SiteHeader /><main id="main-content" className="page-container min-h-[65vh] py-10 sm:py-14">{children}</main><SiteFooter /></>
}
