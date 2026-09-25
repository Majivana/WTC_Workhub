import { createBrowserRouter, Navigate, Outlet, useLocation } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth'
import { AppShell } from '../components/layout/AppShell'
import { LoginPage } from '../features/auth/LoginPage'
import { DashboardPage } from '../features/dashboard/DashboardPage'
import { AttendancePage } from '../features/attendance/AttendancePage'
import { WorkPage } from '../features/work/WorkPage'
import { NotificationsPage } from '../features/notifications/NotificationsPage'
import { ReviewPage } from '../features/review/ReviewPage'
import { EscalationsPage } from '../features/escalations/EscalationsPage'
import { AdministrationPage } from '../features/administration/AdministrationPage'
import { ReportsPage } from '../features/reports/ReportsPage'

function RequireAuth() {
  const { user, loading } = useAuth()
  const location = useLocation()
  if (loading) return <div className="boot-state" role="status">Restoring your secure session…</div>
  if (!user) return <Navigate to="/login" replace state={{ from: location.pathname }} />
  return <Outlet />
}

export const router = createBrowserRouter([
  {
    element: <AuthProvider><Outlet /></AuthProvider>,
    children: [
      { path: '/login', element: <LoginPage /> },
      {
        path: '/app',
        element: <RequireAuth />,
        children: [{ element: <AppShell />, children: [
          { index: true, element: <DashboardPage /> },
          { path: 'attendance', element: <AttendancePage /> },
          { path: 'work', element: <WorkPage /> },
          { path: 'notifications', element: <NotificationsPage /> },
          { path: 'review', element: <ReviewPage /> },
          { path: 'escalations', element: <EscalationsPage /> },
          { path: 'administration', element: <AdministrationPage /> },
          { path: 'reports', element: <ReportsPage /> },
        ] }],
      },
      { path: '*', element: <Navigate to="/app" replace /> },
    ],
  },
])
