// OPTIMIZATION: This file is lazy-loaded (see AdminDashboard.tsx).
// Recharts is only bundled into a separate chunk, not loaded on initial page load.
import React from 'react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, PieChart, Pie, Cell, Legend,
} from 'recharts';
import type { RechargeResponse, TransactionResponse } from '../../../types';

const COLORS = ['#0EA5E9', '#6366F1', '#10B981', '#F59E0B', '#EF4444'];

interface Props {
  recharges: RechargeResponse[];
  transactions: TransactionResponse[];
  loading: boolean;
}

const AdminDashboardCharts: React.FC<Props> = ({ recharges, transactions, loading }) => {
  // Build chart data from last 7 days using transaction timestamps
  const last7 = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    // Get start and end of the day
    const dayStart = new Date(d.getFullYear(), d.getMonth(), d.getDate());
    const dayEnd = new Date(d.getFullYear(), d.getMonth(), d.getDate() + 1);
    
    const label = d.toLocaleDateString('en', { weekday: 'short' });
    
    // Filter transactions for this specific day
    const dayTransactions = transactions.filter((t) => {
      const txnDate = new Date(t.createdAt);
      return txnDate >= dayStart && txnDate < dayEnd;
    });
    
    // Get recharges for transactions on this day
    const dayRechargeIds = new Set(dayTransactions.map(t => t.rechargeId));
    const dayRecharges = recharges.filter(r => dayRechargeIds.has(r.rechargeId));
    
    return {
      day: label,
      recharges: dayRecharges.length,
      revenue: dayRecharges
        .filter((r) => r.status === 'SUCCESS')
        .reduce((s, r) => s + r.amount, 0),
    };
  });

  const statusDist = ['SUCCESS', 'PENDING', 'FAILED', 'REFUNDED']
    .map((s) => ({ name: s, value: recharges.filter((r) => r.status === s).length }))
    .filter((d) => d.value > 0);

  if (loading) return null;

  return (
    <div className="space-y-6">
      {/* Charts Row 1 */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        <div className="sm:p-6 p-4 rounded-xl border border-surface-border/50 bg-transparent xl:col-span-2 w-full overflow-hidden">
          <h3 className="font-display font-bold text-white mb-6">Revenue & Recharges (Last 7 Days)</h3>
          <div className="w-full h-56 sm:h-56 md:h-56">
            <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={last7}>
              <defs>
                <linearGradient id="rev" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%"  stopColor="#0EA5E9" stopOpacity={0.25} />
                  <stop offset="95%" stopColor="#0EA5E9" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="rec" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%"  stopColor="#6366F1" stopOpacity={0.25} />
                  <stop offset="95%" stopColor="#6366F1" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
              <XAxis dataKey="day" tick={{ fill: '#CBD5E1', fontSize: 12, angle: -45, textAnchor: 'end', height: 80 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: '#CBD5E1', fontSize: 11 }} axisLine={false} tickLine={false} />
              <Tooltip
                contentStyle={{ background: '#1E293B', border: '1px solid #334155', borderRadius: 12, color: '#F1F5F9' }}
              />
              <Area type="monotone" dataKey="revenue"   stroke="#0EA5E9" strokeWidth={2} fill="url(#rev)" name="Revenue ₹" />
              <Area type="monotone" dataKey="recharges" stroke="#6366F1" strokeWidth={2} fill="url(#rec)" name="Recharges" />
            </AreaChart>
          </ResponsiveContainer>
          </div>
        </div>

        <div className="sm:p-6 p-4 rounded-xl border border-surface-border/50 bg-transparent w-full overflow-hidden">
          <h3 className="font-display font-bold text-white mb-6">Status Distribution</h3>
          {statusDist.length === 0 ? (
            <div className="flex items-center justify-center h-40 text-slate-300">No data</div>
          ) : (
            <div className="w-full h-48">
              <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={statusDist}
                  cx="50%" cy="50%"
                  innerRadius={55} outerRadius={80}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {statusDist.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                </Pie>
                <Tooltip contentStyle={{ background: '#1E293B', border: '1px solid #334155', borderRadius: 12 }} />
                <Legend
                  iconType="circle" iconSize={8}
                  formatter={(v) => <span style={{ color: '#CBD5E1', fontSize: 12 }}>{v}</span>}
                />
              </PieChart>
            </ResponsiveContainer>
            </div>
          )}
        </div>
      </div>

      {/* Charts Row 2 */}
      <div className="sm:p-6 p-4 rounded-xl border border-surface-border/50 bg-transparent w-full overflow-hidden">
        <h3 className="font-display font-bold text-white mb-6">Daily Recharge Volume</h3>
        <div className="w-full h-44">
          <ResponsiveContainer width="100%" height="100%">
          <BarChart data={last7}>
            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
            <XAxis dataKey="day" tick={{ fill: '#CBD5E1', fontSize: 12, angle: -45, textAnchor: 'end', height: 80 }} axisLine={false} tickLine={false} />
            <YAxis tick={{ fill: '#CBD5E1', fontSize: 11 }} axisLine={false} tickLine={false} />
            <Tooltip contentStyle={{ background: '#1E293B', border: '1px solid #334155', borderRadius: 12, color: '#F1F5F9' }} />
            <Bar dataKey="recharges" fill="#0EA5E9" radius={[6, 6, 0, 0]} name="Recharges" />
          </BarChart>
        </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboardCharts;
