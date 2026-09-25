import { useEffect, useMemo, useRef, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Camera, CameraOff, CircleCheck, MapPin, RotateCcw } from 'lucide-react'
import { useAuth } from '../../app/auth'
import { workApi } from '../../api/resources'
import { PageError, PageLoading, Status, SuccessMessage } from '../../components/ui/Feedback'
import type { WorkPeriod } from '../../types/domain'
import { formatDate, formatHours, formatTimestamp } from '../../utils/format'

export function AttendancePage() {
  const { user } = useAuth()
  const cache = useQueryClient()
  const video = useRef<HTMLVideoElement>(null)
  const stream = useRef<MediaStream | null>(null)
  const periods = useQuery({ queryKey: ['work-periods'], queryFn: workApi.periods })
  const [periodId, setPeriodId] = useState('')
  const period = useMemo(() => periods.data?.find(item => item.id === periodId) ?? periods.data?.[0], [periods.data, periodId])
  const dashboard = useQuery({ queryKey: ['dashboard', user?.id, period?.id], queryFn: () => workApi.dashboard(user!.id, period!.id), enabled: !!user && !!period })
  const [photo, setPhoto] = useState<Blob | null>(null)
  const [photoUrl, setPhotoUrl] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [cameraActive, setCameraActive] = useState(false)
  const active = dashboard.data?.attendance.find(session => session.status === 'ACTIVE')

  useEffect(() => () => stream.current?.getTracks().forEach(track => track.stop()), [])
  useEffect(() => {
    if (!photo) { setPhotoUrl(null); return }
    const url = URL.createObjectURL(photo)
    setPhotoUrl(url)
    return () => URL.revokeObjectURL(url)
  }, [photo])
  useEffect(() => { if (video.current && stream.current) video.current.srcObject = stream.current }, [cameraActive])

  async function startCamera() {
    setError('')
    if (!navigator.mediaDevices?.getUserMedia) { setError('This browser does not support camera access. Use a recent browser on a secure connection.'); return }
    try {
      stream.current?.getTracks().forEach(track => track.stop())
      stream.current = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user', width: { ideal: 720 }, height: { ideal: 540 } }, audio: false })
      setCameraActive(true)
      if (video.current) video.current.srcObject = stream.current
    } catch (cause) {
      const name = cause instanceof DOMException ? cause.name : ''
      setError(name === 'NotAllowedError' ? 'Camera access was blocked. Allow camera access in your browser settings and try again.' : name === 'NotFoundError' ? 'No camera was found on this device.' : 'The camera could not be started. Check that another application is not using it.')
    }
  }

  async function capture() {
    const node = video.current
    if (!node || node.videoWidth === 0) { setError('The camera is still starting. Wait a moment and try again.'); return }
    const canvas = document.createElement('canvas')
    const scale = Math.min(1, 640 / node.videoWidth)
    canvas.width = Math.round(node.videoWidth * scale)
    canvas.height = Math.round(node.videoHeight * scale)
    canvas.getContext('2d')?.drawImage(node, 0, 0, canvas.width, canvas.height)
    let blob = await canvasBlob(canvas, 0.76)
    if (blob.size > 2 * 1024 * 1024) blob = await canvasBlob(canvas, 0.55)
    if (blob.size > 2 * 1024 * 1024) { setError('This image is larger than the 2 MB attendance limit. Move closer to the camera and retake it.'); return }
    setPhoto(blob)
    setError('')
    stream.current?.getTracks().forEach(track => track.stop())
    stream.current = null
    setCameraActive(false)
  }

  const captureRequest = useMutation({
    mutationFn: async () => {
      if (!photo) throw new Error('Capture a selfie before continuing.')
      if (!user?.campusId) throw new Error('Your account has no assigned campus. Contact your administrator.')
      if (!period) throw new Error('Choose a work period before continuing.')
      const coords = await locate()
      const checksum = await sha256(photo)
      const imageBase64 = await blobBase64(photo)
      const input = { userId: user.id, campusId: user.campusId, workPeriodId: period.id,
        mediaType: 'image/jpeg', sizeBytes: photo.size, checksum,
        latitude: coords.latitude, longitude: coords.longitude, imageBase64 }
      return active ? workApi.attendanceOut(active.id, input) : workApi.attendanceIn(input)
    },
    onSuccess: async result => {
      setSuccess(active ? `Clock-out recorded at ${formatTimestamp(result.clockOutAt ?? new Date().toISOString())}.` : `Clock-in recorded at ${formatTimestamp(result.clockInAt)}. Workhub confirmed the campus location and private selfie storage.`)
      setPhoto(null)
      await cache.invalidateQueries({ queryKey: ['dashboard', user?.id, period?.id] })
    },
    onError: cause => setError(attendanceError(cause)),
  })

  if (periods.isLoading) return <PageLoading label="Loading attendance" />
  if (periods.isError) return <PageError error={periods.error} />

  return <div className="page-stack attendance-page">
    <div className="page-heading"><div><span className="eyebrow">FIRST-PARTY ATTENDANCE</span><h1>Attendance</h1><p>Clock in and out from your assigned campus. Workhub checks location and records the session time.</p></div></div>
    {!user?.campusId && <div className="alert alert-error" role="alert">Your account does not have a campus assignment. Contact an administrator before attempting attendance.</div>}
    <section className="panel attendance-panel">
      <div className="section-heading"><div><span className="eyebrow">TODAY'S SESSION</span><h2>{active ? 'Session in progress' : 'Ready to record attendance'}</h2></div>{active && <Status value={active.status} />}</div>
      {periods.data?.length ? <label className="field attendance-period"><span>Work period</span><select value={period?.id ?? ''} onChange={event => { setPeriodId(event.target.value); setPhoto(null); setError('') }}>{periods.data.map((item: WorkPeriod) => <option key={item.id} value={item.id}>{item.name} · {formatDate(item.startDate)} to {formatDate(item.endDate)}</option>)}</select></label> : <div className="alert alert-error">No work periods have been configured.</div>}
      {dashboard.isLoading && <PageLoading label="Checking your current session" />}{dashboard.isError && <PageError error={dashboard.error} />}
      {active && <div className="session-callout"><div><span>Clocked in</span><strong>{formatTimestamp(active.clockInAt)}</strong></div><div><span>Session status</span><strong>In progress</strong></div></div>}
      {success && <SuccessMessage>{success}</SuccessMessage>}
      {error && <div className="alert alert-error" role="alert">{error}</div>}
      <div className="capture-grid">
        <div className="camera-column">
          <div className={`camera-frame ${photo ? 'camera-preview' : ''}`}>
            {cameraActive ? <video ref={video} autoPlay playsInline muted aria-label="Live camera preview" /> : photoUrl ? <img src={photoUrl} alt="Selfie preview before attendance submission" /> : <div className="camera-placeholder"><Camera size={24} /><span>Your camera preview will appear here</span></div>}
          </div>
          <div className="capture-actions">{cameraActive ? <button className="button button-secondary" onClick={() => { stream.current?.getTracks().forEach(track => track.stop()); stream.current = null; setCameraActive(false) }}><CameraOff size={16} /> Stop camera</button> : <button className="button button-secondary" onClick={startCamera}><Camera size={16} /> {photo ? 'Retake selfie' : 'Open camera'}</button>}{cameraActive && <button className="button button-primary" onClick={capture}><CircleCheck size={16} /> Capture selfie</button>}{photo && !cameraActive && <button className="button button-quiet" onClick={() => setPhoto(null)}><RotateCcw size={15} /> Clear</button>}</div>
          {photo && <p className="field-hint">Selfie ready · {(photo.size / 1024).toFixed(0)} KB · JPEG</p>}
        </div>
        <div className="capture-guidance"><h3>Before you continue</h3><ul><li><Camera size={16} /><span>Camera access is used to capture a still selfie for this clock action.</span></li><li><MapPin size={16} /><span>Location is checked against the campus assigned to your account.</span></li><li><CircleCheck size={16} /><span>The server confirms the geofence and stores the private image before reporting success.</span></li></ul><p>Precise coordinates and image data are sent securely to the Workhub API and are not shown in routine logs. A camera or location denial prevents this capture.</p></div>
      </div>
      <div className="attendance-submit"><button className="button button-primary" disabled={!photo || !period || captureRequest.isPending || dashboard.isLoading || !user?.campusId} onClick={() => { setError(''); setSuccess(''); captureRequest.mutate() }}>{captureRequest.isPending ? 'Verifying and recording…' : active ? 'Capture clock-out' : 'Capture clock-in'}</button><span>Workhub uses server time. The location check is performed by the backend.</span></div>
    </section>
    {period && dashboard.data && <section className="panel"><div className="section-heading"><div><span className="eyebrow">SESSION HISTORY</span><h2>Attendance record</h2></div></div>{dashboard.data.attendance.length === 0 ? <p className="muted">No sessions recorded for this period.</p> : <div className="table-wrap"><table><thead><tr><th>Clock in</th><th>Duration</th><th>Session</th><th>Reconciliation</th></tr></thead><tbody>{dashboard.data.attendance.map((session, index) => <tr key={`${session.id}-${index}`}><td>{formatTimestamp(session.clockInAt)}</td><td>{session.durationMinutes ? formatHours(session.durationMinutes / 60) : '—'}</td><td><Status value={session.status} /></td><td className="mono">{session.reconciliationReference ?? 'In progress'}</td></tr>)}</tbody></table></div>}</section>}
  </div>
}

