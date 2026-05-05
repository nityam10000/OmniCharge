import { useAppDispatch } from './redux';
import { useNavigate } from 'react-router-dom';
import { logout } from '../../store/slices/authSlice';
import { useAppContext } from '../../core/context/AppContext';
import toast from 'react-hot-toast';

/**
 * Custom logout hook that ensures caches are cleared and user is redirected
 */
export const useLogout = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { invalidateMyRecharges, invalidateMyTransactions, invalidateOperators } = useAppContext();

  const handleLogout = () => {
    // Clear caches immediately
    invalidateMyRecharges();
    invalidateMyTransactions();
    invalidateOperators();

    // Dispatch logout action
    dispatch(logout());

    // Show success message and redirect
    toast.success('Logged out successfully');
    navigate('/login', { replace: true });
  };

  return { handleLogout };
};
