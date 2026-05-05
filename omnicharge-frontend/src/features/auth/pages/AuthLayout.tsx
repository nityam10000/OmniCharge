import React from 'react';
import { Zap } from 'lucide-react';

const AuthLayout: React.FC<{
  title: string;
  subtitle: string;
  children: React.ReactNode;
}> = ({ title, subtitle, children }) => (
  <div className="min-h-screen flex items-center justify-center bg-surface p-4">
    <div className="w-full max-w-sm animate-slide-up">
      {/* Large Icon */}
      <div className="flex justify-center mb-8">
        <div className="w-20 h-20 rounded-2xl bg-brand flex items-center justify-center">
          <Zap size={40} className="text-white" />
        </div>
      </div>

      {/* Content */}
      <div className="text-center mb-8">
        <h1 className="font-display text-3xl font-bold text-white mb-2">{title}</h1>
        <p className="text-slate-400 text-sm">{subtitle}</p>
      </div>

      {/* Form */}
      {children}
    </div>
  </div>
);

export default AuthLayout;
