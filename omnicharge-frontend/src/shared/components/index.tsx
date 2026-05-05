import React from 'react';
import { Loader2 } from 'lucide-react';

// ── Loading Spinner ───────────────────────────────────────────────────────────
export const Spinner: React.FC<{ size?: number; className?: string }> = ({ size = 20, className = '' }) => (
  <Loader2 size={size} className={`animate-spin text-brand ${className}`} />
);

export const FullPageLoader: React.FC = () => (
  <div className="fixed inset-0 bg-surface/80 backdrop-blur-sm flex items-center justify-center z-50">
    <div className="flex flex-col items-center gap-3">
      <div className="w-12 h-12 rounded-full border-2 border-brand border-t-transparent animate-spin" />
      <p className="text-slate-300 text-sm">Loading…</p>
    </div>
  </div>
);

// ── Skeleton ──────────────────────────────────────────────────────────────────
export const Skeleton: React.FC<{ className?: string }> = ({ className = '' }) => (
  <div className={`shimmer-bg rounded-xl ${className}`} />
);

export const StatCardSkeleton: React.FC = () => (
  <div className="card">
    <Skeleton className="h-4 w-24 mb-3" />
    <Skeleton className="h-8 w-32 mb-2" />
    <Skeleton className="h-3 w-20" />
  </div>
);

// ── Error State ───────────────────────────────────────────────────────────────
export const ErrorState: React.FC<{ message: string; onRetry?: () => void }> = ({ message, onRetry }) => (
  <div className="flex flex-col items-center justify-center py-16 text-center">
    <div className="w-16 h-16 rounded-full bg-accent-red/10 flex items-center justify-center mb-4">
      <span className="text-2xl">⚠️</span>
    </div>
    <p className="text-slate-300 mb-4">{message}</p>
    {onRetry && (
      <button onClick={onRetry} className="btn-primary text-sm px-4 py-2">
        Try Again
      </button>
    )}
  </div>
);

// ── Empty State ───────────────────────────────────────────────────────────────
export const EmptyState: React.FC<{ title: string; description?: string; icon?: string }> = ({
  title, description, icon = '📭',
}) => (
  <div className="flex flex-col items-center justify-center py-8 md:py-16 text-center">
    <div className="w-12 md:w-16 h-12 md:h-16 rounded-full bg-surface-elevated flex items-center justify-center mb-3 md:mb-4 text-xl md:text-2xl">
      {icon}
    </div>
    <h3 className="text-sm md:text-base text-slate-200 font-semibold mb-1">{title}</h3>
    {description && <p className="text-xs md:text-sm text-slate-300">{description}</p>}
  </div>
);

// ── Page Header ───────────────────────────────────────────────────────────────
export const PageHeader: React.FC<{
  title: string;
  subtitle?: string;
  action?: React.ReactNode;
}> = ({ title, subtitle, action }) => (
  <div className="flex flex-col md:flex-row md:items-start md:justify-between mb-6 md:mb-8 gap-4 md:gap-0">
    <div className="min-w-0">
      <h1 className="font-display text-xl md:text-2xl font-bold text-white truncate">{title}</h1>
      {subtitle && <p className="text-slate-300 mt-1 text-xs md:text-sm">{subtitle}</p>}
    </div>
    {action && <div className="shrink-0">{action}</div>}
  </div>
);

// ── Badge ─────────────────────────────────────────────────────────────────────
export const StatusBadge: React.FC<{ status?: string }> = ({ status }) => {
  if (!status) return <span className="badge bg-surface-elevated text-slate-300">Unknown</span>;
  
  const map: Record<string, string> = {
    SUCCESS: 'badge-success',
    CAPTURED: 'badge-success',
    ACTIVE: 'badge-success',
    PENDING: 'badge-warning',
    CREATED: 'badge-warning',
    FAILED: 'badge-danger',
    REFUNDED: 'badge-info',
    ADMIN: 'badge-warning',
    USER: 'badge-info',
  };
  return <span className={map[status] || 'badge bg-surface-elevated text-slate-200'}>{status}</span>;
};

