import React, { useEffect, useState } from 'react';
import { transactionApi } from '../../../core/api/services';
import type { TransactionResponse, PageResponse } from '../../../types';
import { PageHeader, StatusBadge, EmptyState, Skeleton, ErrorState, Pagination } from '../../../shared/components';
import { CreditCard } from 'lucide-react';

const MyTransactionsPage: React.FC = () => {
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const pageSize = 10;

  useEffect(() => {
    setLoading(true);
    setError(null);
    let cancelled = false;

    transactionApi.getMyTransactions(page, pageSize)
      .then((r) => {
        if (!cancelled) {
          const pageResponse = r.data as unknown as PageResponse<TransactionResponse>;
          setTransactions(pageResponse.content);
          setTotalPages(pageResponse.totalPages);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          const e = err as { response?: { data?: { message?: string } } };
          setError(e.response?.data?.message || 'Failed to load transactions');
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
      <PageHeader title="My Transactions" subtitle="All payment records" />

      {loading ? (
        <div className="space-y-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="card flex items-center gap-4">
              <Skeleton className="w-10 h-10 rounded-xl" />
              <div className="flex-1 space-y-2">
                <Skeleton className="h-4 w-48" />
                <Skeleton className="h-3 w-32" />
              </div>
              <Skeleton className="h-6 w-20 rounded-lg" />
            </div>
          ))}
        </div>
      ) : error ? (
        <ErrorState message={error} onRetry={() => { setError(null); setLoading(true); }} />
      ) : transactions.length === 0 ? (
        <div className="card">
          <EmptyState title="No transactions yet" description="Completed payment records will appear here" icon="" />
        </div>
      ) : (
        <>
          <div className="space-y-2">
            {transactions.map((t) => (
              <div key={t.id} className="card p-3 sm:p-5 flex items-center gap-3 sm:gap-4">
                <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-lg bg-accent-purple/10 flex items-center justify-center flex-shrink-0">
                  <CreditCard size={16} className="sm:block hidden text-accent-purple" />
                  <CreditCard size={14} className="sm:hidden block text-accent-purple" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-1.5 sm:gap-2 flex-wrap">
                    <p className="font-semibold text-white text-xs sm:text-sm font-mono">{t.razorpayOrderId}</p>
                    <StatusBadge status={t.status} />
                  </div>
                  <div className="flex flex-wrap gap-2 sm:gap-3 text-xs text-slate-400 mt-0.5">
                    <span>Recharge #{t.rechargeId}</span>
                    {t.razorpayPaymentId && <span>Pay: {t.razorpayPaymentId}</span>}
                    <span>{new Date(t.createdAt).toLocaleString()}</span>
                  </div>
                </div>
                <div className="text-right flex-shrink-0">
                  <p className="font-bold text-white text-base sm:text-lg">₹{t.amount}</p>
                </div>
              </div>
            ))}
          </div>
          <Pagination 
            page={page} 
            totalPages={totalPages}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </div>
  );
};

export default MyTransactionsPage;
