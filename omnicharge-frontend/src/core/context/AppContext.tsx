import React, { createContext, useContext, useState, useCallback, useRef, type ReactNode } from 'react';
import type { OperatorResponse, PlanResponse, RechargeResponse, TransactionResponse } from '../../types';

// ── Cache entry with TTL
interface CacheEntry<T> {
  data: T;
  timestamp: number;
}

const CACHE_TTL = 60_000; // 1 minute

function isExpired<T>(entry: CacheEntry<T>): boolean {
  return Date.now() - entry.timestamp > CACHE_TTL;
}

// ── Context shape
interface AppContextValue {
  // Operators
  operators: OperatorResponse[];
  operatorsLoading: boolean;
  fetchOperators: () => Promise<OperatorResponse[]>;
  invalidateOperators: () => void;

  // Plans cache (keyed by operatorId)
  getPlans: (operatorId: number) => PlanResponse[] | null;
  setPlanCache: (operatorId: number, plans: PlanResponse[]) => void;
  invalidatePlans: (operatorId?: number) => void;

  // User recharges cache
  myRecharges: RechargeResponse[] | null;
  setMyRecharges: (r: RechargeResponse[]) => void;
  invalidateMyRecharges: () => void;

  // User transactions cache
  myTransactions: TransactionResponse[] | null;
  setMyTransactions: (t: TransactionResponse[]) => void;
  invalidateMyTransactions: () => void;
}

const AppContext = createContext<AppContextValue | null>(null);

export const useAppContext = (): AppContextValue => {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useAppContext must be used inside AppContextProvider');
  return ctx;
};

// ── Provider ──────────────────────────────────────────────────────────────────
export const AppContextProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [operators, setOperators] = useState<OperatorResponse[]>([]);
  const [operatorsLoading, setOperatorsLoading] = useState(false);

  const operatorsCacheRef = useRef<CacheEntry<OperatorResponse[]> | null>(null);
  const plansCacheRef = useRef<Map<number, CacheEntry<PlanResponse[]>>>(new Map());
  const rechargesCacheRef = useRef<CacheEntry<RechargeResponse[]> | null>(null);
  const txCacheRef = useRef<CacheEntry<TransactionResponse[]> | null>(null);

  const [myRecharges, setMyRechargesState] = useState<RechargeResponse[] | null>(null);
  const [myTransactions, setMyTransactionsState] = useState<TransactionResponse[] | null>(null);

  const fetchOperators = useCallback(async (): Promise<OperatorResponse[]> => {
    // Return from cache if fresh
    if (operatorsCacheRef.current && !isExpired(operatorsCacheRef.current)) {
      return operatorsCacheRef.current.data;
    }
    setOperatorsLoading(true);
    try {
      const { operatorsApi } = await import('../api/services');
      const { data } = await operatorsApi.getAll();
      operatorsCacheRef.current = { data, timestamp: Date.now() };
      setOperators(data);
      return data;
    } finally {
      setOperatorsLoading(false);
    }
  }, []);

  const getPlans = useCallback((operatorId: number): PlanResponse[] | null => {
    const entry = plansCacheRef.current.get(operatorId);
    if (!entry || isExpired(entry)) return null;
    return entry.data;
  }, []);

  const setPlanCache = useCallback((operatorId: number, plans: PlanResponse[]) => {
    plansCacheRef.current.set(operatorId, { data: plans, timestamp: Date.now() });
  }, []);

  const invalidatePlans = useCallback((operatorId?: number) => {
    if (operatorId !== undefined) {
      plansCacheRef.current.delete(operatorId);
    } else {
      plansCacheRef.current.clear();
    }
  }, []);

  const invalidateOperators = useCallback(() => {
    operatorsCacheRef.current = null;
  }, []);

  const setMyRecharges = useCallback((r: RechargeResponse[]) => {
    rechargesCacheRef.current = { data: r, timestamp: Date.now() };
    setMyRechargesState(r);
  }, []);

  const invalidateMyRecharges = useCallback(() => {
    rechargesCacheRef.current = null;
    setMyRechargesState(null);
  }, []);

  const setMyTransactions = useCallback((t: TransactionResponse[]) => {
    txCacheRef.current = { data: t, timestamp: Date.now() };
    setMyTransactionsState(t);
  }, []);

  const invalidateMyTransactions = useCallback(() => {
    txCacheRef.current = null;
    setMyTransactionsState(null);
  }, []);

  return (
    <AppContext.Provider
      value={{
        operators,
        operatorsLoading,
        fetchOperators,
        invalidateOperators,
        getPlans,
        setPlanCache,
        invalidatePlans,
        myRecharges,
        setMyRecharges,
        invalidateMyRecharges,
        myTransactions,
        setMyTransactions,
        invalidateMyTransactions,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};
