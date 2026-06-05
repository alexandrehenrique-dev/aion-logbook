import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import { Toaster } from 'sonner';
import { AuthProvider } from '../features/auth/AuthContext';
import { ThemeProvider } from './providers/ThemeProvider';
import { ProtectedRoute } from './components/ProtectedRoute';
import { MainLayout } from './layouts/MainLayout';
import { Login } from './pages/Login';
import { Onboarding } from './pages/Onboarding';
import { Dashboard } from './pages/Dashboard';
import { Plans } from './pages/Plans';
import { PlanDetail } from './pages/PlanDetail';
import { Directions } from './pages/Directions';
import { DirectionDetail } from './pages/DirectionDetail';
import { Analytics } from './pages/Analytics';
import { Sessions } from './pages/Sessions';
import { Calendar } from './pages/Calendar';
import { Settings } from './pages/Settings';
import { Logbook } from './pages/Logbook';
import { ComponentShowcase } from './pages/ComponentShowcase';

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Toaster richColors position="top-right" />
        <BrowserRouter basename={import.meta.env.BASE_URL.replace(/\/$/, '')}>
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<Login />} />

            <Route
              path="/onboarding"
              element={
                <ProtectedRoute>
                  <Onboarding />
                </ProtectedRoute>
              }
            />

            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <MainLayout><Dashboard /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/plans"
              element={
                <ProtectedRoute>
                  <MainLayout><Plans /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/plans/:id"
              element={
                <ProtectedRoute>
                  <MainLayout><PlanDetail /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/directions"
              element={
                <ProtectedRoute>
                  <MainLayout><Directions /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/directions/:id"
              element={
                <ProtectedRoute>
                  <MainLayout><DirectionDetail /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/analytics"
              element={
                <ProtectedRoute>
                  <MainLayout><Analytics /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/sessions"
              element={
                <ProtectedRoute>
                  <MainLayout><Sessions /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/calendar"
              element={
                <ProtectedRoute>
                  <MainLayout><Calendar /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/journal"
              element={
                <ProtectedRoute>
                  <MainLayout><Logbook /></MainLayout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/settings"
              element={
                <ProtectedRoute>
                  <MainLayout><Settings /></MainLayout>
                </ProtectedRoute>
              }
            />

            <Route path="/showcase" element={<ComponentShowcase />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}
