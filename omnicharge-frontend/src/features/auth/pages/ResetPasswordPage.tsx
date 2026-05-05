import React, { useState, useRef } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Lock, Eye, EyeOff } from 'lucide-react';
import { authApi } from '../../../core/api/services';
import { Spinner, FormField } from '../../../shared/components';
import AuthLayout from './AuthLayout';
import toast from 'react-hot-toast';

const schema = z.object({
  email: z.string().email(),
  newPassword: z.string().min(8, 'Password must be at least 8 characters'),
  confirmPassword: z.string(),
}).refine((d) => d.newPassword === d.confirmPassword, {
  message: 'Passwords do not match', path: ['confirmPassword'],
});
type FormData = z.infer<typeof schema>;

const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const emailFromState = (location.state as { email?: string })?.email || '';
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);
  const [showPw, setShowPw] = useState(false);
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const inputsRef = useRef<(HTMLInputElement | null)[]>([]);

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { email: emailFromState },
  });

  const handleOtpChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;
    const newOtp = [...otp];
    newOtp[index] = value.slice(-1);
    setOtp(newOtp);
    if (value && index < 5) inputsRef.current[index + 1]?.focus();
  };

  const handleOtpKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) inputsRef.current[index - 1]?.focus();
  };

  const handleOtpPaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    const newOtp = [...otp];
    pasted.split('').forEach((c, i) => { newOtp[i] = c; });
    setOtp(newOtp);
    inputsRef.current[Math.min(pasted.length, 5)]?.focus();
  };

  const onSubmit = async (data: FormData) => {
    const otpStr = otp.join('');
    if (otpStr.length !== 6) { toast.error('Enter the 6-digit OTP'); return; }
    setLoading(true);
    setApiError(null);
    try {
      await authApi.resetPassword({ email: data.email, otp: otpStr, newPassword: data.newPassword });
      toast.success('Password reset successfully!');
      navigate('/login');
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      setApiError(e.response?.data?.message || 'Reset failed. Check OTP and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout title="Set New Password" subtitle="Enter code and new password">
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {!emailFromState && (
          <FormField label="Email" error={errors.email?.message}>
            <input {...register('email')} type="email" className="input-field bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20" placeholder="you@example.com" />
          </FormField>
        )}

        <div>
          <label className="label block text-xs font-semibold text-slate-300 mb-2">Verification Code</label>
          <div className="flex gap-2" onPaste={handleOtpPaste}>
            {otp.map((digit, i) => (
              <input
                key={i}
                ref={(el) => { inputsRef.current[i] = el; }}
                value={digit}
                onChange={(e) => handleOtpChange(i, e.target.value)}
                onKeyDown={(e) => handleOtpKeyDown(i, e)}
                maxLength={1}
                inputMode="numeric"
                className="flex-1 bg-surface-card/30 border border-surface-border text-white text-xl text-center font-semibold focus:border-brand focus:ring-1 focus:ring-brand/20 rounded-lg py-2"
              />
            ))}
          </div>
        </div>

        <FormField label="New Password" error={errors.newPassword?.message}>
          <div className="relative">
            <Lock size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
            <input {...register('newPassword')} type={showPw ? 'text' : 'password'} placeholder="8+ characters" className="input-field pl-11 pr-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20" />
            <button type="button" onClick={() => setShowPw((v) => !v)} className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-300">
              {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
            </button>
          </div>
        </FormField>

        <FormField label="Confirm Password" error={errors.confirmPassword?.message}>
          <input {...register('confirmPassword')} type="password" placeholder="Repeat password" className="input-field bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20" />
        </FormField>

        {apiError && (
          <div className="bg-accent-red/10 border border-accent-red/30 rounded-lg px-3 py-2 text-xs text-accent-red text-center">{apiError}</div>
        )}

        <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-2.5 text-base font-semibold rounded-lg mt-6">
          {loading && <Spinner size={18} />}
          {loading ? 'Resetting...' : 'Reset Password'}
        </button>

        <Link to="/login" className="block text-center text-xs text-slate-500 hover:text-slate-400 transition-colors mt-4">
          Back to sign in
        </Link>
      </form>
    </AuthLayout>
  );
};

export default ResetPasswordPage;
