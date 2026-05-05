import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { AdminSidebar, MobileSidebarToggle } from './Sidebar';

const AdminLayout: React.FC = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex min-h-screen bg-surface-bg">
      {/* Desktop Sidebar - Hidden on Mobile */}
      <aside className="hidden lg:flex lg:w-64 bg-surface-card border-r border-surface-border flex-col overflow-hidden h-screen sticky top-0">
        <AdminSidebar />
      </aside>

      {/* Mobile Sidebar - Overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="fixed inset-0 bg-black/50" onClick={() => setSidebarOpen(false)} />
          <aside className="fixed left-0 top-0 bottom-0 w-64 bg-surface-card border-r border-surface-border z-50 flex flex-col overflow-y-auto">
            <AdminSidebar mobile onClose={() => setSidebarOpen(false)} />
          </aside>
        </div>
      )}

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-h-screen">
        {/* Mobile Header - Hidden on Desktop */}
        <div className="lg:hidden sticky top-0 z-30 bg-surface-card border-b border-surface-border px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-gradient-brand flex items-center justify-center">
              <span className="text-white font-bold text-sm">⚡</span>
            </div>
            <span className="font-display font-bold text-white text-lg">OmniCharge</span>
          </div>
          <MobileSidebarToggle onClick={() => setSidebarOpen(true)} />
        </div>

        {/* Page Content */}
        <div className="flex-1 overflow-y-auto">
          <div className="p-3 md:p-4 lg:p-8 max-w-7xl mx-auto w-full">
            <Outlet />
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminLayout;
