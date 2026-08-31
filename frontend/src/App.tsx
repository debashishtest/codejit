import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { AppShell } from './components/AppShell'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AuthPage } from './pages/AuthPage'
import { AssessmentDetailPage } from './pages/AssessmentDetailPage'
import { CreateAssessmentPage } from './pages/CreateAssessmentPage'
import { DashboardPage } from './pages/DashboardPage'
import { JoinPage } from './pages/JoinPage'
import { SolvePage } from './pages/SolvePage'
import { LandingPage } from './pages/LandingPage'
import { NotificationProvider } from './notifications/NotificationProvider'
import { InterviewSchedulePage } from './pages/InterviewSchedulePage'
import { InterviewJoinPage } from './pages/InterviewJoinPage'
import { LiveInterviewPage } from './pages/LiveInterviewPage'

function App() {
  return <BrowserRouter><AuthProvider><NotificationProvider><Routes>
    <Route path="/" element={<LandingPage />} />
    <Route path="/login" element={<AuthPage mode="login" />} />
    <Route path="/register" element={<AuthPage mode="register" />} />
    <Route element={<ProtectedRoute />}><Route element={<AppShell />}>
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/interviews" element={<InterviewSchedulePage />} />
      <Route path="/interviews/join" element={<InterviewJoinPage />} />
      <Route path="/interviews/:id/live" element={<LiveInterviewPage />} />
      <Route path="/join" element={<JoinPage />} />
      <Route path="/assessments/new" element={<CreateAssessmentPage />} />
      <Route path="/assessments/:id" element={<AssessmentDetailPage />} />
      <Route path="/assessments/:id/solve" element={<SolvePage />} />
      <Route path="/candidates" element={<DashboardPage />} />
      <Route path="/activity" element={<DashboardPage />} />
      <Route path="/settings" element={<DashboardPage />} />
    </Route></Route>
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes></NotificationProvider></AuthProvider></BrowserRouter>
}
export default App
