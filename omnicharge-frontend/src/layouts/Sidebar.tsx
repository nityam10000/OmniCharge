import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Zap, History, CreditCard, User, LogOut, Menu, X, ChevronRight } from 'lucide-react';
import { useAppSelector } from '../shared/hooks/redux';
import { useLogout } from '../shared/hooks/useLogout';

const navItems = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/recharge', icon: Zap, label: 'Recharge' },
  { to: '/my-recharges', icon: History, label: 'My Recharges' },
  { to: '/my-transactions', icon: CreditCard, label: 'Transactions' },
  { to: '/profile', icon: User, label: 'Profile' },
];

const Logo: React.FC = () => (
  <div className="flex items-center gap-3 px-4 py-1">
    <div className="w-9 h-9 rounded-xl bg-gradient-brand flex items-center justify-center">
      <Zap size={18} className="text-white" />
    </div>
    <span className="font-display font-bold text-lg text-white">OmniCharge</span>
  </div>
);

export const Sidebar: React.FC<{ mobile?: boolean; onClose?: () => void }> = ({ mobile, onClose }) => {
  const { user } = useAppSelector((s) => s.auth);
  const { handleLogout } = useLogout();

  return (
    <div className={`flex flex-col h-full ${mobile ? '' : ''}`}>
      <div className="px-4 py-6 flex items-center justify-between">
        <Logo />
        {mobile && (
          <button onClick={onClose} className="text-slate-200 hover:text-white p-1">
            <X size={20} />
          </button>
        )}
      </div>

      <nav className="flex-1 px-3 space-y-1">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onClose}
            className={({ isActive }) =>
              isActive ? 'sidebar-item-active flex' : 'sidebar-item flex'
            }
          >
            <Icon size={18} />
            <span className="flex-1">{label}</span>
            <ChevronRight size={14} className="opacity-30" />
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-surface-border">
        <div className="flex items-center gap-3 px-3 py-3 rounded-xl bg-surface-elevated mb-2">
          <div className="w-8 h-8 rounded-full bg-gradient-brand flex items-center justify-center text-sm font-bold text-white">
            {user?.name?.split(' ')[0]?.[0]?.toUpperCase() || 'U'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-white truncate">{user?.name}</p>
            <p className="text-xs text-slate-300 truncate">{user?.email}</p>
          </div>
        </div>
        <button onClick={handleLogout} className="sidebar-item w-full text-accent-red hover:text-red-400 hover:bg-accent-red/10">
          <LogOut size={18} />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
};

export const MobileSidebarToggle: React.FC<{ onClick: () => void }> = ({ onClick }) => (
  <button onClick={onClick} className="p-2 rounded-xl bg-surface-card border border-surface-border text-slate-200 hover:text-white transition-colors">
    <Menu size={20} />
  </button>
);

// ── Admin Sidebar ─────────────────────────────────────────────────────────────
const adminNavItems = [
  { to: '/admin', icon: LayoutDashboard, label: 'Dashboard Overview', exact: true },
  { to: '/admin/users', icon: User, label: 'Manage Users' },
  { to: '/admin/operators', icon: Zap, label: 'Edit Operators' },
  { to: '/admin/plans', icon: CreditCard, label: 'Edit Plans' },
  { to: '/admin/recharges', icon: History, label: 'View Recharges' },
];

export const AdminSidebar: React.FC<{ mobile?: boolean; onClose?: () => void }> = ({ mobile, onClose }) => {
  const { user } = useAppSelector((s) => s.auth);
  const { handleLogout } = useLogout();

  return (
    <div className="flex flex-col h-full">
      <div className="px-4 py-6 flex items-center justify-between">
        <Logo />
        {mobile && (
          <button onClick={onClose} className="text-slate-200 hover:text-white p-1">
            <X size={20} />
          </button>
        )}
      </div>
      <div className="px-3 mb-4">
        <span className="px-3 text-xs font-semibold text-slate-300 uppercase tracking-widest">Admin Panel</span>
      </div>
      <nav className="flex-1 px-3 space-y-1 overflow-y-auto">
        {adminNavItems.map(({ to, icon: Icon, label, exact }) => (
          <NavLink
            key={to}
            to={to}
            end={exact}
            onClick={onClose}
            className={({ isActive }) => isActive ? 'sidebar-item-active flex' : 'sidebar-item flex'}
          >
            <Icon size={18} />
            <span className="flex-1">{label}</span>
            <ChevronRight size={14} className="opacity-30" />
          </NavLink>
        ))}
      </nav>
      <div className="p-4 border-t border-surface-border flex-shrink-0">
        <div className="flex items-center gap-3 px-3 py-3 rounded-xl bg-surface-elevated mb-2">
          <div className="w-8 h-8 rounded-full bg-amber-500/20 border border-amber-500/30 flex items-center justify-center text-sm font-bold text-amber-400">
            {user?.name?.split(' ')[0]?.[0]?.toUpperCase() || 'A'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-white truncate">{user?.name}</p>
            <span className="text-xs text-amber-400 font-medium">Administrator</span>
          </div>
        </div>
        <button onClick={handleLogout} className="sidebar-item w-full text-accent-red hover:text-red-400 hover:bg-accent-red/10">
          <LogOut size={18} />
          <span>Logout</span>
        </button>
      </div>
    </div>
  );
};

// ── Mobile drawer ─────────────────────────────────────────────────────────────
export const MobileDrawer: React.FC<{
  isOpen: boolean;
  onClose: () => void;
  isAdmin?: boolean;
}> = ({ isOpen, onClose, isAdmin }) => {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 z-50 lg:hidden">
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={onClose} />
      <div className="absolute left-0 top-0 bottom-0 w-72 bg-surface-card border-r border-surface-border animate-slide-in-right">
        {isAdmin ? <AdminSidebar mobile onClose={onClose} /> : <Sidebar mobile onClose={onClose} />}
      </div>
    </div>
  );
};

export const SidebarWrapper: React.FC<{ isAdmin?: boolean }> = ({ isAdmin }) => {
  const [mobileOpen, setMobileOpen] = useState(false);

  return (
    <>
      {/* Desktop sidebar */}
      <aside className="hidden lg:flex flex-col w-64 min-h-screen bg-surface-card border-r border-surface-border fixed top-0 left-0 bottom-0">
        {isAdmin ? <AdminSidebar /> : <Sidebar />}
      </aside>

      {/* Mobile top bar toggle */}
      <div className="lg:hidden fixed top-4 left-4 z-40">
        <MobileSidebarToggle onClick={() => setMobileOpen(true)} />
      </div>

      {/* Mobile drawer */}
      <MobileDrawer isOpen={mobileOpen} onClose={() => setMobileOpen(false)} isAdmin={isAdmin} />
    </>
  );
};
