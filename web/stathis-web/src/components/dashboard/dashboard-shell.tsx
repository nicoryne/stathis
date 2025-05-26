'use client';

import { cn } from '@/lib/utils';
import { Sidebar } from './sidebar';

interface DashboardShellProps {
  children: React.ReactNode;
  className?: string;
}

export function DashboardShell({ children, className }: DashboardShellProps) {
  return (
    <div className="flex min-h-screen flex-col">
      <div className="flex flex-1">
        <Sidebar className="w-64 flex-shrink-0" />
        <main className={cn('flex-1 overflow-y-auto p-6 md:p-8', className)}>
          {children}
        </main>
      </div>
    </div>
  );
}
