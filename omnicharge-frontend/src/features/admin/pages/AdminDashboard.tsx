import React, { useEffect, useState, lazy, Suspense } from 'react';
import { Users, Zap, IndianRupee, TrendingUp, Activity } from 'lucide-react';
import { usersApi, rechargeApi, transactionApi } from '../../../core/api/services';
import type { RechargeResponse, UserResponse, TransactionResponse } from '../../../types';
import { Skeleton, ErrorState } from '../../../shared/components';
import { useIntersectionObserver } from '../../../shared/hooks/useOptimizations';


const Charts = lazy(() => import('./AdminDashboardCharts'));

interface StatCardProps {
  title: string; value: string | number; icon: React.ReactNode;
  gradient: string; change?: string; loading?: boolean;
}

const AdminStatCard: React.FC<StatCardProps> = ({ title, value, icon, gradient, change, loading }) => (
  <div className="relative overflow-hidden rounded-xl bg-transparent border border-surface-border/50 p-3 md:p-4 h-full aspect-square flex flex-col justify-between group hover:border-surface-border transition-all duration-200 max-w-48 md:max-w-56">
    
    {/* Header: Title and Icon */}
    <div className="flex items-start justify-between gap-2">
      <p className="text-slate-200 text-xs md:text-xs font-semibold uppercase tracking-tight flex-1 line-clamp-2">{title}</p>
      <div className="flex items-center justify-center flex-shrink-0">
        {React.cloneElement(icon as React.ReactElement<any>, { size: 20, className: `text-slate-300` })}
      </div>
    </div>

    {/* Center: Value with gradient text */}
    <div className="flex-1 flex items-center justify-center">
      {loading
        ? <Skeleton className="h-12 md:h-16 w-20 md:w-24 rounded" />
        : (
          <p className={`text-5xl md:text-6xl font-bold font-display leading-none text-center bg-gradient-to-r ${gradient} bg-clip-text text-transparent`}>
            {value}
          </p>
        )
      }
    </div>

    {/* Footer: Change indicator */}
    {change && (
      <div className="flex items-center justify-start">
        <p className="text-xs text-accent-green flex items-center gap-1">
          <TrendingUp size={10} />{change}
        </p>
      </div>
    )}
  </div>
);

const AdminDashboard: React.FC = () => {
  const [users, setUsers]       = useState<UserResponse[]>([]);
  const [recharges, setRecharges] = useState<RechargeResponse[]>([]);
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);

  // OPTIMIZATION: intersection observer — charts only render when scrolled into view
  const [chartsRef, chartsVisible] = useIntersectionObserver();

  useEffect(() => {
    Promise.all([usersApi.getAllUsers(), rechargeApi.getAll(0, 100)])
      .then(([uRes, rRes]) => {
        setUsers(uRes.data);
        setRecharges(rRes.data.content ?? []);
      })
      .catch(() => setError('Failed to load dashboard data'))
      .finally(() => setLoading(false));
  }, []);

  // Fetch transactions for chart data
  useEffect(() => {
    if (!recharges.length) return;
    
    Promise.all(recharges.map(r => 
      transactionApi.getByRechargeId(r.rechargeId)
        .then(txn => txn.data)
        .catch(() => null)
    ))
      .then(txns => {
        const validTxns = txns.filter((t): t is TransactionResponse => t !== null);
        setTransactions(validTxns);
      })
      .catch(() => setTransactions([]));
  }, [recharges]);

  // FIX: derive counts from fetched data — /recharge/count & /users/count don't exist on backend
  const totalRevenue = recharges
    .filter((r) => r.status === 'SUCCESS')
    .reduce((s, r) => s + r.amount, 0);

  const successCount = recharges.filter((r) => r.status === 'SUCCESS').length;
  const successRate = recharges.length
    ? `${Math.round((successCount / recharges.length) * 100)}%`
    : '—';

  if (error) return <ErrorState message={error} onRetry={() => window.location.reload()} />;

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="font-display text-2xl font-bold text-white">Admin Overview</h1>
        <p className="text-slate-200 mt-1 text-sm">Platform stats and insights</p>
      </div>

      {/* Stat cards */}
      <div className="p-4 md:p-0">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 md:gap-4">
          <AdminStatCard
            title="Total Users"
            value={loading ? '—' : users.length}
            icon={<Users size={20} className="text-white" />}
            gradient="from-sky-500 to-blue-600"
            loading={loading}
          />
          <AdminStatCard
            title="Total Recharges"
            value={loading ? '—' : recharges.length}
            icon={<Zap size={20} className="text-white" />}
            gradient="from-violet-500 to-purple-600"
            loading={loading}
          />
          <AdminStatCard
            title="Revenue (INR)"
            value={loading ? '—' : `₹${totalRevenue.toLocaleString()}`}
            icon={<IndianRupee size={20} className="text-white" />}
            gradient="from-emerald-500 to-green-600"
            loading={loading}
          />
          <AdminStatCard
            title="Success Rate"
            value={loading ? '—' : successRate}
            icon={<Activity size={20} className="text-white" />}
            gradient="from-amber-500 to-orange-600"
            loading={loading}
          />
        </div>
      </div>

      {/* Charts — lazy rendered on scroll */}
      <div ref={chartsRef} className="space-y-6">
        {chartsVisible ? (
          <Suspense fallback={
            <div className="space-y-6">
              <div className="h-64 shimmer-bg rounded-xl border border-surface-border/50" />
              <div className="h-48 shimmer-bg rounded-xl border border-surface-border/50" />
            </div>
          }>
            <Charts recharges={recharges} transactions={transactions} loading={loading} />
          </Suspense>
        ) : (
          <div className="space-y-6">
            <div className="h-64 shimmer-bg rounded-xl border border-surface-border/50" />
            <div className="h-48 shimmer-bg rounded-xl border border-surface-border/50" />
          </div>
        )}
      </div>

      {/* Recent activity */}
      <div className="p-4 sm:p-6 rounded-xl border border-surface-border/50 bg-transparent">
        <h3 className="font-display font-bold text-white mb-4">Recent Recharges</h3>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-surface-border">
                {['ID','Mobile','Amount','Status','Date'].map((h) => (
                  <th key={h} className="text-left py-3 px-2 text-xs text-slate-300 font-semibold uppercase tracking-wider">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {loading
                ? Array.from({ length: 5 }).map((_, i) => (
                    <tr key={i} className="table-row">
                      {Array.from({ length: 5 }).map((_, j) => (
                        <td key={j} className="py-3 px-2"><Skeleton className="h-4 w-full" /></td>
                      ))}
                    </tr>
                  ))
                : recharges.slice(0, 8).map((r) => (
                    <tr key={r.rechargeId} className="table-row">
                      <td className="py-3 px-2 text-slate-200 font-mono">#{r.rechargeId}</td>
                      <td className="py-3 px-2 text-white">Plan #{r.planId}</td>
                      <td className="py-3 px-2 text-white font-semibold">₹{r.amount}</td>
                      <td className="py-3 px-2">
                        <span className={`badge text-xs ${
                          r.status === 'SUCCESS'  ? 'badge-success'  :
                          r.status === 'PENDING'  ? 'badge-warning'  :
                          r.status === 'FAILED'   ? 'badge-danger'   : 'badge-info'
                        }`}>{r.status}</span>
                      </td>
                      <td className="py-3 px-2 text-slate-300">
                        {r.rechargeId}
                      </td>
                    </tr>
                  ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
