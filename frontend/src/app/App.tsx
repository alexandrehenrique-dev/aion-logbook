import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import { AuthProvider } from '../features/auth/AuthContext';
import { ThemeProvider } from './providers/ThemeProvider';
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
import { ComponentShowcase } from './pages/ComponentShowcase';

export default function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/onboarding" element={<Onboarding />} />

            <Route path="/dashboard" element={<MainLayout><Dashboard /></MainLayout>} />
            <Route path="/plans" element={<MainLayout><Plans /></MainLayout>} />
            <Route path="/plans/:id" element={<MainLayout><PlanDetail /></MainLayout>} />
            <Route path="/directions" element={<MainLayout><Directions /></MainLayout>} />
            <Route path="/directions/:id" element={<MainLayout><DirectionDetail /></MainLayout>} />
            <Route path="/analytics" element={<MainLayout><Analytics /></MainLayout>} />
            <Route path="/sessions" element={<MainLayout><Sessions /></MainLayout>} />
            <Route path="/calendar" element={<MainLayout><Calendar /></MainLayout>} />
            <Route path="/settings" element={<MainLayout><Settings /></MainLayout>} />

            <Route path="/showcase" element={<ComponentShowcase />} />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </ThemeProvider>
  );
}
