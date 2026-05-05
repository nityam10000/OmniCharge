import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAppSelector } from '../shared/hooks/redux';
import { FullPageLoader } from '../shared/components';

// FIX: Guard all routes against race condition where tokens exist in localStorage
// but the user profile hasn't loaded yet. Without this check, user?.role is null
// and AdminRoute immediately redirects admins to /dashboard on first load.

export const ProtectedRoute: React.FC = () => {
  const { isAuthenticated, loading } = useAppSelector((s) => s.auth);
  if (loading) return <FullPageLoader />;
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};

export const AdminRoute: React.FC = () => {
  const { isAuthenticated, user, loading } = useAppSelector((s) => s.auth);
  // FIX: Wait for profile to load before checking role
  if (loading) return <FullPageLoader />;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  // FIX: If authenticated but user not yet loaded, wait (prevents flash redirect)
  if (!user) return <FullPageLoader />;
  if (user.role !== 'ADMIN') return <Navigate to="/dashboard" replace />;
  return <Outlet />;
};

export const PublicRoute: React.FC = () => {
  const { isAuthenticated, user, loading } = useAppSelector((s) => s.auth);
  // FIX: Wait for auth state to resolve before deciding to redirect
  if (loading) return <FullPageLoader />;
  if (!isAuthenticated) return <Outlet />;
  // FIX: Wait for profile before redirecting to role-specific page
  if (!user) return <FullPageLoader />;
  return <Navigate to={user.role === 'ADMIN' ? '/admin' : '/dashboard'} replace />;
};
