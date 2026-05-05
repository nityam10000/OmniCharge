import React, { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Mail, Lock } from 'lucide-react';
import { useAppDispatch, useAppSelector } from '../../../shared/hooks/redux';
import { loginThunk, clearError } from '../../../store/slices/authSlice';
import { Spinner, FormField } from '../../../shared/components';
import AuthLayout from './AuthLayout';
import toast from 'react-hot-toast';

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(1, 'Password is required'),
});
type FormData = z.infer<typeof schema>;

const LoginPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { loading, error } = useAppSelector((s) => s.auth);
  const [showPw, setShowPw] = React.useState(false);

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  useEffect(() => {
    dispatch(clearError());
  }, [dispatch]);

  const onSubmit = async (data: FormData) => {
    console.log('Login attempt:', { email: data.email });
    const result = await dispatch(loginThunk(data));
    if (loginThunk.fulfilled.match(result)) {
      console.log('Login successful');
      toast.success('Welcome back!');
      const role = result.payload.user?.role;
      navigate(role === 'ADMIN' ? '/admin' : '/dashboard');
    } else if (loginThunk.rejected.match(result)) {
      console.error('Login rejected:', result.payload);
      toast.error(`Login failed: ${result.payload || 'Invalid email or password'}`);
    }
  };

  return (
    <AuthLayout title="Sign In" subtitle="Enter your email and password">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <FormField label="Email" error={errors.email?.message}>
          <div className="relative">
            <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              {...register('email')}
              type="email"
              placeholder="you@example.com"
              className="input-field pl-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20 transition-all"
            />
          </div>
        </FormField>

        <FormField label="Password" error={errors.password?.message}>
          <div className="relative">
            <Lock size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              {...register('password')}
              type={showPw ? 'text' : 'password'}
              placeholder="Enter your password"
              className="input-field pl-11 pr-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20 transition-all"
            />
            <button
              type="button"
              onClick={() => setShowPw((v) => !v)}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-300 transition-colors"
            >
              {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          </div>
        </FormField>

        {error && (
          <div className="bg-accent-red/10 border border-accent-red/30 rounded-lg px-3 py-2 text-xs text-accent-red text-center">
            {error}
          </div>
        )}

        <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-2.5 text-base font-semibold rounded-lg mt-6">
          {loading ? <Spinner size={18} /> : null}
          {loading ? 'Signing in...' : 'Sign In'}
        </button>

        <div className="flex items-center justify-between text-xs mt-4">
          <Link to="/forgot-password" className="text-brand hover:text-brand-light transition-colors">
            Forgot password?
          </Link>
          <span className="text-slate-500">•</span>
          <Link to="/login/otp" className="text-brand hover:text-brand-light transition-colors">
            OTP Sign In
          </Link>
        </div>

        <p className="text-center text-xs text-slate-500 mt-4">
          No account?{' '}
          <Link to="/signup" className="text-brand hover:text-brand-light font-semibold transition-colors">
            Create one
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
};

export default LoginPage;
