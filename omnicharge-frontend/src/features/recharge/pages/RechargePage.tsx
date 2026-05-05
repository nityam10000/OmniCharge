import React, { useEffect, useState, useCallback, memo } from 'react';
import { createPortal } from 'react-dom';
import { Search, Phone, Filter, ArrowLeft, ChevronDown, AlertCircle } from 'lucide-react';
import { plansApi, rechargeApi, transactionApi } from '../../../core/api/services';
import type { OperatorResponse, PlanResponse } from '../../../types';
import { Spinner, EmptyState, ErrorState } from '../../../shared/components';
import { useAppSelector } from '../../../shared/hooks/redux';
import { useAppContext } from '../../../core/context/AppContext';
import { useDebounce } from '../../../shared/hooks/useOptimizations';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';

declare global {
  interface Window { Razorpay: new (options: object) => { open: () => void }; }
}

// Lazy load Razorpay script
const loadRazorpayScript = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    // If already loaded, resolve immediately
    if (window.Razorpay) {
      resolve();
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.async = true;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error('Failed to load Razorpay script'));
    document.body.appendChild(script);
  });
};

// ── Memoised plan card ────────────────────────────────────────────────────────
const PlanCard = memo(({ plan, selected, onClick }: {
  plan: PlanResponse; selected: boolean; onClick: () => void;
}) => (
  <button
    onClick={onClick}
    className={`w-full p-3 md:p-4 rounded-xl md:rounded-2xl border text-left transition-all ${
      selected ? 'bg-brand/10 border-brand' : 'bg-surface-card border-surface-border'
    }`}
  >
    <div className="flex justify-between items-start mb-2 md:mb-3">
      <div className="badge-info text-xs">{plan.validity}</div>
      <span className="text-right">
        <span className="font-bold text-base md:text-lg text-white block">₹{plan.amount}</span>
      </span>
    </div>
    {plan.description && <p className="text-xs md:text-sm text-slate-200 font-medium mb-2 md:mb-3 line-clamp-2">{plan.description}</p>}
    <div className="flex items-center justify-between">
      <span className="text-xs text-slate-300">Select plan</span>
      <ArrowLeft size={12} className="text-brand rotate-180" />
    </div>
  </button>
));

