'use client';

import type React from 'react';

import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet';
import { 
  Activity, 
  BarChart3, 
  BookOpen, 
  GraduationCap, 
  Heart, 
  Home, 
  Menu, 
  School, 
  Settings, 
  Shield, 
  Users, 
  Video, 
  Award, 
  UserCircle
} from 'lucide-react';
import Image from 'next/image';
import { usePathname } from 'next/navigation';
import Link from 'next/link';
import { useState } from 'react';
import { Logo } from '../logo';


interface SidebarProps extends React.HTMLAttributes<HTMLDivElement> {}

export function Sidebar({ className }: SidebarProps) {
  const pathname = usePathname();
  const [open, setOpen] = useState(false);

  type RouteItem = {
    label: string;
    sublabel?: string;
    icon: any;
    href: string;
    active: boolean;
  };

  const sections: { title: string; items: RouteItem[] }[] = [
    {
      title: 'Overview',
      items: [
        {
          label: 'Dashboard',
          sublabel: 'Overview and analytics',
          icon: Home,
          href: '/dashboard',
          active: pathname === '/dashboard'
        }
      ]
    },
    {
      title: 'Management',
      items: [
        {
          label: 'Classrooms',
          sublabel: 'Manage your classrooms',
          icon: School,
          href: '/classroom',
          active: pathname.startsWith('/classroom')
        },
        {
          label: 'Student Progress',
          sublabel: 'Track student performance',
          icon: Award,
          href: '/student-progress',
          active: pathname.startsWith('/student-progress')
        }
      ]
    },
    {
      title: 'Monitoring',
      items: [
        {
          label: 'Live Monitoring',
          sublabel: 'Real-time vitals and activity',
          icon: Activity,
          href: '/monitoring',
          active: pathname === '/monitoring'
        }
      ]
    },
    {
      title: 'Account',
      items: [
        {
          label: 'Profile',
          sublabel: 'Manage your profile',
          icon: UserCircle,
          href: '/profile',
          active: pathname === '/profile'
        }
      ]
    }
  ];

  const actives = {
    dashboard: pathname === '/dashboard',
    classroom: pathname.startsWith('/classroom'),
    studentProgress: pathname.startsWith('/student-progress'),
    monitoring: pathname === '/monitoring',
    profile: pathname === '/profile',
  } as const;

  return (
    <>
      <Sheet open={open} onOpenChange={setOpen}>
        <SheetTrigger asChild className="md:hidden">
          <Button variant="outline" size="icon" className="ml-2 rounded-xl bg-background/50 border-border/50">
            <Menu className="h-5 w-5" />
            <span className="sr-only">Toggle Menu</span>
          </Button>
        </SheetTrigger>
        <SheetContent side="left" className="p-0 bg-card/80 backdrop-blur-xl border-border/50">
          <MobileSidebar actives={actives} setOpen={setOpen} />
        </SheetContent>
      </Sheet>

      <div className={cn('bg-card/80 backdrop-blur-xl hidden border-r border-border/50 md:block', className)}>
        <div className="flex h-full flex-col">
          {/* Logo Section */}
          <div className="flex h-16 items-center border-b border-border/50 px-6">
            <div className="flex items-center gap-3">
              <div className="relative">
                <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg" />
                <Image
                  src="/images/logos/stathis.webp"
                  alt="Stathis Logo"
                  width={32}
                  height={32}
                  className="relative"
                />
              </div>
              <span className="text-xl font-bold tracking-tight">Stathis</span>
            </div>
          </div>
          
          {/* Navigation */}
          <ScrollArea className="flex-1 py-4">
            <div className="space-y-6">
              {sections.map((section) => (
                <div key={section.title} className="px-4">
                  <div className="text-[10px] uppercase tracking-wider text-muted-foreground/80 mb-2">
                    {section.title}
                  </div>
                  <nav className="grid gap-2">
                    {section.items.map((route) => (
                      <div key={route.href}>
                        <Link
                          href={route.href}
                          className={cn(
                            'hover:bg-primary/10 hover:text-primary group block rounded-xl border transition-all duration-200',
                            route.active 
                              ? 'bg-primary/10 text-primary border-primary/20 shadow-sm' 
                              : 'text-muted-foreground border-transparent hover:text-foreground'
                          )}
                        >
                          <div className="flex items-center gap-3 px-4 py-3">
                            <route.icon className="h-5 w-5" />
                            <div className="flex flex-col">
                              <span className="text-sm font-medium">{route.label}</span>
                              {route.sublabel && (
                                <span className="text-xs text-muted-foreground">{route.sublabel}</span>
                              )}
                            </div>
                          </div>
                        </Link>
                      </div>
                    ))}
                  </nav>
                </div>
              ))}
            </div>
          </ScrollArea>
        </div>
      </div>
    </>
  );
}

