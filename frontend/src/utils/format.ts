export function formatDate(value: string) {
  const date = new Date(`${value.slice(0, 10)}T00:00:00`)
  return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat('en-ZA', { day: 'numeric', month: 'short', year: 'numeric' }).format(date)
}

export function formatTimestamp(value: string) {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat('en-ZA', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' }).format(date)
}

export function formatHours(hours: number) {
  if (!Number.isFinite(hours)) return '—'
  const clean = Math.round(hours * 100) / 100
  return `${new Intl.NumberFormat('en-ZA', { maximumFractionDigits: 2 }).format(clean)} h`
}

export function titleCase(value: string) {
  return value.toLowerCase().replaceAll('_', ' ').replace(/\b\p{L}/gu, letter => letter.toUpperCase())
}