const RechargePage: React.FC = () => {
  const navigate = useNavigate();
  const { user } = useAppSelector((s) => s.auth);
  const { fetchOperators, operatorsLoading, operators, getPlans, setPlanCache, invalidateMyRecharges, invalidateMyTransactions } = useAppContext();

  const [plans, setPlans] = useState<PlanResponse[]>([]);
  const [selectedOp, setSelectedOp] = useState<OperatorResponse | null>(null);
  const [selectedPlan, setSelectedPlan] = useState<PlanResponse | null>(null);
  const [mobile, setMobile] = useState('');
  const [search, setSearch] = useState('');
  const [plansLoading, setPlansLoading] = useState(false);
  const [plansError, setPlansError] = useState<string | null>(null);
  const [paying, setPaying] = useState(false);
  const [sortBy, setSortBy] = useState<'price' | 'validity'>('price');
  const [opDropdownOpen, setOpDropdownOpen] = useState(false);
  const [paymentStartTime, setPaymentStartTime] = useState<number | null>(null);
  const [paymentTimeoutSeconds, setPaymentTimeoutSeconds] = useState<number>(0);

  const debouncedSearch = useDebounce(search, 250);

  useEffect(() => { fetchOperators(); }, [fetchOperators]);

  useEffect(() => {
    if (!opDropdownOpen) return;
    
    const handleClickOutside = (e: MouseEvent) => {
      if (!(e.target as HTMLElement).closest('[data-operator-dropdown]')) {
        setOpDropdownOpen(false);
      }
    };
    
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, [opDropdownOpen]);

  // Payment timeout monitor - mark payment as failed after 5 minutes
  useEffect(() => {
    if (!paying || !paymentStartTime) return;

    const interval = setInterval(() => {
      const elapsedSeconds = Math.floor((Date.now() - paymentStartTime) / 1000);
      const remainingSeconds = Math.max(0, 300 - elapsedSeconds); // 5 minutes = 300 seconds
      
      setPaymentTimeoutSeconds(remainingSeconds);

      // After 5 minutes, mark payment as timeout
      if (elapsedSeconds >= 300) {
        clearInterval(interval);
        setPaying(false);
        setSelectedPlan(null);
        setPaymentStartTime(null);
        toast.error('Payment timeout: Order cancelled after 5 minutes of inactivity');
      }
    }, 1000); // Update every second

    return () => clearInterval(interval);
  }, [paying, paymentStartTime]);

  const loadPlans = useCallback(async (op: OperatorResponse) => {
    setSelectedOp(op);
    setSelectedPlan(null);
    setPlansError(null);

    const cached = getPlans(op.id);
    if (cached) { setPlans(cached); return; }

    setPlansLoading(true);
    try {
      const { data } = await plansApi.getByOperator(op.id);
      setPlanCache(op.id, data);
      setPlans(data);
    } catch {
      setPlansError('Failed to load plans. Please try again.');
    } finally {
      setPlansLoading(false);
    }
  }, [getPlans, setPlanCache]);

  const filteredAndSortedPlans = plans
    .filter((p) =>
      !debouncedSearch ||
      String(p.amount).includes(debouncedSearch) ||
      p.validity.toLowerCase().includes(debouncedSearch.toLowerCase()) ||
      p.description?.toLowerCase().includes(debouncedSearch.toLowerCase())
    )
    .sort((a, b) => {
      if (sortBy === 'price') return a.amount - b.amount;
      return a.validity.localeCompare(b.validity);
    });

  const handlePay = async () => {
    if (!selectedPlan || !mobile || mobile.length !== 10) {
      toast.error('Enter a valid 10-digit mobile number'); return;
    }
    if (!selectedOp) { toast.error('Please select an operator'); return; }
    if (!user) { toast.error('Please log in first'); return; }
    
    setPaying(true);
    // Mark payment start time for 5-minute timeout
    setPaymentStartTime(Date.now());
    setPaymentTimeoutSeconds(300);
    
    try {
      // Load Razorpay script only when payment is initiated
      await loadRazorpayScript();
      const rechargeRes = await rechargeApi.add({
        operatorId: selectedOp.id,
        planId: selectedPlan.id,
        paymentMethod: 'CARD',
      });
      const recharge = rechargeRes.data;

      const orderRes = await transactionApi.createOrder({
        rechargeId: recharge.rechargeId,
        paymentMethod: 'CARD',
      });
      const order = orderRes.data;

      const rzp = new window.Razorpay({
        key: order.keyId,
        amount: order.amount * 100,
        currency: order.currency || 'INR',
        order_id: order.razorpayOrderId,
        name: 'OmniCharge',
        description: `${selectedOp?.name} – ${selectedPlan.validity} – ${mobile}`,
        theme: { color: '#0EA5E9' },
        handler: async (response: {
          razorpay_order_id: string;
          razorpay_payment_id: string;
          razorpay_signature: string;
        }) => {
          try {
            await transactionApi.verifyPayment({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
              rechargeId: recharge.rechargeId,
            });
            toast.success('Recharge successful!');
            setSelectedPlan(null);
            setMobile('');
            setSelectedOp(null);
            setPaymentStartTime(null);
            setPaymentTimeoutSeconds(0);
            // Invalidate cache to trigger dashboard refetch
            invalidateMyRecharges();
            invalidateMyTransactions();
            // Redirect to dashboard after 1.5s
            setTimeout(() => navigate('/dashboard'), 1500);
          } catch {
            toast.error('Payment verification failed');
            setPaymentStartTime(null);
            setPaymentTimeoutSeconds(0);
          }
        },
        modal: { ondismiss: () => {
          toast.error('Payment cancelled');
          setSelectedPlan(null);
          setPaymentStartTime(null);
          setPaymentTimeoutSeconds(0);
        } },
      });
      rzp.open();
    } catch (err: unknown) {
      console.error('Recharge error:', err);
      const e = err as { response?: { data?: { message?: string } } };
      toast.error(e.response?.data?.message || 'Recharge initiation failed');
      // Close modal on error so user can scroll again
      setSelectedPlan(null);
      setPaymentStartTime(null);
      setPaymentTimeoutSeconds(0);
    } finally {
      setPaying(false);
    }
  };

  return (
    <div className="space-y-4 md:space-y-6 animate-fade-in pb-6">
      {/* Dynamic Top Header */}
      <div className="sticky top-16 lg:top-0 z-30 bg-surface-card/95 backdrop-blur-sm border-b border-surface-border -mx-4 md:mx-0 px-4 py-3 md:py-4 rounded-b-xl md:rounded-b-2xl">
        <div className="flex items-center justify-between mb-3 md:mb-4">
          <button onClick={() => navigate(-1)} className="p-1.5 md:p-2 hover:bg-surface-elevated rounded-lg flex-shrink-0">
            <ArrowLeft size={18} className="text-slate-200" />
          </button>
          <div className="text-center flex-1">
            <h1 className="font-display font-bold text-white text-base md:text-lg">Recharge</h1>
            {mobile && <p className="text-xs text-slate-300">{mobile}</p>}
          </div>
          <div className="w-8 md:w-10 flex-shrink-0" />
        </div>

        {/* Mobile Number Input - Desktop View */}
        <div className="relative hidden md:block space-y-1.5">
          <div className="relative">
            <Phone size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-200" />
            <input
              value={mobile}
              onChange={(e) => setMobile(e.target.value.replace(/\D/g, '').slice(0, 10))}
              placeholder="Enter 10-digit mobile"
              inputMode="numeric"
              className={`w-full bg-surface-elevated border text-slate-100 placeholder-slate-300 rounded-2xl px-4 py-2 md:py-2.5 text-sm focus:outline-none focus:ring-1 focus:ring-brand/50 transition-colors pl-10 ${
                mobile.length >= 1 && mobile.length !== 10 
                  ? 'border-accent-red/50 focus:border-accent-red' 
                  : mobile.length === 10 
                  ? 'border-accent-green/50 focus:border-accent-green' 
                  : 'border-surface-border focus:border-brand'
              }`}
            />
          </div>
          {mobile.length >= 1 && mobile.length !== 10 && (
            <p className="text-xs text-accent-red font-medium">Please enter exactly 10 digits</p>
          )}
          {mobile.length === 10 && (
            <p className="text-xs text-accent-green font-medium">✓ Valid number</p>
          )}
        </div>
      </div>

      {/* Mobile View Header - Compact Stacked Layout */}
      <div className="md:hidden -mx-4 px-4 space-y-2">
        {/* Mobile Number Input */}
        <div className="flex gap-2 items-start">
          <div className="relative flex-1 space-y-1.5">
            <div className="relative">
              <Phone size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-200" />
              <input
                value={mobile}
                onChange={(e) => setMobile(e.target.value.replace(/\D/g, '').slice(0, 10))}
                placeholder="Enter 10-digit mobile"
                inputMode="numeric"
                className={`w-full bg-surface-elevated border text-slate-100 placeholder-slate-300 rounded-2xl px-4 py-2 text-sm focus:outline-none focus:ring-1 focus:ring-brand/50 transition-colors pl-10 ${
                  mobile.length >= 1 && mobile.length !== 10 
                    ? 'border-accent-red/50 focus:border-accent-red' 
                    : mobile.length === 10 
                    ? 'border-accent-green/50 focus:border-accent-green' 
                    : 'border-surface-border focus:border-brand'
                }`}
              />
            </div>
            {mobile.length >= 1 && mobile.length !== 10 && (
              <p className="text-xs text-accent-red font-medium">Please enter exactly 10 digits</p>
            )}
            {mobile.length === 10 && (
              <p className="text-xs text-accent-green font-medium">✓ Valid number</p>
            )}
          </div>
          <button
            onClick={() => setMobile('')}
            className="px-3 py-2 rounded-lg bg-brand text-white text-xs font-semibold hover:bg-brand/90 transition-colors flex-shrink-0 mt-0"
          >
            CHANGE
          </button>
        </div>

        {/* Search and Filter */}
        {selectedOp && (
          <div className="flex gap-2">
            <div className="relative flex-1">
              <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-200" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search for a pack or add-ons"
                className="input-field pl-10 py-2 text-xs w-full"
              />
            </div>
            <button
              onClick={() => setSortBy(sortBy === 'price' ? 'validity' : 'price')}
              className="flex items-center justify-center px-3 py-2 rounded-lg bg-surface-elevated hover:bg-surface-border transition-colors flex-shrink-0"
              title={`Sort by ${sortBy === 'price' ? 'validity' : 'price'}`}
            >
              <Filter size={14} className="text-slate-300" />
            </button>
          </div>
        )}
      </div>

      {/* Operators Dropdown Selector - Only Show if Mobile is Valid */}
      {mobile.length === 10 && !operatorsLoading && operators.length > 0 && (
        <div className="space-y-2" data-operator-dropdown>
          <label className="text-xs font-semibold text-slate-200 block">Select Operator</label>
          <div className="relative">
            <button
              onClick={() => setOpDropdownOpen(!opDropdownOpen)}
              className="w-full flex items-center justify-between px-4 py-3 rounded-xl bg-surface-elevated border border-surface-border text-white hover:border-brand transition-colors text-left"
            >
              <span className="font-medium">{selectedOp?.name || 'Choose an operator'}</span>
              <ChevronDown size={18} className={`transition-transform ${opDropdownOpen ? 'rotate-180' : ''}`} />
            </button>

            {opDropdownOpen && (
              <div className="absolute top-full left-0 right-0 mt-2 bg-surface-card border border-surface-border rounded-xl shadow-lg z-50 max-h-60 overflow-y-auto">
                {operators.map((op) => (
                  <button
                    key={op.id}
                    onClick={() => {
                      loadPlans(op);
                      setOpDropdownOpen(false);
                    }}
                    className={`w-full px-4 py-3 text-left hover:bg-surface-elevated transition-colors border-b border-surface-border/50 last:border-b-0 ${
                      selectedOp?.id === op.id ? 'bg-brand/10 text-brand font-semibold' : 'text-slate-200'
                    }`}
                  >
                    {op.name}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {selectedOp ? (
        <>
          {/* Search and Filter - Desktop Only */}
          <div className="hidden md:flex gap-2">
            <div className="relative flex-1">
              <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-200" />
              <input
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search…"
                className="input-field pl-10 py-2 md:py-2.5 text-xs md:text-sm w-full"
              />
            </div>
            <button
              onClick={() => setSortBy(sortBy === 'price' ? 'validity' : 'price')}
              className="flex items-center gap-1.5 md:gap-2 px-2.5 md:px-4 py-2 md:py-2.5 rounded-lg bg-surface-elevated hover:bg-surface-border transition-colors flex-shrink-0"
              title={`Sort by ${sortBy === 'price' ? 'validity' : 'price'}`}
            >
              <Filter size={14} className="text-slate-300" />
              <span className="text-xs text-slate-300 hidden md:inline">{sortBy === 'price' ? 'Price' : 'Validity'}</span>
            </button>
          </div>

          {/* Plans List */}
          {plansLoading ? (
            <div className="space-y-2 md:space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="rounded-xl md:rounded-2xl h-20 md:h-24 shimmer-bg" />
              ))}
            </div>
          ) : plansError ? (
            <ErrorState message={plansError} onRetry={() => selectedOp && loadPlans(selectedOp)} />
          ) : filteredAndSortedPlans.length === 0 ? (
            <div className="rounded-lg md:rounded-xl border border-surface-border/50 bg-transparent p-6 md:p-8 text-center">
              <EmptyState title="No plans found" />
            </div>
          ) : (
            <div className="space-y-2 md:space-y-3">
              {filteredAndSortedPlans.map((plan) => (
                <PlanCard
                  key={plan.id}
                  plan={plan}
                  selected={selectedPlan?.id === plan.id}
                  onClick={() => setSelectedPlan(plan)}
                />
              ))}
            </div>
          )}

          {/* Checkout Modal Overlay - Using Portal to cover entire viewport */}
          {selectedPlan && selectedOp && mobile.length === 10 && createPortal(
            <>
              {/* Backdrop Blur - Covers full viewport */}
              <div 
                className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40 animate-fade-in"
                onClick={() => setSelectedPlan(null)}
              />

              {/* Modal Sheet */}
              <div className="fixed inset-0 z-50 flex items-end md:items-center justify-center md:p-4 animate-slide-up pointer-events-none">
                <div 
                  className="w-full md:w-[90%] md:max-w-lg bg-surface-card rounded-t-2xl md:rounded-2xl border border-surface-border md:shadow-2xl pointer-events-auto flex flex-col md:max-h-[calc(100vh-8rem)]"
                  onClick={(e) => e.stopPropagation()}
                >
                  {/* Header */}
                  <div className="flex items-center justify-between px-4 py-4 md:px-6 md:py-5 border-b border-surface-border flex-shrink-0">
                    <button
                      onClick={() => setSelectedPlan(null)}
                      className="p-2 hover:bg-surface-elevated rounded-lg transition-colors"
                    >
                      <ArrowLeft size={20} className="text-slate-200" />
                    </button>
                    <h3 className="font-display font-bold text-white flex-1 text-center">Confirm Plan</h3>
                    <div className="w-10" />
                  </div>

                  {/* Payment Timeout Warning */}
                  {paying && paymentTimeoutSeconds > 0 && (
                    <div className={`px-4 py-3 md:px-6 md:py-4 border-b transition-colors ${
                      paymentTimeoutSeconds <= 60 
                        ? 'bg-accent-red/10 border-accent-red/30' 
                        : 'bg-yellow-500/10 border-yellow-500/30'
                    }`}>
                      <div className="flex items-center gap-3">
                        <AlertCircle size={18} className={paymentTimeoutSeconds <= 60 ? 'text-accent-red' : 'text-yellow-500'} />
                        <div className="flex-1">
                          <p className={`text-sm font-medium ${paymentTimeoutSeconds <= 60 ? 'text-accent-red' : 'text-yellow-500'}`}>
                            Payment Timeout Warning
                          </p>
                          <p className="text-xs text-slate-300 mt-1">
                            {paymentTimeoutSeconds <= 60 
                              ? `Order will be cancelled in ${paymentTimeoutSeconds}s. Complete payment immediately`
                              : `Complete payment within ${Math.ceil(paymentTimeoutSeconds / 60)} minute(s)`
                            }
                          </p>
                        </div>
                        <span className={`text-lg font-bold ${paymentTimeoutSeconds <= 60 ? 'text-accent-red' : 'text-yellow-500'}`}>
                          {Math.floor(paymentTimeoutSeconds / 60)}:{String(paymentTimeoutSeconds % 60).padStart(2, '0')}
                        </span>
                      </div>
                    </div>
                  )}

                  {/* Content - No scrolling */}
                  <div className="px-4 py-5 md:px-6 md:py-6 space-y-5 flex-1">
                    {/* Plan Details Card */}
                    <div className="rounded-lg md:rounded-xl bg-surface-elevated border border-surface-border/50 p-5 md:p-6">
                      <div className="grid grid-cols-2 gap-5">
                        <div className="text-center">
                          <p className="text-xs text-slate-300 mb-3 font-medium">Operator</p>
                          <p className="text-base md:text-lg font-semibold text-white">{selectedOp.name}</p>
                        </div>
                        <div className="text-center">
                          <p className="text-xs text-slate-300 mb-3 font-medium">Validity</p>
                          <p className="text-base md:text-lg font-semibold text-white">{selectedPlan.validity}</p>
                        </div>
                        <div className="col-span-2">
                          <p className="text-xs text-slate-300 mb-3 font-medium">Amount</p>
                          <p className="text-4xl md:text-5xl font-bold text-brand">₹{selectedPlan.amount}</p>
                        </div>
                        {selectedPlan.description && (
                          <div className="col-span-2">
                            <p className="text-xs text-slate-300 mb-3 font-medium">Plan Details</p>
                            <p className="text-sm md:text-base text-slate-200 leading-relaxed">{selectedPlan.description}</p>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Mobile Number Display */}
                    {mobile && (
                      <div className="rounded-lg bg-surface-elevated px-5 md:px-6 py-4 md:py-4.5 flex items-center justify-between border border-surface-border/30">
                        <span className="text-sm md:text-base text-slate-300 font-medium">Recharge Number</span>
                        <span className="font-mono font-semibold text-white text-base md:text-lg">{mobile}</span>
                      </div>
                    )}
                  </div>

                  {/* Footer */}
                  <div className="border-t border-surface-border px-4 py-5 md:px-6 md:py-6 space-y-3 flex-shrink-0 bg-surface-card">
                    <button
                      onClick={handlePay}
                      disabled={paying || mobile.length !== 10 || !selectedPlan}
                      className="btn-primary w-full flex items-center justify-center gap-2 py-3 md:py-3.5 text-sm md:text-base disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {paying && <Spinner size={16} />}
                      {paying ? 'Processing…' : `Pay ₹${selectedPlan.amount}`}
                    </button>
                    <button
                      onClick={() => setSelectedPlan(null)}
                      className="w-full py-3 md:py-3.5 text-slate-200 hover:text-white hover:bg-surface-elevated rounded-lg transition-colors font-medium text-sm"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              </div>
            </>,
            document.body
          )}
        </>
      ) : (
        <div className="card text-center py-12">
          <p className="text-slate-300">Select an operator to see plans</p>
        </div>
      )}
    </div>
  );
};

export default RechargePage;
