import { createBrowserRouter, Navigate, Outlet, useLocation } from 'react-router-dom'
import type { ReactNode } from 'react'
import { AuthProvider, useAuth } from './auth'
import { AppShell } from '../components/layout/AppShell'
import { lazy, Suspense } from 'react'

const LoginPage = lazy(() => import('../features/auth/LoginPage').then(module => ({ default: module.LoginPage })))
const DashboardPage = lazy(() => import('../features/dashboard/DashboardPage').then(module => ({ default: module.DashboardPage })))
const AttendancePage = lazy(() => import('../features/attendance/AttendancePage').then(module => ({ default: module.AttendancePage })))
const WorkPage = lazy(() => import('../features/work/WorkPage').then(module => ({ default: module.WorkPage })))
const NotificationsPage = lazy(() => import('../features/notifications/NotificationsPage').then(module => ({ default: module.NotificationsPage })))
const ReviewPage = lazy(() => import('../features/review/ReviewPage').then(module => ({ default: module.ReviewPage })))
const EscalationsPage = lazy(() => import('../features/escalations/EscalationsPage').then(module => ({ default: module.EscalationsPage })))
const AdministrationPage = lazy(() => import('../features/administration/AdministrationPage').then(module => ({ default: module.AdministrationPage })))
const ReportsPage = lazy(() => import('../features/reports/ReportsPage').then(module => ({ default: module.ReportsPage })))

function LazyPage({ children }: { children: ReactNode }) {
  return <Suspense fallback={<div className="boot-state" role="status">Loading workspace…</div>}>{children}</Suspense>
}

function RequireAuth() {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <div className="boot-state" role="status">Restoring your secure session…</div>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return <Outlet />
}

function RequireAccess({ roles = [], permissions = [], children }: { roles?: string[]; permissions?: string[]; children: ReactNode }) {
  const { has, hasRole } = useAuth()
  return has(...permissions) || hasRole(...roles) ? children : <Navigate to="/app" replace />
}

function RequireGuest() {
  const { user, loading } = useAuth()
  if (loading) return <div className="boot-state" role="status">Restoring your secure session…</div>
  return user ? <Navigate to="/app" replace /> : <LazyPage><LoginPage /></LazyPage>
}

export const router = createBrowserRouter([
  {
    element: <AuthProvider><Outlet /></AuthProvider>,
    children: [
      { path: '/login', element: <RequireGuest /> },
      {
        path: '/app',
        element: <RequireAuth />,
        children: [{ element: <AppShell />, children: [
          { index: true, element: <LazyPage><DashboardPage /></LazyPage> },
          { path: 'attendance', element: <RequireAccess roles={['STUDENT']}><LazyPage><AttendancePage /></LazyPage></RequireAccess> },
          { path: 'work', element: <LazyPage><WorkPage /></LazyPage> },
          { path: 'notifications', element: <LazyPage><NotificationsPage /></LazyPage> },
          { path: 'review', element: <RequireAccess roles={['SUPERVISOR', 'MENTOR', 'ADMIN', 'SUPER_ADMIN']} permissions={['SUBMISSION_REVIEW', 'VERIFICATION_REVIEW']}><LazyPage><ReviewPage /></LazyPage></RequireAccess> },
          { path: 'escalations', element: <RequireAccess roles={['SUPERVISOR', 'MENTOR', 'ADMIN', 'SUPER_ADMIN']} permissions={['REPORT_READ']}><LazyPage><EscalationsPage /></LazyPage></RequireAccess> },
          { path: 'administration', element: <RequireAccess roles={['ADMIN', 'SUPER_ADMIN']} permissions={['USER_MANAGE', 'ORGANIZATION_READ', 'ORGANIZATION_MANAGE']}><LazyPage><AdministrationPage /></LazyPage></RequireAccess> },
          { path: 'reports', element: <RequireAccess roles={['ADMIN', 'SUPER_ADMIN']} permissions={['REPORT_READ']}><LazyPage><ReportsPage /></LazyPage></RequireAccess> },
        ] }],
      },
      { path: '*', element: <Navigate to="/app" replace /> },
    ],
  },
])
