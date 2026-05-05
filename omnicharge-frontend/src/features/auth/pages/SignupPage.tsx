import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Eye, EyeOff, Mail, Lock, Phone } from 'lucide-react';
import { usersApi } from '../../../core/api/services';
import { Spinner, FormField } from '../../../shared/components';
import AuthLayout from './AuthLayout';
import toast from 'react-hot-toast';

const schema = z.object({
  firstName: z.string().min(2, 'First name too short'),
  lastName: z.string().min(2, 'Last name too short'),
  email: z.string().email('Enter a valid email'),
  phone: z.string().regex(/^[6-9]\d{9}$/, 'Phone must be a 10-digit number starting with 6-9'),
  password: z.string().regex(
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,20}$/,
    'Password must be 8-20 characters with uppercase, lowercase, number & special character'
  ),
  confirmPassword: z.string(),
}).refine((d) => d.password === d.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});
type FormData = z.infer<typeof schema>;

const SignupPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [showPw, setShowPw] = useState(false);
  const [showCPw, setShowCPw] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setLoading(true);
    setApiError(null);
    try {
      await usersApi.register({
        name: `${data.firstName} ${data.lastName}`,
        email: data.email,
        contactNo: data.phone,
        password: data.password,
      });
      toast.success('Account created! Please sign in.');
      navigate('/login');
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      setApiError(e.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Create Account" subtitle="Sign up to get started">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div className="grid grid-cols-2 gap-3">
          <FormField label="First Name" error={errors.firstName?.message}>
            <input {...register('firstName')} placeholder="John" className="input-field bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20" />
          </FormField>
          <FormField label="Last Name" error={errors.lastName?.message}>
            <input {...register('lastName')} placeholder="Doe" className="input-field bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20" />
          </FormField>
        </div>

        <FormField label="Email" error={errors.email?.message}>
          <div className="relative">
            <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input {...register('email')} type="email" placeholder="you@example.com" className="input-field pl-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20" />
          </div>
        </FormField>

        <FormField label="Phone" error={errors.phone?.message}>
          <div className="relative">
            <Phone size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input {...register('phone')} type="tel" placeholder="10-digit" className="input-field pl-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20" />
          </div>
        </FormField>

        <FormField label="Password" error={errors.password?.message}>
          <div className="relative">
            <Lock size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              {...register('password')}
              type={showPw ? 'text' : 'password'}
              placeholder="8+ chars with uppercase & symbol"
              className="input-field pl-11 pr-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20 text-sm"
            />
            <button type="button" onClick={() => setShowPw((v) => !v)} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-300">
              {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          </div>
        </FormField>

        <FormField label="Confirm Password" error={errors.confirmPassword?.message}>
          <div className="relative">
            <Lock size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              {...register('confirmPassword')}
              type={showCPw ? 'text' : 'password'}
              placeholder="Repeat password"
              className="input-field pl-11 pr-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20"
            />
            <button type="button" onClick={() => setShowCPw((v) => !v)} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-300">
              {showCPw ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          </div>
        </FormField>

        {apiError && (
          <div className="bg-accent-red/10 border border-accent-red/30 rounded-lg px-3 py-2 text-xs text-accent-red text-center">{apiError}</div>
        )}

        <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-2.5 text-base font-semibold rounded-lg mt-6">
          {loading && <Spinner size={18} />}
          {loading ? 'Creating...' : 'Create Account'}
        </button>

        <p className="text-center text-xs text-slate-500 mt-4">
          Already have an?{' '}
          <Link to="/login" className="text-brand hover:text-brand-light font-semibold transition-colors">
            Sign in
          </Link>
        </p>
      </form>
    </AuthLayout>
  );
};

export default SignupPage;
