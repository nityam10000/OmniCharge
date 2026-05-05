import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { User, Mail, Shield, Edit3, Check, X } from 'lucide-react';
import { useAppSelector, useAppDispatch } from '../../../shared/hooks/redux';
import { usersApi } from '../../../core/api/services';
import { fetchProfileThunk } from '../../../store/slices/authSlice';
import { PageHeader, FormField, Spinner } from '../../../shared/components';
import toast from 'react-hot-toast';

const schema = z.object({
  name:  z.string().min(2, 'Name must be at least 2 characters'),
  email: z.string().email('Enter a valid email'),
});
type FormData = z.infer<typeof schema>;

const ProfilePage: React.FC = () => {
  const dispatch = useAppDispatch();
  const { user } = useAppSelector((s) => s.auth);
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(false);
  const [initialLoading, setInitialLoading] = useState(true);

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      name:  user?.name || '',
      email: user?.email || '',
    },
  });

  // Fetch user profile on mount or if user not loaded
  useEffect(() => {
    if (user) {
      // User already loaded from Redux
      setInitialLoading(false);
      return;
    }

    const loadProfile = async () => {
      try {
        setInitialLoading(true);
        // Set a timeout to prevent infinite loading
        const timeout = setTimeout(() => {
          console.warn('Profile fetch timeout');
          setInitialLoading(false);
        }, 5000);

        await dispatch(fetchProfileThunk() as any);
        clearTimeout(timeout);
      } catch (err) {
        console.error('Failed to load profile:', err);
      } finally {
        setInitialLoading(false);
      }
    };
    
    loadProfile();
  }, []); // Only run once on mount

  // Update form values when user data changes
  useEffect(() => {
    if (user) {
      reset({
        name:  user.name || '',
        email: user.email || '',
      });
    }
  }, [user, reset]);

  const onSubmit = async (data: FormData) => {
    setLoading(true);
    try {
      // FIX: pass structured data — services.ts toUserRequest() maps fields correctly
      await usersApi.updateProfile({
        name:  data.name,
        email: data.email,
      });
      await dispatch(fetchProfileThunk());
      toast.success('Profile updated!');
      setEditing(false);
    } catch (err: unknown) {
      const e = err as { response?: { data?: { message?: string } } };
      toast.error(e.response?.data?.message || 'Update failed');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => { reset(); setEditing(false); };

  if (initialLoading) {
    return (
      <div className="animate-fade-in">
        <PageHeader title="Profile" subtitle="Manage your account information" />
        <div className="card flex items-center justify-center min-h-48 sm:min-h-64">
          <div className="flex flex-col items-center gap-3 text-center">
            <Spinner size={32} />
            <p className="text-slate-300 text-sm">Loading your profile...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="animate-fade-in">
        <PageHeader title="Profile" subtitle="Manage your account information" />
        <div className="card text-center py-8 sm:py-12">
          <p className="text-slate-300 mb-4 text-sm">Unable to load your profile.</p>
          <button 
            onClick={() => window.location.href = '/login'}
            className="btn-primary"
          >
            Return to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <PageHeader title="Profile" subtitle="Manage your account information" />

      <div className="card relative">
        {/* Edit Button - Top Right Corner */}
        {!editing && (
          <button
            onClick={() => setEditing(true)}
            className="absolute top-4 right-4 sm:top-6 sm:right-6 btn-ghost flex items-center justify-center gap-2 text-xs sm:text-sm p-2 sm:py-2 sm:px-3 flex-shrink-0"
          >
            <Edit3 size={16} /> <span className="hidden sm:inline">Edit</span>
          </button>
        )}

        {/* Avatar Section */}
        <div className="flex flex-col sm:flex-row sm:items-center gap-4 mb-6 sm:mb-8 pb-6 sm:pb-8 border-b border-surface-border">
          <div className="w-14 h-14 sm:w-16 sm:h-16 rounded-2xl bg-gradient-brand flex items-center justify-center text-xl sm:text-2xl font-bold text-white flex-shrink-0">
            {user?.name?.split(' ')[0]?.[0]?.toUpperCase() || 'U'}{user?.name?.split(' ')[1]?.[0]?.toUpperCase() || 'S'}
          </div>
          <div className="flex-1 min-w-0">
            <h2 className="font-display font-bold text-lg sm:text-xl text-white">
              {user?.name || 'User Profile'}
            </h2>
            <p className="text-slate-300 text-xs sm:text-sm line-clamp-1">{user?.email || 'No email'}</p>
            <span className={`badge mt-2 ${(user?.role as any) === 'ROLE_ADMIN' ? 'badge-warning' : 'badge-info'}`}>
              {(user?.role as any) === 'ROLE_ADMIN' ? 'Administrator' : 'User'}
            </span>
          </div>
        </div>

        {editing ? (
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 sm:space-y-5">
            <FormField label="Full name" error={errors.name?.message}>
              <input {...register('name')} className="input-field text-sm" />
            </FormField>
            <FormField label="Email" error={errors.email?.message}>
              <input {...register('email')} type="email" className="input-field text-sm" />
            </FormField>
            <div className="flex flex-col sm:flex-row gap-2 sm:gap-3 pt-2">
              <button type="submit" disabled={loading} className="btn-primary flex items-center justify-center gap-2 py-2.5 sm:py-3 text-sm sm:text-base flex-1 sm:flex-initial">
                {loading ? <Spinner size={15} /> : <Check size={16} />}
                {loading ? 'Saving…' : 'Save Changes'}
              </button>
              <button type="button" onClick={handleCancel} className="btn-ghost flex items-center justify-center gap-2 py-2.5 sm:py-3 text-sm sm:text-base flex-1 sm:flex-initial">
                <X size={16} /> Cancel
              </button>
            </div>
          </form>
        ) : (
          <div className="space-y-3 sm:space-y-4">
            {[
              { icon: User,   label: 'Full name', value: user?.name || 'Not set' },
              { icon: Mail,   label: 'Email',     value: user?.email || 'No email' },
              { icon: Shield, label: 'Role',      value: (user?.role?.replace('ROLE_', '') || 'User') },
            ].map(({ icon: Icon, label, value }) => (
              <div key={label} className="flex items-center gap-3 sm:gap-4 p-3 sm:p-4 bg-surface-elevated rounded-lg sm:rounded-xl">
                <div className="w-8 h-8 sm:w-9 sm:h-9 rounded-lg bg-surface-card flex items-center justify-center flex-shrink-0">
                  <Icon size={16} className="text-brand" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-xs text-slate-300">{label}</p>
                  <p className="text-white text-sm sm:text-base font-medium break-all">{value}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default ProfilePage;
