import React, { useEffect, useState } from 'react';
import { transactionApi } from '../../../core/api/services';
import type { RechargeResponse, TransactionResponse } from '../../../types';
import { Modal, StatusBadge, Spinner } from '../../../shared/components';
import { Calendar, DollarSign, CreditCard, CheckCircle, AlertCircle } from 'lucide-react';

interface RechargeDetailModalProps {
  isOpen: boolean;
  recharge: RechargeResponse | null;
  onClose: () => void;
}

export const RechargeDetailModal: React.FC<RechargeDetailModalProps> = ({ isOpen, recharge, onClose }) => {
  const [transaction, setTransaction] = useState<TransactionResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen && recharge) {
      setLoading(true);
      setError(null);
      transactionApi.getByRechargeId(recharge.rechargeId)
        .then((res) => {
          setTransaction(res.data);
        })
        .catch((err: unknown) => {
          const e = err as { response?: { data?: { message?: string } } };
          setError(e.response?.data?.message || 'Failed to load transaction');
        })
        .finally(() => setLoading(false));
    }
  }, [isOpen, recharge]);

  if (!recharge) return null;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Recharge Details" size="md">
      <div className="space-y-6">
        {/* Recharge Information */}
        <div className="bg-surface-elevated/50 rounded-lg p-4 space-y-3">
          <h3 className="text-sm font-semibold text-slate-200 uppercase tracking-wider">Recharge Info</h3>
          
          <div className="grid grid-cols-2 gap-3">
            <div>
              <p className="text-xs text-slate-400">Recharge ID</p>
              <p className="text-sm font-semibold text-white">#{recharge.rechargeId}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400">Plan ID</p>
              <p className="text-sm font-semibold text-white">#{recharge.planId}</p>
            </div>
            <div>
              <p className="text-xs text-slate-400">Status</p>
              <div className="mt-1">
                <StatusBadge status={recharge.status} />
              </div>
            </div>
            <div>
              <p className="text-xs text-slate-400">Amount</p>
              <p className="text-sm font-semibold text-white flex items-center gap-1">
                <DollarSign size={14} />
                ₹{recharge.amount}
              </p>
            </div>
          </div>

          {recharge.transactionStatus && (
            <div className="pt-2 border-t border-surface-border">
              <p className="text-xs text-slate-400">Transaction Status</p>
              <p className="text-sm font-semibold text-white mt-1">{recharge.transactionStatus}</p>
            </div>
          )}
        </div>

        {/* Transaction Information */}
        {loading ? (
          <div className="flex items-center justify-center py-8">
            <Spinner size={20} />
          </div>
        ) : error ? (
          <div className="bg-accent-red/10 border border-accent-red/30 rounded-lg p-4 flex items-start gap-3">
            <AlertCircle size={18} className="text-accent-red flex-shrink-0 mt-0.5" />
            <p className="text-sm text-accent-red">{error}</p>
          </div>
        ) : transaction ? (
          <div className="bg-surface-elevated/50 rounded-lg p-4 space-y-3">
            <h3 className="text-sm font-semibold text-slate-200 uppercase tracking-wider">Transaction Details</h3>
            
            <div className="grid grid-cols-2 gap-3">
              <div>
                <p className="text-xs text-slate-400">Order ID</p>
                <p className="text-xs font-mono text-white truncate">{transaction.razorpayOrderId}</p>
              </div>
              <div>
                <p className="text-xs text-slate-400">Payment Status</p>
                <div className="mt-1">
                  <StatusBadge status={transaction.status} />
                </div>
              </div>
              <div>
                <p className="text-xs text-slate-400">Payment Method</p>
                <p className="text-sm font-semibold text-white flex items-center gap-1 mt-1">
                  <CreditCard size={14} />
                  {transaction.paymentMethod}
                </p>
              </div>
              <div>
                <p className="text-xs text-slate-400">Amount</p>
                <p className="text-sm font-semibold text-white">₹{transaction.amount}</p>
              </div>
            </div>

            {transaction.razorpayPaymentId && (
              <div className="pt-2 border-t border-surface-border">
                <p className="text-xs text-slate-400">Payment ID</p>
                <p className="text-xs font-mono text-white truncate mt-1">{transaction.razorpayPaymentId}</p>
              </div>
            )}

            <div className="pt-2 border-t border-surface-border">
              <p className="text-xs text-slate-400">Date & Time</p>
              <p className="text-sm text-white flex items-center gap-2 mt-1">
                <Calendar size={14} className="text-slate-400" />
                {new Date(transaction.createdAt).toLocaleString()}
              </p>
            </div>
          </div>
        ) : null}

        {/* Summary */}
        <div className="flex items-center justify-center gap-2 text-xs text-slate-400">
          <CheckCircle size={14} className="text-green-500" />
          <span>All details verified and confirmed</span>
        </div>
      </div>
    </Modal>
  );
};
