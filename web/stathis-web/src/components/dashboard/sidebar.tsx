'use client';

import type React from 'react';

import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import {
  Activity,
  BarChart3,
  Heart,
  Home,
  Menu,
  Settings,
  Shield,
  Users,
  Video
} from 'lucide-react';
import { usePathname } from 'next/navigation';
import Link from 'next/link';
import { useState } from 'react';
import { Logo } from '../logo';

interface SidebarProps extends React.HTMLAttributes<HTMLDivElement> {}

export function Sidebar({ className }: SidebarProps) {
  const pathname = usePathname();
  const [open, setOpen] = useState(false);

  const routes = [
    {
      label: 'Dashboard',
      icon: Home,
      href: '/dashboard',
      active: pathname === '/dashboard'
    },
    {
      label: 'Posture Analysis',
      icon: Activity,
      href: '/dashboard/posture',
      active: pathname === '/dashboard/posture'
    },
    {
      label: 'Health Vitals',
      icon: Heart,
      href: '/dashboard/vitals',
      active: pathname === '/dashboard/vitals'
    },
    {
      label: 'Exercise Activities',
      icon: Video,
      href: '/dashboard/exercises',
      active: pathname === '/dashboard/exercises'
    },
    {
      label: 'Safety Alerts',
      icon: Shield,
      href: '/dashboard/alerts',
      active: pathname === '/dashboard/alerts'
    },
    {
      label: 'Analytics',
      icon: BarChart3,
      href: '/dashboard/analytics',
      active: pathname === '/dashboard/analytics'
    },
    {
      label: 'Students',
      icon: Users,
      href: '/dashboard/students',
      active: pathname === '/dashboard/students'
    },
    {
      label: 'Settings',
      icon: Settings,
      href: '/dashboard/settings',
      active: pathname === '/dashboard/settings'
    }
  ];

  return (
    <>
      <Sheet open={open} onOpenChange={setOpen}>
        <SheetTrigger asChild className="md:hidden">
          <Button variant="outline" size="icon" className="ml-2">
            <Menu className="h-5 w-5" />
            <span className="sr-only">Toggle Menu</span>
          </Button>
        </SheetTrigger>
        <SheetContent side="left" className="p-0">
          <MobileSidebar routes={routes} setOpen={setOpen} />
        </SheetContent>
      </Sheet>

      <div className={cn('bg-background hidden border-r md:block', className)}>
        <div className="flex h-full flex-col">
          <div className="flex h-14 items-center border-b px-4">
            <Logo />
          </div>
          <ScrollArea className="flex-1 py-2">
            <nav className="grid gap-1 px-2">
              {routes.map((route) => (
                <Link
                  key={route.href}
                  href={route.href}
                  className={cn(
                    'hover:bg-accent hover:text-accent-foreground flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium',
                    route.active ? 'bg-accent text-accent-foreground' : 'transparent'
                  )}
                >
                  <route.icon className="h-5 w-5" />
                  {route.label}
                </Link>
              ))}
            </nav>
          </ScrollArea>
        </div>
      </div>
    </>
  );
}

interface MobileSidebarProps {
  routes: {
    label: string;
    icon: any;
    href: string;
    active: boolean;
  }[];
  setOpen: (open: boolean) => void;
}

function MobileSidebar({ routes, setOpen }: MobileSidebarProps) {
  return (
    <div className="flex h-full flex-col">
      <div className="flex h-14 items-center border-b px-4">
        <Logo />
      </div>
      <ScrollArea className="flex-1">
        <nav className="grid gap-1 p-2">
          {routes.map((route) => (
            <Link
              key={route.href}
              href={route.href}
              onClick={() => setOpen(false)}
              className={cn(
                'hover:bg-accent hover:text-accent-foreground flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium',
                route.active ? 'bg-accent text-accent-foreground' : 'transparent'
              )}
            >
              <route.icon className="h-5 w-5" />
              {route.label}
            </Link>
          ))}
        </nav>
      </ScrollArea>
    </div>
  );
}
