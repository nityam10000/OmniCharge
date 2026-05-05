import React, { useEffect, useState } from 'react';
import { rechargeApi } from '../../../core/api/services';
import type { RechargeResponse, PageResponse } from '../../../types';
import { PageHeader, StatusBadge, EmptyState, Skeleton, ErrorState, Pagination } from '../../../shared/components';
import { Zap, ChevronRight } from 'lucide-react';
import { Link } from 'react-router-dom';
import { RechargeDetailModal } from '../components/RechargeDetailModal';

const MyRechargesPage: React.FC = () => {
  const [recharges, setRecharges] = useState<RechargeResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedRecharge, setSelectedRecharge] = useState<RechargeResponse | null>(null);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const pageSize = 10;

  useEffect(() => {
    setLoading(true);
    setError(null);
    let cancelled = false;

    rechargeApi.getMyRecharges(page, pageSize)
      .then((r) => {
        if (!cancelled) {
          const pageResponse = r.data as unknown as PageResponse<RechargeResponse>;
          // Sort by creation time (newest first)
          const sortedRecharges = [...pageResponse.content].sort((a, b) => 
            new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
          );
          setRecharges(sortedRecharges);
          setTotalPages(pageResponse.totalPages);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          const e = err as { response?: { data?: { message?: string } } };
          setError(e.response?.data?.message || 'Failed to load recharges');
        }
      })
      .finally(() => { if (!cancelled) setLoading(false); });

    return () => { cancelled = true; };
  }, [page]);

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  return (
    <div className="animate-fade-in">
      <PageHeader
        title="My Recharges"
        subtitle="All your recharge history"
        action={
          <Link to="/recharge" className="btn-primary flex items-center gap-2">
            <Zap size={15} /> New Recharge
          </Link>
        }
      />

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="card flex items-center gap-4">
              <Skeleton className="w-10 h-10 rounded-xl" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-48" />
                <Skeleton className="h-3 w-32" />
              </div>
              <Skeleton className="h-6 w-20 rounded-lg" />
              <Skeleton className="h-6 w-14 rounded-lg" />
            </div>
          ))}
        </div>
      ) : error ? (
        <ErrorState message={error} onRetry={() => { setError(null); setLoading(true); }} />
      ) : recharges.length === 0 ? (
        <div className="card">
          <EmptyState title="No recharges yet" description="Your recharge history will appear here" icon="" />
        </div>
      ) : (
        <>
          <div className="space-y-2">
            {recharges.map((r) => (
              <div
                key={r.rechargeId}
                onClick={() => {
                  setSelectedRecharge(r);
                  setDetailModalOpen(true);
                }}
                className="card p-3 sm:p-5 flex items-center gap-3 sm:gap-4 transition-all hover:border-brand/50 cursor-pointer hover:shadow-lg hover:shadow-brand/10"
              >
                <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-lg bg-brand/10 flex items-center justify-center flex-shrink-0">
                  <Zap size={16} className="sm:block hidden text-brand" />
                  <Zap size={14} className="sm:hidden block text-brand" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-1.5 sm:gap-2 flex-wrap">
                    <p className="font-semibold text-white text-xs sm:text-sm">
                      Plan #{r.planId}
                    </p>
                    <StatusBadge status={r.status} />
                  </div>
                  <p className="text-xs text-slate-400 mt-0.5">Recharge #{r.rechargeId}</p>
                </div>
                <div className="text-right flex-shrink-0 flex items-center gap-3">
                  <p className="font-bold text-white text-base sm:text-lg">₹{r.amount}</p>
                  <ChevronRight size={18} className="text-slate-400" />
                </div>
              </div>
            ))}
          </div>
          <Pagination 
            page={page} 
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
          <RechargeDetailModal
            isOpen={detailModalOpen}
            recharge={selectedRecharge}
            onClose={() => setDetailModalOpen(false)}
          />
        </>
      )}
    </div>
  );
};

export default MyRechargesPage;