function canvasBlob(canvas: HTMLCanvasElement, quality: number) {
  return new Promise<Blob>((resolve, reject) => canvas.toBlob(blob => blob ? resolve(blob) : reject(new Error('The selfie image could not be created.')), 'image/jpeg', quality))
}

async function sha256(blob: Blob) {
  const hash = await crypto.subtle.digest('SHA-256', await blob.arrayBuffer())
  return Array.from(new Uint8Array(hash), byte => byte.toString(16).padStart(2, '0')).join('')
}

function blobBase64(blob: Blob) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result).split(',')[1] ?? '')
    reader.onerror = () => reject(new Error('The selfie image could not be prepared.'))
    reader.readAsDataURL(blob)
  })
}

function locate() {
  return new Promise<{ latitude: number; longitude: number }>((resolve, reject) => {
    if (!navigator.geolocation) { reject(new Error('This browser does not support location access.')); return }
    navigator.geolocation.getCurrentPosition(
      position => resolve({ latitude: position.coords.latitude, longitude: position.coords.longitude }),
      cause => reject(new Error(cause.code === cause.PERMISSION_DENIED ? 'Location access was denied. Allow location access in browser settings and try again.' : cause.code === cause.TIMEOUT ? 'Location could not be determined before the request timed out. Try again in a place with a clearer GPS signal.' : 'The device location is unavailable. Check device location settings and try again.')),
      { enableHighAccuracy: true, timeout: 15_000, maximumAge: 0 },
    )
  })
}

function attendanceError(cause: unknown) {
  if (!(cause instanceof Error)) return 'Attendance could not be recorded. Check your network and try again.'
  const text = cause.message.toLowerCase()
  if (text.includes('outside') || text.includes('geofence')) return 'Workhub could not verify that you are within the approved campus area. Check your location setting and try again at your assigned campus.'
  if (text.includes('already has an active')) return 'There is already an open attendance session. Refresh the page to continue with clock-out.'
  if (text.includes('campus')) return 'The selected campus does not match your account assignment. Contact your administrator.'
  return cause.message
}
