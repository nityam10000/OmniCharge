import React, { useEffect, useState, memo } from 'react';
import { Link } from 'react-router-dom';
import { Zap, CreditCard, TrendingUp, ArrowRight, CheckCircle, Clock, XCircle } from 'lucide-react';
import { useAppSelector } from '../../../shared/hooks/redux';
import { useAppContext } from '../../../core/context/AppContext';
import { rechargeApi, transactionApi } from '../../../core/api/services';
import type { RechargeResponse, TransactionResponse, PageResponse } from '../../../types';
import { StatusBadge, Skeleton, ErrorState } from '../../../shared/components';
import toast from 'react-hot-toast';

const StatCard = memo<{
  title: string; value: string | number; icon: React.ReactNode;
  gradient: string; subtitle?: string;
}>(({ title, value, icon, gradient, subtitle }) => (
  <div className="relative overflow-hidden rounded-xl bg-surface-card/30 backdrop-blur-md border border-surface-border/40 p-3 md:p-4 h-full aspect-square flex flex-col justify-between group hover:border-surface-border/60 transition-all duration-200 w-full">
    
    {/* Header: Title and Icon */}
    <div className="flex items-start justify-between gap-2">
      <p className="text-slate-200 text-xs md:text-xs font-semibold uppercase tracking-tight flex-1 line-clamp-2">{title}</p>
      <div className="flex items-center justify-center flex-shrink-0">
        {React.cloneElement(icon as React.ReactElement<any>, { size: 20, className: `text-slate-300` })}
      </div>
    </div>

    {/* Center: Value with gradient text */}
    <div className="flex-1 flex items-center justify-center">
      <p className={`text-5xl md:text-6xl font-bold font-display leading-none text-center bg-gradient-to-r ${gradient} bg-clip-text text-transparent`}>
        {value}
      </p>
    </div>

    {/* Footer: Subtitle */}
    {subtitle && (
      <div className="flex items-center justify-start">
        <p className="text-xs text-slate-300">{subtitle}</p>
      </div>
    )}
  </div>
));

const greeting = () => {
  const h = new Date().getHours();
  if (h < 12) return 'Good morning';
  if (h < 17) return 'Good afternoon';
  return 'Good evening';
};

