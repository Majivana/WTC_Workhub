import { useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import { Activity, Bell, BriefcaseBusiness, ClipboardCheck, FileBarChart, LogOut, Menu, Shield, UserRound, X } from 'lucide-react'
import { useAuth } from '../../app/auth'
import { notificationApi } from '../../api/resources'
import { useQuery } from '@tanstack/react-query'

const labels: Record<string, string> = {
  '/app': 'Workspace', '/app/attendance': 'Attendance', '/app/work': 'Work entries',
  '/app/notifications': 'Notifications', '/app/review': 'Review queue',
  '/app/escalations': 'Escalations', '/app/administration': 'Administration', '/app/reports': 'Reports',
}

export function AppShell() {
  const { user, signOut, has, hasRole } = useAuth()
  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()
  const notifications = useQuery({
    queryKey: ['notifications', user?.id],
    queryFn: () => notificationApi.list(user!.id),
    enabled: !!user,
    refetchInterval: 60_000,
  })
  const unread = notifications.data?.filter(item => !item.readAt).length ?? 0
  const reviewAccess = has('SUBMISSION_REVIEW', 'VERIFICATION_REVIEW') || hasRole('SUPERVISOR', 'MENTOR', 'ADMIN', 'SUPER_ADMIN')
  const adminAccess = has('USER_MANAGE') || hasRole('ADMIN', 'SUPER_ADMIN')
  const nav = [
    { to: '/app', label: 'Overview', icon: Activity, show: true, end: true },
    { to: '/app/attendance', label: 'Attendance', icon: UserRound, show: hasRole('STUDENT') },
    { to: '/app/work', label: 'Work entries', icon: BriefcaseBusiness, show: true },
    { to: '/app/review', label: 'Reviews', icon: ClipboardCheck, show: reviewAccess },
    { to: '/app/notifications', label: 'Notifications', icon: Bell, show: true, count: unread },
    { to: '/app/escalations', label: 'Escalations', icon: Shield, show: hasRole('SUPERVISOR', 'MENTOR', 'ADMIN', 'SUPER_ADMIN') },
    { to: '/app/administration', label: 'Administration', icon: UserRound, show: adminAccess },
    { to: '/app/reports', label: 'Reports', icon: FileBarChart, show: has('REPORT_READ') || hasRole('ADMIN', 'SUPER_ADMIN') },
  ]
  const title = labels[location.pathname] ?? 'Workspace'

  return <div className="app-shell">
    {mobileOpen && <button className="scrim" aria-label="Close navigation" onClick={() => setMobileOpen(false)} />}
    <aside className={`sidebar ${mobileOpen ? 'sidebar-open' : ''}`} aria-label="Main navigation">
      <div className="brand"><div className="brand-mark">W</div><div><strong>Workhub</strong><span>WeThinkCode_</span></div><button className="icon-button mobile-close" aria-label="Close menu" onClick={() => setMobileOpen(false)}><X size={20} /></button></div>
      <div className="nav-caption">WORKSPACE</div>
      <nav>{nav.filter(item => item.show).map(item => <NavLink key={item.to} to={item.to} end={item.end} onClick={() => setMobileOpen(false)} className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}><item.icon size={18} strokeWidth={1.8} /><span>{item.label}</span>{item.count ? <span className="nav-count">{item.count}</span> : null}</NavLink>)}</nav>
      <div className="sidebar-footer"><div className="profile"><div className="avatar">{user?.displayName?.slice(0, 1).toUpperCase()}</div><div className="profile-copy"><strong>{user?.displayName}</strong><span>{roleLabel(user?.systemRole)}</span></div></div><button className="signout" onClick={signOut}><LogOut size={16} /> Sign out</button></div>
    </aside>
    <div className="app-main">
      <header className="topbar"><button className="icon-button mobile-menu" aria-label="Open navigation" onClick={() => setMobileOpen(true)}><Menu size={21} /></button><div className="breadcrumbs"><span>Workhub</span><span className="crumb-separator">/</span><strong>{title}</strong></div><div className="topbar-actions"><Link to="/app/notifications" className="icon-button notification-link" aria-label={`Notifications${unread ? `, ${unread} unread` : ''}`}><Bell size={19} />{unread > 0 && <i />}</Link><span className="topbar-role">{roleLabel(user?.systemRole)}</span></div></header>
      <main className="page-content"><Outlet /></main>
    </div>
  </div>
}

function roleLabel(role?: string) { return role?.toLowerCase().replaceAll('_', ' ') ?? '' }
