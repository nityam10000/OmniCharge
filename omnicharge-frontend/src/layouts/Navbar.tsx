import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Zap, History, CreditCard, User, LogOut, ChevronDown } from 'lucide-react';
import { useAppSelector } from '../shared/hooks/redux';
import { useLogout } from '../shared/hooks/useLogout';

const Logo: React.FC = () => (
  <div className="flex items-center gap-3">
    <div className="w-8 h-8 rounded-lg bg-gradient-brand flex items-center justify-center">
      <Zap size={16} className="text-white" />
    </div>
    <span className="font-display font-bold text-white hidden sm:inline">OmniCharge</span>
  </div>
);

export const Navbar: React.FC<{ isAdmin?: boolean }> = ({ isAdmin }) => {
  const { user } = useAppSelector((s) => s.auth);
  const { handleLogout } = useLogout();
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  return (
    <nav className="sticky top-0 z-40 bg-surface-card border-b border-surface-border">
      <div className="px-4">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Logo />

          {/* Desktop Navigation - Hidden on Mobile */}
          <div className="hidden lg:flex items-center gap-1">
            {!isAdmin ? (
              <>
                <NavLink
                  to="/dashboard"
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <LayoutDashboard size={16} />
                  Dashboard
                </NavLink>
                <NavLink
                  to="/recharge"
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <Zap size={16} />
                  Recharge
                </NavLink>
                <NavLink
                  to="/my-recharges"
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <History size={16} />
                  My Recharges
                </NavLink>
                <NavLink
                  to="/my-transactions"
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <CreditCard size={16} />
                  Transactions
                </NavLink>
              </>
            ) : (
              <>
                <NavLink
                  to="/admin"
                  end
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <LayoutDashboard size={16} />
                  Overview
                </NavLink>
                <NavLink
                  to="/admin/users"
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <User size={16} />
                  Users
                </NavLink>
                <NavLink
                  to="/admin/operators"
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <Zap size={16} />
                  Operators
                </NavLink>
                <NavLink
                  to="/admin/plans"
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <CreditCard size={16} />
                  Plans
                </NavLink>
                <NavLink
                  to="/admin/recharges"
                  className={({ isActive }) =>
                    `flex items-center gap-2 px-3 py-2 rounded-lg transition-colors text-sm font-medium ${
                      isActive
                        ? 'bg-brand/10 text-brand'
                        : 'text-slate-200 hover:text-white hover:bg-surface-elevated'
                    }`
                  }
                >
                  <History size={16} />
                  Recharges
                </NavLink>
              </>
            )}
          </div>

          {/* User Menu Dropdown */}
          <div className="relative">
            <button
              onClick={() => setUserMenuOpen(!userMenuOpen)}
              className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-surface-elevated transition-colors"
            >
              <div className="w-7 h-7 rounded-full bg-gradient-brand flex items-center justify-center text-xs font-bold text-white">
                {user?.name?.split(' ')[0]?.[0]?.toUpperCase() || 'U'}
              </div>
              <span className="text-sm text-white hidden lg:inline truncate max-w-xs">
                {user?.name}
              </span>
              <ChevronDown size={14} className="text-slate-300 hidden sm:inline" />
            </button>

            {userMenuOpen && (
              <div className="absolute right-0 mt-1 w-48 bg-surface-card border border-surface-border rounded-lg z-50">
                <div className="p-3 border-b border-surface-border">
                  <p className="text-sm font-semibold text-white truncate">{user?.name}</p>
                  <p className="text-xs text-slate-300 truncate">{user?.email}</p>
                </div>
                <div className="p-2 space-y-1">
                  <NavLink
                    to="/profile"
                    onClick={() => setUserMenuOpen(false)}
                    className="flex items-center gap-2 px-3 py-2 rounded-lg text-slate-200 hover:text-white hover:bg-surface-elevated transition-colors text-sm"
                  >
                    <User size={15} />
                    Profile
                  </NavLink>
                  <button
                    onClick={() => {
                      handleLogout();
                      setUserMenuOpen(false);
                    }}
                    className="w-full flex items-center gap-2 px-3 py-2 rounded-lg text-accent-red hover:bg-accent-red/10 transition-colors text-sm"
                  >
                    <LogOut size={15} />
                    Logout
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

// Mobile Bottom Navigation
export const MobileBottomNav: React.FC<{ isAdmin?: boolean }> = ({ isAdmin }) => {
  return (
    <div className="fixed bottom-0 left-0 right-0 z-40 bg-surface-card border-t border-surface-border lg:hidden">
      <div className="flex items-center justify-around h-20">
        {!isAdmin ? (
          <>
            <NavLink
              to="/dashboard"
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <LayoutDashboard size={24} />
              <span className="text-xs font-medium">Dashboard</span>
            </NavLink>
            <NavLink
              to="/recharge"
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <Zap size={24} />
              <span className="text-xs font-medium">Recharge</span>
            </NavLink>
            <NavLink
              to="/my-recharges"
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <History size={24} />
              <span className="text-xs font-medium">History</span>
            </NavLink>
            <NavLink
              to="/my-transactions"
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <CreditCard size={24} />
              <span className="text-xs font-medium">Payments</span>
            </NavLink>
          </>
        ) : (
          <>
            <NavLink
              to="/admin"
              end
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <LayoutDashboard size={24} />
              <span className="text-xs font-medium">Overview</span>
            </NavLink>
            <NavLink
              to="/admin/users"
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <User size={24} />
              <span className="text-xs font-medium">Users</span>
            </NavLink>
            <NavLink
              to="/admin/operators"
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <Zap size={24} />
              <span className="text-xs font-medium">Operators</span>
            </NavLink>
            <NavLink
              to="/admin/plans"
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <CreditCard size={24} />
              <span className="text-xs font-medium">Plans</span>
            </NavLink>
            <NavLink
              to="/admin/recharges"
              className={({ isActive }) =>
                `flex flex-col items-center justify-center gap-1 px-3 py-2 flex-1 transition-colors ${
                  isActive
                    ? 'text-brand'
                    : 'text-slate-200 hover:text-white'
                }`
              }
            >
              <History size={24} />
              <span className="text-xs font-medium">Recharges</span>
            </NavLink>
          </>
        )}
      </div>
    </div>
  );
};