const UserDashboard: React.FC = () => {
  const { user } = useAppSelector((s) => s.auth);
  const { myRecharges, setMyRecharges, myTransactions, setMyTransactions } = useAppContext();

  const [recharges, setRecharges] = useState<RechargeResponse[]>(myRecharges ?? []);
  const [transactions, setTransactions] = useState<TransactionResponse[]>(myTransactions ?? []);
  const [loading, setLoading] = useState(!myRecharges || !myTransactions);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    // OPTIMIZATION: skip fetch if context cache already has data AND user hasn't changed
    if (myRecharges && myTransactions && user?.userId) {
      setRecharges(myRecharges);
      setTransactions(myTransactions);
      return;
    }
    let cancelled = false;
    const load = async () => {
      try {
        const [rRes, tRes] = await Promise.all([
          rechargeApi.getMyRecharges(0, 100),
          transactionApi.getMyTransactions(0, 100),
        ]);
        if (!cancelled) {
          const rechargesData = (rRes.data as unknown as PageResponse<RechargeResponse>).content;
          const transactionsData = (tRes.data as unknown as PageResponse<TransactionResponse>).content;
          setRecharges(rechargesData);
          setTransactions(transactionsData);
          // Store in context for other pages (MyRechargesPage, etc.)
          setMyRecharges(rechargesData);
          setMyTransactions(transactionsData);
        }
      } catch (err: unknown) {
        if (!cancelled) {
          const e = err as { response?: { data?: { message?: string } } };
          const msg = e.response?.data?.message || 'Failed to load dashboard data';
          setError(msg);
          toast.error(msg);
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [myRecharges, myTransactions, setMyRecharges, setMyTransactions, user?.userId]);

  const totalSpent = recharges
    .filter((r) => r.status === 'SUCCESS')
    .reduce((sum, r) => sum + r.amount, 0);

  const successCount = recharges.filter((r) => r.status === 'SUCCESS').length;
  const recentRecharges = recharges.slice(0, 5);

  if (error && !loading) {
    return <ErrorState message={error} onRetry={() => { setError(null); setLoading(true); }} />;
  }

  return (
    <div className="space-y-6 md:space-y-8 animate-fade-in">
      {/* Hero Section with Welcome & Featured Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6">
        {/* Welcome Card - Left (2 columns on desktop only) */}
        <div className="col-span-2 lg:col-span-2 rounded-2xl border border-surface-border/40 bg-gradient-to-br from-surface-card/50 via-surface-card/30 to-transparent backdrop-blur-md p-6 md:p-8 flex flex-col justify-between min-h-48">
          <div>
            <p className="text-slate-300 text-xs md:text-sm font-semibold uppercase tracking-wider mb-1">Welcome back</p>
            <h1 className="font-display text-2xl md:text-3xl font-bold text-white mb-2">
              {greeting()}, {user?.name?.split(' ')[0]}
            </h1>
            <p className="text-slate-300 text-xs md:text-sm leading-relaxed">
              Track your recharges and manage your account seamlessly.
            </p>
          </div>
          <Link to="/recharge" className="btn-primary flex items-center justify-center gap-2 text-sm w-full mt-6 py-3">
            <Zap size={18} /> Start Recharge
          </Link>
        </div>

        {/* Featured Stats - Right (2 columns) */}
        {loading ? (
          <>
            <Skeleton className="rounded-2xl col-span-1 h-48" />
            <Skeleton className="rounded-2xl col-span-1 h-48" />
          </>
        ) : (
          <>
            <StatCard 
              title="Total Recharges" 
              value={recharges.length} 
              icon={<Zap size={20} className="text-slate-300" />} 
              gradient="from-sky-500 to-blue-600"
              subtitle="All time" 
            />
            <StatCard 
              title="Success Rate" 
              value={`${recharges.length ? Math.round((successCount / recharges.length) * 100) : 0}%`}
              icon={<CheckCircle size={20} className="text-slate-300" />} 
              gradient="from-emerald-500 to-green-600"
              subtitle={`${successCount} successful`}
            />
          </>
        )}
      </div>

      {/* Middle Row - Full Width Stats Grid */}
      {!loading && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
          <div className="rounded-2xl border border-surface-border/40 bg-surface-card/30 backdrop-blur-md p-6 md:p-8">
            <div className="flex items-center justify-between mb-6">
              <div>
                <p className="text-slate-300 text-xs font-semibold uppercase tracking-tight">Total Amount Spent</p>
                <p className={`text-5xl md:text-6xl font-bold font-display mt-3 bg-gradient-to-r from-violet-500 to-purple-600 bg-clip-text text-transparent`}>
                  ₹{totalSpent.toLocaleString()}
                </p>
                <p className="text-slate-300 text-xs mt-2">Across {recharges.length} recharges</p>
              </div>
              <CreditCard size={40} className="text-violet-500 opacity-20" />
            </div>
          </div>

          <div className="rounded-2xl border border-surface-border/40 bg-surface-card/30 backdrop-blur-md p-6 md:p-8">
            <div className="flex items-center justify-between mb-6">
              <div>
                <p className="text-slate-300 text-xs font-semibold uppercase tracking-tight">Total Transactions</p>
                <p className={`text-5xl md:text-6xl font-bold font-display mt-3 bg-gradient-to-r from-amber-500 to-orange-600 bg-clip-text text-transparent`}>
                  {transactions.length}
                </p>
                <p className="text-slate-300 text-xs mt-2">Activity recorded</p>
              </div>
              <TrendingUp size={40} className="text-amber-500 opacity-20" />
            </div>
          </div>
        </div>
      )}

      {/* Recent Recharges - Enhanced Card */}
      <div className="rounded-2xl border border-surface-border/40 bg-surface-card/30 backdrop-blur-md overflow-hidden">
        <div className="flex items-center justify-between p-6 md:p-8 border-b border-surface-border/40 bg-surface-card/50">
          <div>
            <h2 className="font-display font-bold text-white text-lg md:text-xl">Recent Activity</h2>
            <p className="text-slate-300 text-xs mt-1">Your latest recharges</p>
          </div>
          <Link to="/my-recharges" className="text-xs md:text-sm text-brand hover:text-brand-light flex items-center gap-2 transition-colors font-medium">
            View all <ArrowRight size={16} />
          </Link>
        </div>

        {loading ? (
          <div className="space-y-3 p-6 md:p-8">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="flex items-center gap-4 p-3">
                <Skeleton className="w-10 h-10 rounded-lg flex-shrink-0" />
                <div className="flex-1 space-y-2">
                  <Skeleton className="h-3 w-40" />
                  <Skeleton className="h-2 w-24" />
                </div>
                <Skeleton className="h-6 w-20 rounded" />
              </div>
            ))}
          </div>
        ) : recentRecharges.length === 0 ? (
          <div className="text-center py-12 px-6">
            <Zap size={40} className="mx-auto text-slate-500 mb-3 opacity-50" />
            <p className="text-slate-300 mb-4 text-sm font-medium">No recharges yet</p>
            <p className="text-slate-400 text-xs mb-6">Start your first recharge to see activity here</p>
            <Link to="/recharge" className="btn-primary text-xs md:text-sm px-4 md:px-6 py-2.5 inline-flex items-center gap-2">
              <Zap size={16} /> Make your first recharge
            </Link>
          </div>
        ) : (
          <div className="divide-y divide-surface-border/40">
            {recentRecharges.map((r, _idx) => (
              <div key={r.rechargeId} className="flex items-center justify-between gap-4 p-4 md:p-5 hover:bg-surface-elevated/30 transition-colors">
                <div className="flex items-center gap-4 flex-1 min-w-0">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0 ${
                    r.status === 'SUCCESS' ? 'bg-accent-green/10' :
                    r.status === 'PENDING' ? 'bg-accent-amber/10' :
                    'bg-accent-red/10'
                  }`}>
                    {r.status === 'SUCCESS' ? <CheckCircle size={18} className="text-accent-green" /> :
                     r.status === 'PENDING' ? <Clock size={18} className="text-accent-amber" /> :
                     <XCircle size={18} className="text-accent-red" />}
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-semibold text-white">
                      Plan #{r.planId}
                    </p>
                    <p className="text-xs text-slate-400">ID: {r.rechargeId}</p>
                  </div>
                </div>
                <div className="flex items-center gap-4 flex-shrink-0">
                  <span className="text-sm font-bold text-white">₹{r.amount}</span>
                  <StatusBadge status={r.status} />
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Quick Actions - Dynamic Grid */}
      <div>
        <h2 className="font-display font-bold text-white mb-4 md:mb-5 text-lg md:text-xl">Quick Actions</h2>
        <div className="grid grid-cols-3 md:grid-cols-3 gap-2 md:gap-4 lg:gap-6">
          {[
            { to: '/recharge', icon: Zap, iconColor: 'text-brand', title: 'New Recharge', desc: 'Browse plans & recharge' },
            { to: '/my-recharges', icon: ArrowRight, iconColor: 'text-sky-500', title: 'Recharge History', desc: 'View past recharges' },
            { to: '/profile', icon: CreditCard, iconColor: 'text-violet-500', title: 'Edit Profile', desc: 'Account settings' },
          ].map((a) => {
            const Icon = a.icon;
            return (
              <Link key={a.to} to={a.to} className="group rounded-full md:rounded-xl backdrop-blur-md hover:border-surface-border/80 hover:bg-surface-card/30 transition-all duration-300 p-3 md:p-4 lg:p-6 flex flex-col items-center md:items-start text-center md:text-left">
                <div className="w-10 h-10 md:w-10 md:h-10 lg:w-12 lg:h-12 rounded-lg bg-surface-elevated/50 group-hover:bg-surface-elevated flex items-center justify-center mb-2 md:mb-3 lg:mb-4 transition-colors">
                  <Icon size={16} className={`${a.iconColor}`} />
                </div>
                <p className="font-semibold text-white text-xs md:text-sm lg:text-base group-hover:text-brand transition-colors leading-tight">{a.title}</p>
                <p className="hidden md:block text-xs text-slate-400 leading-snug mt-0.5">{a.desc}</p>
                <div className="hidden md:flex mt-auto pt-3 lg:pt-4 text-slate-600 group-hover:text-brand transition-colors">
                  <ArrowRight size={16} />
                </div>
              </Link>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default UserDashboard;
