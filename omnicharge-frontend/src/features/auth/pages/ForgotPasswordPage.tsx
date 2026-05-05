import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Mail } from 'lucide-react';
import { authApi } from '../../../core/api/services';
import { Spinner, FormField } from '../../../shared/components';
import AuthLayout from './AuthLayout';
import toast from 'react-hot-toast';

const schema = z.object({ email: z.string().email('Enter a valid email') });
type FormData = z.infer<typeof schema>;

const ForgotPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  const { register, handleSubmit, getValues, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
    setLoading(true);
    setApiError(null);
    try {
      await authApi.forgotPassword({ email: data.email });
      setSent(true);
      toast.success('OTP sent to your email!');
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      setApiError(e.response?.data?.message || 'Email not found');
    } finally {
      setLoading(false);
    }
  };

  if (sent) {
    return (
      <AuthLayout title="Check Your Email" subtitle="We sent a reset code">
        <div className="space-y-8 text-center">
          <div className="text-5xl">✉️</div>
          <div>
            <p className="text-slate-400 text-sm mb-1">Reset code sent to</p>
            <p className="text-white font-semibold text-sm break-all">{getValues('email')}</p>
          </div>
          <button
            onClick={() => navigate('/reset-password', { state: { email: getValues('email') } })}
            className="btn-primary w-full py-2.5 text-base font-semibold rounded-lg"
          >
            Next
          </button>
          <Link to="/login" className="block text-xs text-slate-500 hover:text-slate-400 transition-colors">
            Back
          </Link>
        </div>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout title="Forgot Password" subtitle="We'll send a reset code">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <FormField label="Email" error={errors.email?.message}>
          <div className="relative">
            <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input {...register('email')} type="email" placeholder="you@example.com" className="input-field pl-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20" />
          </div>
        </FormField>

        {apiError && (
          <div className="bg-accent-red/10 border border-accent-red/30 rounded-lg px-3 py-2 text-xs text-accent-red text-center">{apiError}</div>
        )}

        <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-2.5 text-base font-semibold rounded-lg mt-6">
          {loading && <Spinner size={18} />}
          {loading ? 'Sending...' : 'Send Code'}
        </button>

        <Link to="/login" className="block text-center text-xs text-slate-500 hover:text-slate-400 transition-colors mt-4">
          Back
        </Link>
      </form>
    </AuthLayout>
  );
};

export default ForgotPasswordPage;
