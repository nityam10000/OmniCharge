import React, { useEffect, useState, useCallback, useMemo } from 'react';
import { Search } from 'lucide-react';
import toast from 'react-hot-toast';
import { rechargeApi } from '../../../core/api/services';
import type { RechargeResponse, PageResponse, RechargeStatus } from '../../../types';
import { PageHeader, Table, StatusBadge, Pagination, Spinner } from '../../../shared/components';
import { useDebounce } from '../../../shared/hooks/useOptimizations';

const STATUS_OPTIONS: RechargeStatus[] = ['PENDING', 'SUCCESS', 'FAILED', 'REFUNDED'];

const AdminRechargesPage: React.FC = () => {
  const [page, setPage]         = useState(0);
  const [pageData, setPageData] = useState<PageResponse<RechargeResponse> | null>(null);
  const [loading, setLoading]   = useState(true);
  const [search, setSearch]     = useState('');
  const [updating, setUpdating] = useState<number | null>(null);

  const debouncedSearch = useDebounce(search, 300);

  const load = useCallback(async (p: number) => {
    setLoading(true);
    try {
      const { data } = await rechargeApi.getAll(p, 10);
      setPageData(data);
    } catch { toast.error('Failed to load recharges'); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(page); }, [load, page]);

  const handleStatusUpdate = async (id: number, status: RechargeStatus) => {
    setUpdating(id);
    try {
      await rechargeApi.updateStatus(id, status);
      toast.success(`Status updated to ${status}`);
      load(page);
    } catch { toast.error('Status update failed'); }
    finally { setUpdating(null); }
  };

  // OPTIMIZATION: client-side filter on current page with debounce
  const filtered = useMemo(() => {
    const q = debouncedSearch.toLowerCase();
    if (!q || !pageData) return pageData?.content ?? [];
    return pageData.content.filter((r) =>
      String(r.rechargeId).includes(q) ||
      r.status.toLowerCase().includes(q) ||
      String(r.planId).includes(q)
    );
  }, [pageData, debouncedSearch]);

  const cols = [
    { key: 'id',      label: 'Recharge ID' },
    { key: 'amount',  label: 'Amount' },
    { key: 'status',  label: 'Status' },
    { key: 'actions', label: 'Update Status' },
  ];

  return (
    <div className="animate-fade-in space-y-6">
      <PageHeader
        title="Recharges"
        subtitle={pageData ? `${pageData.totalElements} total` : ''}
      />

      <div className="relative w-full md:max-w-sm">
        <Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-300" />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by ID or status…"
          className="input-field pl-11 w-full text-sm"
        />
      </div>

      {/* Desktop Table View */}
      <div className="hidden md:block overflow-x-auto">
        <Table columns={cols} loading={loading}>
          {filtered.length === 0 ? (
            <tr><td colSpan={4} className="py-12 text-center text-slate-300">No recharges found</td></tr>
          ) : filtered.map((r) => (
            <tr key={r.rechargeId} className="table-row">
              <td className="px-4 py-3 text-slate-200 font-mono text-xs">#{r.rechargeId}</td>
              <td className="px-4 py-3 text-white font-semibold">₹{r.amount}</td>
              <td className="px-4 py-3"><StatusBadge status={r.status} /></td>
              <td className="px-4 py-3">
                <div className="flex items-center gap-1 flex-wrap">
                  {STATUS_OPTIONS.filter((s) => s !== r.status).map((s) => (
                    <button
                      key={s}
                      disabled={updating === r.rechargeId}
                      onClick={() => handleStatusUpdate(r.rechargeId, s)}
                      className="text-xs px-2 py-1 rounded-lg bg-surface-elevated text-slate-200 hover:text-white hover:bg-surface-border transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                    >
                      {updating === r.rechargeId && <Spinner size={10} />}
                      {s}
                    </button>
                  ))}
                </div>
              </td>
            </tr>
          ))}
        </Table>
      </div>

      {/* Mobile Card View */}
      <div className="md:hidden space-y-3">
        {loading ? (
          Array.from({ length: 3 }).map((_, i) => (
            <div key={i} className="p-4 rounded-lg border border-surface-border/50 bg-transparent space-y-2">
              <div className="h-4 bg-surface-border/30 rounded w-1/3" />
              <div className="h-3 bg-surface-border/30 rounded w-2/3" />
            </div>
          ))
        ) : filtered.length === 0 ? (
          <div className="py-8 text-center text-slate-300 text-sm">No recharges found</div>
        ) : (
          filtered.map((r) => (
            <div key={r.rechargeId} className="p-4 rounded-lg border border-surface-border/50 bg-transparent space-y-3">
              {/* Header with Recharge ID and Status */}
              <div className="flex items-start justify-between gap-3">
                <div>
                  <p className="text-slate-300 text-xs mb-1">Recharge ID</p>
                  <p className="text-white font-medium text-sm">#{r.rechargeId}</p>
                </div>
                <StatusBadge status={r.status} />
              </div>

              {/* Amount */}
              <div>
                <p className="text-slate-300 text-xs mb-1">Amount</p>
                <p className="text-white font-bold text-lg">₹{r.amount}</p>
              </div>

              {/* Action Buttons */}
              <div className="flex flex-wrap gap-2 pt-2">
                {STATUS_OPTIONS.filter((s) => s !== r.status).map((s) => (
                  <button
                    key={s}
                    disabled={updating === r.rechargeId}
                    onClick={() => handleStatusUpdate(r.rechargeId, s)}
                    className="text-xs px-3 py-2 rounded-lg bg-surface-elevated text-slate-200 hover:text-white hover:bg-surface-border transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                  >
                    {updating === r.rechargeId && <Spinner size={10} />}
                    {s}
                  </button>
                ))}
              </div>
            </div>
          ))
        )}
      </div>

      {pageData && (
        <Pagination
          page={page}
          totalPages={pageData.totalPages}
          onPageChange={(p) => { setPage(p); }}
        />
      )}
    </div>
  );
};

export default AdminRechargesPage;
