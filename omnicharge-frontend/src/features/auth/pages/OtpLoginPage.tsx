import React, { useState, useRef, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, ArrowLeft } from 'lucide-react';
import { useAppDispatch, useAppSelector } from '../../../shared/hooks/redux';
import { sendOtpThunk, verifyOtpThunk, clearError } from '../../../store/slices/authSlice';
import { Spinner, FormField } from '../../../shared/components';
import AuthLayout from './AuthLayout';
import toast from 'react-hot-toast';

const emailSchema = z.object({ email: z.string().email('Enter a valid email') });
type EmailForm = z.infer<typeof emailSchema>;

const OtpLoginPage: React.FC = () => {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const { loading, error } = useAppSelector((s) => s.auth);
  const [step, setStep] = useState<'email' | 'otp'>('email');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [resendTimer, setResendTimer] = useState(0);
  const inputsRef = useRef<(HTMLInputElement | null)[]>([]);

  const { register, handleSubmit, formState: { errors } } = useForm<EmailForm>({
    resolver: zodResolver(emailSchema),
  });

  useEffect(() => { dispatch(clearError()); }, [dispatch]);

  useEffect(() => {
    if (resendTimer > 0) {
      const t = setTimeout(() => setResendTimer((v) => v - 1), 1000);
      return () => clearTimeout(t);
    }
  }, [resendTimer]);

  const onSendOtp = async (data: EmailForm) => {
    const res = await dispatch(sendOtpThunk({ email: data.email }));
    if (sendOtpThunk.fulfilled.match(res)) {
      setEmail(data.email);
      setStep('otp');
      setResendTimer(60);
      toast.success('OTP sent to your email!');
      setTimeout(() => inputsRef.current[0]?.focus(), 100);
    }
  };

  const handleOtpChange = (index: number, value: string) => {
    if (!/^\d*$/.test(value)) return;
    const newOtp = [...otp];
    newOtp[index] = value.slice(-1);
    setOtp(newOtp);
    if (value && index < 5) inputsRef.current[index + 1]?.focus();
  };

  const handleOtpKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputsRef.current[index - 1]?.focus();
    }
  };

  const handleOtpPaste = (e: React.ClipboardEvent) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    const newOtp = [...otp];
    pasted.split('').forEach((c, i) => { newOtp[i] = c; });
    setOtp(newOtp);
    inputsRef.current[Math.min(pasted.length, 5)]?.focus();
  };

  const handleVerify = async () => {
    const otpStr = otp.join('');
    if (otpStr.length !== 6) { toast.error('Enter the 6-digit OTP'); return; }
    const res = await dispatch(verifyOtpThunk({ email, otp: otpStr }));
    if (verifyOtpThunk.fulfilled.match(res)) {
      toast.success('Welcome!');
      const role = res.payload.user?.role;
      navigate((role as any) === 'ROLE_ADMIN' ? '/admin' : '/dashboard');
    }
  };

  const handleResend = async () => {
    await dispatch(sendOtpThunk({ email }));
    setResendTimer(60);
    setOtp(['', '', '', '', '', '']);
    toast.success('New OTP sent!');
    setTimeout(() => inputsRef.current[0]?.focus(), 100);
  };

  return (
    <AuthLayout
      title={step === 'email' ? 'Sign In with OTP' : 'Verify Email'}
      subtitle={step === 'email' ? "We'll send a code to your email" : `Enter 6-digit code`}
    >
      {step === 'email' ? (
        <form onSubmit={handleSubmit(onSendOtp)} className="space-y-4">
          <FormField label="Email" error={errors.email?.message}>
            <div className="relative">
              <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
              <input
                {...register('email')}
                type="email"
                placeholder="you@example.com"
                className="input-field pl-11 bg-surface-card/30 border border-surface-border text-white placeholder-slate-600 focus:border-brand focus:ring-1 focus:ring-brand/20"
              />
            </div>
          </FormField>

          {error && (
            <div className="bg-accent-red/10 border border-accent-red/30 rounded-lg px-3 py-2 text-xs text-accent-red text-center">{error}</div>
          )}

          <button type="submit" disabled={loading} className="btn-primary w-full flex items-center justify-center gap-2 py-2.5 text-base font-semibold rounded-lg mt-6">
            {loading && <Spinner size={18} />}
            {loading ? 'Sending...' : 'Send Code'}
          </button>

          <Link to="/login" className="block text-center text-xs text-slate-500 hover:text-slate-400 flex items-center justify-center gap-1">
            <ArrowLeft size={14} /> Back
          </Link>
        </form>
      ) : (
        <div className="space-y-6">
          <div className="flex justify-center gap-2">
            {otp.map((digit, i) => (
              <input
                key={i}
                ref={(el) => { inputsRef.current[i] = el; }}
                value={digit}
                onChange={(e) => handleOtpChange(i, e.target.value)}
                onKeyDown={(e) => handleOtpKeyDown(i, e)}
                maxLength={1}
                inputMode="numeric"
                className="w-10 h-10 bg-surface-card/30 border border-surface-border text-white text-lg font-semibold text-center focus:border-brand focus:ring-1 focus:ring-brand/20 rounded-lg"
                onPaste={handleOtpPaste}
              />
            ))}
          </div>

          {error && (
            <div className="bg-accent-red/10 border border-accent-red/30 rounded-lg px-3 py-2 text-xs text-accent-red text-center">{error}</div>
          )}

          <button
            onClick={handleVerify}
            disabled={loading || otp.join('').length !== 6}
            className="btn-primary w-full flex items-center justify-center gap-2 py-2.5 text-base font-semibold rounded-lg disabled:opacity-50"
          >
            {loading && <Spinner size={18} />}
            {loading ? 'Verifying...' : 'Verify'}
          </button>

          <div className="flex items-center justify-between text-xs pt-2">
            <button
              onClick={() => { setStep('email'); setOtp(['', '', '', '', '', '']); }}
              className="text-slate-400 hover:text-slate-300 flex items-center gap-1"
            >
              <ArrowLeft size={14} /> Change
            </button>
            {resendTimer > 0 ? (
              <span className="text-slate-500">Resend in {resendTimer}s</span>
            ) : (
              <button onClick={handleResend} className="text-brand hover:text-brand-light font-medium">
                Resend
              </button>
            )}
          </div>
        </div>
      )}

      <p className="mt-6 text-center text-sm text-slate-300">
        Don't have an account?{' '}
        <Link to="/signup" className="text-brand hover:text-brand-light font-medium transition-colors">
          Create account
        </Link>
      </p>
    </AuthLayout>
  );
};

export default OtpLoginPage;
