import { useEffect, useRef } from 'react'
import Button from './Button.jsx'
import Icon from './Icon.jsx'

export default function FeatureNoticeDialog({ notice, onClose, id }) {
  const dialogRef = useRef(null)

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (notice && !dialog.open) dialog.showModal()
    if (!notice && dialog.open) dialog.close()
  }, [notice])

  return (
    <dialog
      ref={dialogRef}
      className="notice-dialog"
      aria-labelledby={`${id}-title`}
      onClose={onClose}
      onClick={(event) => {
        if (event.target === dialogRef.current) onClose()
      }}
    >
      {notice && (
        <div className="relative p-7 sm:p-9">
          <button
            type="button"
            className="absolute right-5 top-5 grid size-10 place-items-center rounded-full text-secondary transition-colors hover:bg-surface-soft hover:text-ink focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-teal"
            onClick={onClose}
            aria-label="Close message"
            autoFocus
          >
            <Icon name="close" size={19} />
          </button>
          <span className="mb-5 grid size-12 place-items-center rounded-2xl bg-accent text-ink"><Icon name="sparkle" size={23} /></span>
          <p className="mb-2 text-xs font-bold uppercase tracking-[0.16em] text-teal">Coming in a later stage</p>
          <h2 id={`${id}-title`} className="max-w-sm pr-5 text-2xl font-semibold tracking-[-0.03em] text-ink">{notice.title}</h2>
          <p className="mt-3 max-w-md leading-7 text-secondary">{notice.description}</p>
          <Button className="mt-7 w-full sm:w-auto" onClick={onClose}>Got it</Button>
        </div>
      )}
    </dialog>
  )
}