// ── Modal ─────────────────────────────────────────────────────────────────────
export const Modal: React.FC<{
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg';
}> = ({ isOpen, onClose, title, children, size = 'md' }) => {
  if (!isOpen) return null;
  const widths = { sm: 'max-w-sm', md: 'max-w-md', lg: 'max-w-2xl' };
  return (
    <div className="fixed inset-0 z-50 flex items-end md:items-center justify-center p-4" onClick={onClose}>
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" />
      <div
        className={`relative bg-surface-card border border-surface-border rounded-t-2xl md:rounded-2xl w-full ${widths[size]} animate-slide-up max-h-[90vh] md:max-h-none overflow-y-auto`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="sticky top-0 flex items-center justify-between p-4 md:p-6 border-b border-surface-border bg-surface-card">
          <h2 className="font-display text-base md:text-lg font-bold text-white truncate">{title}</h2>
          <button onClick={onClose} className="text-slate-200 hover:text-white transition-colors p-1 rounded-lg hover:bg-surface-elevated shrink-0 ml-2">
            ✕
          </button>
        </div>
        <div className="p-4 md:p-6">{children}</div>
      </div>
    </div>
  );
};

// ── Confirm Dialog ────────────────────────────────────────────────────────────
export const ConfirmDialog: React.FC<{
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  loading?: boolean;
}> = ({ isOpen, onClose, onConfirm, title, message, loading }) => (
  <Modal isOpen={isOpen} onClose={onClose} title={title} size="sm">
    <p className="text-slate-200 mb-6">{message}</p>
    <div className="flex gap-3 justify-end">
      <button onClick={onClose} className="btn-ghost">Cancel</button>
      <button onClick={onConfirm} disabled={loading} className="btn-danger flex items-center gap-2">
        {loading && <Spinner size={14} />} Confirm
      </button>
    </div>
  </Modal>
);

// ── Table ─────────────────────────────────────────────────────────────────────
export const Table: React.FC<{
  columns: { key: string; label: string; className?: string }[];
  children: React.ReactNode;
  loading?: boolean;
}> = ({ columns, children, loading }) => (
  <div className="overflow-x-auto rounded-xl border border-surface-border">
    <table className="w-full text-xs md:text-sm">
      <thead>
        <tr className="bg-surface-elevated">
          {columns.map((col) => (
            <th key={col.key} className={`px-2 md:px-4 py-2 md:py-3 text-left text-xs font-semibold text-slate-200 uppercase tracking-wider ${col.className || ''}`}>
              {col.label}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {loading ? (
          Array.from({ length: 5 }).map((_, i) => (
            <tr key={i} className="table-row">
              {columns.map((col) => (
                <td key={col.key} className="px-2 md:px-4 py-2 md:py-3">
                  <Skeleton className="h-4 w-full" />
                </td>
              ))}
            </tr>
          ))
        ) : children}
      </tbody>
    </table>
  </div>
);

// ── Pagination ────────────────────────────────────────────────────────────────
export const Pagination: React.FC<{
  page: number;
  totalPages: number;
  onPageChange: (p: number) => void;
}> = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;
  return (
    <div className="flex flex-col sm:flex-row items-center justify-center gap-2 sm:gap-3 mt-4 sm:mt-6">
      <button
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        className="px-2 sm:px-3 py-1.5 rounded-lg bg-surface-elevated text-slate-300 hover:text-white disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-xs sm:text-sm w-full sm:w-auto"
      >
        ‹ Prev
      </button>
      <span className="text-slate-200 text-xs sm:text-sm whitespace-nowrap">Page {page + 1} of {totalPages}</span>
      <button
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="px-2 sm:px-3 py-1.5 rounded-lg bg-surface-elevated text-slate-300 hover:text-white disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-xs sm:text-sm w-full sm:w-auto"
      >
        Next ›
      </button>
    </div>
  );
};

// ── Input with label ──────────────────────────────────────────────────────────
export const FormField: React.FC<{
  label: string;
  error?: string;
  children: React.ReactNode;
}> = ({ label, error, children }) => (
  <div>
    <label className="label">{label}</label>
    {children}
    {error && <p className="mt-1.5 text-xs text-accent-red">{error}</p>}
  </div>
);
