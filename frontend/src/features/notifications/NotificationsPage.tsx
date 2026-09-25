import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bell, Check } from 'lucide-react'
import { useAuth } from '../../app/auth'
import { notificationApi } from '../../api/resources'
import { EmptyState, PageError, PageLoading } from '../../components/ui/Feedback'
import { formatTimestamp } from '../../utils/format'

export function NotificationsPage() {
  const { user } = useAuth()
  const cache = useQueryClient()
  const list = useQuery({ queryKey: ['notifications', user?.id], queryFn: () => notificationApi.list(user!.id), enabled: !!user })
  const markRead = useMutation({ mutationFn: (id: string) => notificationApi.markRead(user!.id, id), onSuccess: () => cache.invalidateQueries({ queryKey: ['notifications', user?.id] }) })
  if (list.isLoading) return <PageLoading label="Loading notifications" />
  if (list.isError) return <PageError error={list.error} />
  return <div className="page-stack"><div className="page-heading"><div><span className="eyebrow">INBOX</span><h1>Notifications</h1><p>Updates related to your work, attendance and review activity.</p></div></div>
    <section className="panel"><div className="section-heading"><div><span className="eyebrow">RECENT UPDATES</span><h2>All notifications</h2></div><span className="muted">{list.data?.filter(item => !item.readAt).length ?? 0} unread</span></div>
      {!list.data?.length ? <EmptyState title="No notifications yet">Workhub updates will appear here when there is something to follow up.</EmptyState> : <ul className="notification-list notification-page-list">{list.data.map(item => <li key={item.id} className={!item.readAt ? 'unread' : ''}><div className="notification-symbol"><Bell size={16} /></div><div className="notification-body"><div className="notification-title"><strong>{item.title}</strong><span className="status status-neutral">{item.type.toLowerCase().replaceAll('_', ' ')}</span></div><p>{item.message}</p><time>{formatTimestamp(item.createdAt)}</time></div>{!item.readAt && <button className="button button-small button-quiet" disabled={markRead.isPending} onClick={() => markRead.mutate(item.id)}><Check size={14} /> Mark read</button>}</li>)}</ul>}
      {markRead.isError && <PageError error={markRead.error} />}
    </section>
  </div>
}
