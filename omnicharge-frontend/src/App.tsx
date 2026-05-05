import { useEffect } from 'react';
import { Provider } from 'react-redux';
import { Toaster } from 'react-hot-toast';
import { store } from './store';
import { AppRouter } from './routes';
import { AppContextProvider } from './core/context/AppContext';
import { fetchProfileThunk } from './store/slices/authSlice';
import { useCacheCleanup } from './shared/hooks/useCacheCleanup';

// FIX: Bootstrap profile on app start if tokens exist in localStorage.
// Without this, page refreshes leave user as null (isAuthenticated=true, user=null)
// causing AdminRoute to flash-redirect authenticated admins to /dashboard.
function AppBootstrap() {
  // Clear caches when user logs out
  useCacheCleanup();

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      store.dispatch(fetchProfileThunk());
    }
  }, []);

  return null;
}

function App() {
  return (
    <Provider store={store}>
      <AppContextProvider>
        <AppBootstrap />
        <AppRouter />
        <Toaster
          position="top-right"
          toastOptions={{
            style: {
              background: '#1E293B',
              color: '#F1F5F9',
              border: '1px solid #334155',
              borderRadius: '12px',
              fontSize: '14px',
            },
            success: { iconTheme: { primary: '#10B981', secondary: '#1E293B' } },
            error: { iconTheme: { primary: '#EF4444', secondary: '#1E293B' } },
          }}
        />
      </AppContextProvider>
    </Provider>
  );
}

export default App;