interface MobileSidebarProps {
  actives: {
    dashboard: boolean;
    classroom: boolean;
    studentProgress: boolean;
    monitoring: boolean;
    profile: boolean;
  };
  setOpen: (open: boolean) => void;
}

function MobileSidebar({ actives, setOpen }: MobileSidebarProps) {
  return (
    <div className="flex h-full flex-col">
      <div className="flex h-16 items-center border-b border-border/50 px-6">
        <div className="flex items-center gap-3">
          <div className="relative">
            <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg" />
            <Image
              src="/images/logos/stathis.webp"
              alt="Stathis Logo"
              width={32}
              height={32}
              className="relative"
            />
          </div>
          <span className="text-xl font-bold tracking-tight">Stathis</span>
        </div>
      </div>
      <ScrollArea className="flex-1">
        <div className="space-y-6 p-4">
          {/* Mirror desktop grouping on mobile for consistency */}
          <div>
            <div className="text-[10px] uppercase tracking-wider text-muted-foreground/80 mb-2">Overview</div>
            <nav className="grid gap-2">
              <Link href="/dashboard" onClick={() => setOpen(false)} className={cn('hover:bg-primary/10 hover:text-primary block rounded-xl border px-4 py-3 text-sm transition-all duration-200', actives.dashboard ? 'bg-primary/10 text-primary border-primary/20 shadow-sm' : 'text-muted-foreground border-transparent hover:text-foreground')}>
                <div className="flex items-center gap-3">
                  <Home className="h-5 w-5" />
                  <div className="flex flex-col"><span className="font-medium">Dashboard</span><span className="text-xs text-muted-foreground">Overview and analytics</span></div>
                </div>
              </Link>
            </nav>
          </div>
          <div>
            <div className="text-[10px] uppercase tracking-wider text-muted-foreground/80 mb-2">Management</div>
            <nav className="grid gap-2">
              <Link href="/classroom" onClick={() => setOpen(false)} className={cn('hover:bg-primary/10 hover:text-primary block rounded-xl border px-4 py-3 text-sm transition-all duration-200', actives.classroom ? 'bg-primary/10 text-primary border-primary/20 shadow-sm' : 'text-muted-foreground border-transparent hover:text-foreground')}>
                <div className="flex items-center gap-3">
                  <School className="h-5 w-5" />
                  <div className="flex flex-col"><span className="font-medium">Classrooms</span><span className="text-xs text-muted-foreground">Manage your classrooms</span></div>
                </div>
              </Link>
              <Link href="/student-progress" onClick={() => setOpen(false)} className={cn('hover:bg-primary/10 hover:text-primary block rounded-xl border px-4 py-3 text-sm transition-all duration-200', actives.studentProgress ? 'bg-primary/10 text-primary border-primary/20 shadow-sm' : 'text-muted-foreground border-transparent hover:text-foreground')}>
                <div className="flex items-center gap-3">
                  <Award className="h-5 w-5" />
                  <div className="flex flex-col"><span className="font-medium">Student Progress</span><span className="text-xs text-muted-foreground">Track student performance</span></div>
                </div>
              </Link>
            </nav>
          </div>
          <div>
            <div className="text-[10px] uppercase tracking-wider text-muted-foreground/80 mb-2">Monitoring</div>
            <nav className="grid gap-2">
              <Link href="/monitoring" onClick={() => setOpen(false)} className={cn('hover:bg-primary/10 hover:text-primary block rounded-xl border px-4 py-3 text-sm transition-all duration-200', actives.monitoring ? 'bg-primary/10 text-primary border-primary/20 shadow-sm' : 'text-muted-foreground border-transparent hover:text-foreground')}>
                <div className="flex items-center gap-3">
                  <Activity className="h-5 w-5" />
                  <div className="flex flex-col"><span className="font-medium">Live Monitoring</span><span className="text-xs text-muted-foreground">Real-time vitals and activity</span></div>
                </div>
              </Link>
            </nav>
          </div>
          <div>
            <div className="text-[10px] uppercase tracking-wider text-muted-foreground/80 mb-2">Account</div>
            <nav className="grid gap-2">
              <Link href="/profile" onClick={() => setOpen(false)} className={cn('hover:bg-primary/10 hover:text-primary block rounded-xl border px-4 py-3 text-sm transition-all duration-200', actives.profile ? 'bg-primary/10 text-primary border-primary/20 shadow-sm' : 'text-muted-foreground border-transparent hover:text-foreground')}>
                <div className="flex items-center gap-3">
                  <UserCircle className="h-5 w-5" />
                  <div className="flex flex-col"><span className="font-medium">Profile</span><span className="text-xs text-muted-foreground">Manage your profile</span></div>
                </div>
              </Link>
            </nav>
          </div>
        </div>
      </ScrollArea>
    </div>
  );
}
