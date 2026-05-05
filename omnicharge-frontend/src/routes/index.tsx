import React, { lazy, Suspense } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ProtectedRoute, AdminRoute, PublicRoute } from './guards';
import { FullPageLoader } from '../shared/components';

// Static imports for frequently accessed pages
import LoginPage from '../features/auth/pages/LoginPage';
import AdminLoginPage from '../features/auth/pages/AdminLoginPage';
import OtpLoginPage from '../features/auth/pages/OtpLoginPage';
import DashboardLayout from '../layouts/DashboardLayout';
import UserDashboard from '../features/dashboard/pages/UserDashboard';
import RechargePage from '../features/recharge/pages/RechargePage';
import MyRechargesPage from '../features/recharge/pages/MyRechargesPage';
import MyTransactionsPage from '../features/recharge/pages/MyTransactionsPage';
import AdminLayout from '../layouts/AdminLayout';
import AdminDashboard from '../features/admin/pages/AdminDashboard';
import AdminUsersPage from '../features/admin/pages/AdminUsersPage';
import AdminOperatorsPage from '../features/admin/pages/AdminOperatorsPage';
import AdminPlansPage from '../features/admin/pages/AdminPlansPage';
import AdminRechargesPage from '../features/admin/pages/AdminRechargesPage';

// Lazy loaded pages
const SignupPage = lazy(() => import('../features/auth/pages/SignupPage'));
const ForgotPasswordPage = lazy(() => import('../features/auth/pages/ForgotPasswordPage'));
const ResetPasswordPage = lazy(() => import('../features/auth/pages/ResetPasswordPage'));
const ProfilePage = lazy(() => import('../features/profile/pages/ProfilePage'));

export const AppRouter: React.FC = () => (
  <BrowserRouter>
    <Suspense fallback={<FullPageLoader />}>
      <Routes>
        {/* Public routes */}
        <Route element={<PublicRoute />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/admin/login" element={<AdminLoginPage />} />
          <Route path="/login/otp" element={<OtpLoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
        </Route>

        {/* User routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<DashboardLayout />}>
            <Route path="/dashboard" element={<UserDashboard />} />
            <Route path="/recharge" element={<RechargePage />} />
            <Route path="/my-recharges" element={<MyRechargesPage />} />
            <Route path="/my-transactions" element={<MyTransactionsPage />} />
            <Route path="/profile" element={<ProfilePage />} />
          </Route>
        </Route>

        {/* Admin routes */}
        <Route element={<AdminRoute />}>
          <Route element={<AdminLayout />}>
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<AdminUsersPage />} />
            <Route path="/admin/operators" element={<AdminOperatorsPage />} />
            <Route path="/admin/plans" element={<AdminPlansPage />} />
            <Route path="/admin/recharges" element={<AdminRechargesPage />} />
          </Route>
        </Route>

        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </Suspense>
  </BrowserRouter>
);
