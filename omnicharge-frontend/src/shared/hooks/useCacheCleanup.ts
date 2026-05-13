import { useEffect, useRef } from 'react';
import { useAppSelector } from './redux';
import { useAppContext } from '../../core/context/AppContext';


export const useCacheCleanup = () => {
  const { isAuthenticated, user } = useAppSelector((s) => s.auth);
  const { invalidateMyRecharges, invalidateMyTransactions, invalidateOperators } = useAppContext();
  const prevAuthStateRef = useRef<boolean>(isAuthenticated);
  const prevUserIdRef = useRef<number | undefined>(user?.userId);

  useEffect(() => {
    // If auth state changed from true to false (logout happened)
    if (prevAuthStateRef.current === true && isAuthenticated === false) {
      invalidateMyRecharges();
      invalidateMyTransactions();
      invalidateOperators();
      prevUserIdRef.current = undefined;
    }
    // If user ID changed (different user logged in)
    else if (prevUserIdRef.current !== undefined && prevUserIdRef.current !== user?.userId && isAuthenticated) {
      invalidateMyRecharges();
      invalidateMyTransactions();
      // Don't invalidate operators as they're shared across users
    }

    prevAuthStateRef.current = isAuthenticated;
    prevUserIdRef.current = user?.userId;
  }, [isAuthenticated, user?.userId, invalidateMyRecharges, invalidateMyTransactions, invalidateOperators]);
};

