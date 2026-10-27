const paths = {
  arrow: <><path d="M5 12h14" /><path d="m13 6 6 6-6 6" /></>,
  battery: <><rect x="3" y="7" width="17" height="10" rx="2" /><path d="M23 10v4" /><path d="M7 10v4m4-4v4" /></>,
  check: <path d="m5 12 4 4L19 6" />,
  close: <><path d="m18 6-12 12" /><path d="m6 6 12 12" /></>,
  desktop: <><rect x="3" y="4" width="18" height="13" rx="2" /><path d="M8 21h8m-4-4v4" /></>,
  laptop: <><rect x="5" y="4" width="14" height="12" rx="1.5" /><path d="M2 20h20l-2-4H4l-2 4Z" /></>,
  menu: <><path d="M4 7h16" /><path d="M4 12h16" /><path d="M4 17h16" /></>,
  phone: <><rect x="6" y="2" width="12" height="20" rx="2.5" /><path d="M10 18h4" /></>,
  search: <><circle cx="10.8" cy="10.8" r="6.8" /><path d="m16 16 4.5 4.5" /></>,
  shield: <><path d="M12 22s8-4 8-11V5l-8-3-8 3v6c0 7 8 11 8 11Z" /><path d="m9 12 2 2 4-4" /></>,
  sparkle: <><path d="m12 3 1.7 5.3L19 10l-5.3 1.7L12 17l-1.7-5.3L5 10l5.3-1.7L12 3Z" /><path d="m19 15 .8 2.2L22 18l-2.2.8L19 21l-.8-2.2L16 18l2.2-.8L19 15Z" /></>,
  tablet: <><rect x="4" y="2" width="16" height="20" rx="2.5" /><path d="M11 18h2" /></>,
  watch: <><rect x="6" y="7" width="12" height="10" rx="3" /><path d="m9 2 1 5m5-5-1 5m-5 10-1 5m6-5 1 5" /></>,
}

export default function Icon({ name, size = 24, className = '' }) {
  return (
    <svg
      aria-hidden="true"
      className={className}
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      focusable="false"
    >
      {paths[name] ?? paths.sparkle}
    </svg>
  )
}
