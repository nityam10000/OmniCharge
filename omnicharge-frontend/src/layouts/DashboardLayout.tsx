import React from 'react';
import { Outlet } from 'react-router-dom';
import { Navbar, MobileBottomNav } from './Navbar';

const DashboardLayout: React.FC = () => (
  <div className="flex flex-col min-h-screen">
    <Navbar />
    <main className="flex-1 min-h-screen lg:pb-0 pb-24">
      <div className="p-4 lg:p-8 max-w-3xl lg:max-w-6xl mx-auto page-enter">
        <Outlet />
      </div>
    </main>
    <MobileBottomNav />
  </div>
);

export default DashboardLayout;
