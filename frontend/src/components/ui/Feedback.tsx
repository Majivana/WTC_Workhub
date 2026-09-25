import { AlertCircle, CheckCircle2, LoaderCircle } from 'lucide-react'
import type { ReactNode } from 'react'
import { ApiError } from '../../api/client'

export function PageLoading({ label = 'Loading' }: { label?: string }) {
  return <div className="loading-line" role="status"><LoaderCircle className="spin" size={18} /> {label}</div>
}

export function PageError({ error }: { error: unknown }) {
  const message = error instanceof ApiError ? error.message : 'The information could not be loaded. Try again.'
  return <div className="alert alert-error" role="alert"><AlertCircle size={18} /><span>{message}</span></div>
}

export function EmptyState({ title, children, action }: { title: string; children: ReactNode; action?: ReactNode }) {
  return <div className="empty-state"><h3>{title}</h3><p>{children}</p>{action}</div>
}

export function SuccessMessage({ children }: { children: ReactNode }) {
  return <div className="alert alert-success" role="status"><CheckCircle2 size={18} /><span>{children}</span></div>
}

export function Status({ value }: { value: string }) {
  const status = value.toLowerCase().replaceAll('_', ' ')
  const tone = value === 'APPROVED' || value === 'COMPLETED' || value === 'ON_TARGET' ? 'success'
    : value === 'REJECTED' || value === 'OUTSIDE_GEOFENCE' ? 'danger'
      : value === 'UNDER_REVIEW' || value === 'SUBMITTED' || value === 'ACTIVE' ? 'info' : 'neutral'
  return <span className={`status status-${tone}`}>{status}</span>
}

