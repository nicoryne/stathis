'use client';

import { Sidebar } from '@/components/dashboard/sidebar';
import { StatCard } from '@/components/dashboard/stat-card';
import { ActivityCard } from '@/components/dashboard/activity-card';
import { AlertCard } from '@/components/dashboard/alert-card';
import { LineChart } from '@/components/dashboard/line-chart';
import { BarChart } from '@/components/dashboard/bar-chart';
import { Button } from '@/components/ui/button';
import { Activity, Heart, Users, Video, Bell } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import { OverviewCard } from '@/components/dashboard/overview-card';
import ThemeSwitcher from '@/components/theme-switcher';
import { getUserDetails, logout } from '@/services/auth';
import { useEffect, useState } from 'react';

export default function DashboardPage() {
  const [userDetails, setUserDetails] = useState({
    first_name: '',
    last_name: '',
    email: ''
  });

  useEffect(() => {
    const fetchUser = async () => {
      const user = await getUserDetails();

      if (user) {
        setUserDetails({
          first_name: user.first_name,
          last_name: user.last_name,
          email: user.email
        });
      }
    };
    fetchUser();
  }, []);

  const handleLogout = async () => {
    await logout();
  };

  // Mock data for the dashboard
  const recentActivities = [
    {
      id: '1',
      name: 'Morning Stretching',
      time: 'Today, 8:30 AM',
      status: 'completed' as const,
      score: 92
    },
    {
      id: '2',
      name: 'Cardio Workout',
      time: 'Today, 10:15 AM',
      status: 'in-progress' as const
    },
    {
      id: '3',
      name: 'Yoga Session',
      time: 'Today, 2:00 PM',
      status: 'scheduled' as const
    },
    {
      id: '4',
      name: 'Strength Training',
      time: 'Yesterday, 4:30 PM',
      status: 'completed' as const,
      score: 85
    }
  ];

  const safetyAlerts = [
    {
      id: '1',
      student: 'Alex Johnson',
      issue: 'Elevated heart rate during exercise',
      time: '10 minutes ago',
      severity: 'medium' as const
    },
    {
      id: '2',
      student: 'Sarah Williams',
      issue: 'Poor posture during squats',
      time: '15 minutes ago',
      severity: 'low' as const
    }
  ];

  const heartRateData = [
    { day: 'Mon', rate: 72 },
    { day: 'Tue', rate: 75 },
    { day: 'Wed', rate: 82 },
    { day: 'Thu', rate: 78 },
    { day: 'Fri', rate: 76 },
    { day: 'Sat', rate: 80 },
    { day: 'Sun', rate: 74 }
  ];

  const postureScoreData = [
    { exercise: 'Squats', score: 85 },
    { exercise: 'Lunges', score: 92 },
    { exercise: 'Pushups', score: 78 },
    { exercise: 'Planks', score: 95 },
    { exercise: 'Stretches', score: 88 }
  ];

  return (
    <div className="flex min-h-screen">
      <Sidebar className="w-64 flex-shrink-0" />

      <div className="flex-1">
        <header className="bg-background border-b">
          <div className="flex h-16 items-center justify-end gap-4 px-4">
            <Button variant="outline" size="icon">
              <Bell className="h-5 w-5" />
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src="/placeholder.svg" alt="User" />
                    <AvatarFallback>
                      {userDetails.first_name.charAt(0).toUpperCase()}
                      {userDetails.last_name.charAt(0).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end" forceMount>
                <DropdownMenuLabel className="font-normal">
                  <div className="flex flex-col space-y-1">
                    <p className="text-sm leading-none font-medium">
                      {userDetails.first_name} {userDetails.last_name}
                    </p>
                    <p className="text-muted-foreground text-xs leading-none">
                      {userDetails.email}
                    </p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleLogout}>Log out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>

        <main className="p-6">
          <div className="mb-6 flex items-center justify-between">
            <h1 className="text-2xl font-bold">Dashboard</h1>
            <Button>Create New Activity</Button>
          </div>

          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
              title="Active Students"
              value="24"
              description="Students currently active"
              icon={Users}
              trend={{ value: 12, positive: true }}
            />
            <StatCard
              title="Exercise Activities"
              value="8"
              description="Activities scheduled today"
              icon={Video}
              trend={{ value: 5, positive: true }}
            />
            <StatCard
              title="Average Heart Rate"
              value="78 BPM"
              description="Class average during exercise"
              icon={Heart}
              trend={{ value: 3, positive: false }}
            />
            <StatCard
              title="Posture Accuracy"
              value="87%"
              description="Average posture score"
              icon={Activity}
              trend={{ value: 8, positive: true }}
            />
          </div>

          <div className="mt-6 grid gap-6 md:grid-cols-2">
            <OverviewCard
              title="Class Performance Overview"
              description="Current metrics for today's PE session"
              metrics={[
                {
                  label: 'Average Posture Score',
                  value: '87%',
                  progress: 87,
                  status: 'positive'
                },
                {
                  label: 'Exercise Completion Rate',
                  value: '24/30',
                  progress: 80,
                  trend: { value: 5, positive: true }
                },
                {
                  label: 'Safety Compliance',
                  value: '95%',
                  progress: 95,
                  status: 'positive'
                },
                {
                  label: 'Engagement Level',
                  value: '78%',
                  progress: 78,
                  status: 'warning'
                }
              ]}
              className="col-span-2 lg:col-span-1"
            />
            <OverviewCard
              title="Health Metrics"
              description="Real-time vitals monitoring"
              metrics={[
                {
                  label: 'Average Heart Rate',
                  value: '78 BPM',
                  status: 'positive'
                },
                {
                  label: 'Max Heart Rate',
                  value: '142 BPM',
                  status: 'warning'
                },
                {
                  label: 'Recovery Time',
                  value: '1m 45s',
                  trend: { value: 12, positive: true }
                },
                {
                  label: 'Hydration Reminders',
                  value: '3 sent',
                  status: 'neutral'
                }
              ]}
              className="col-span-2 lg:col-span-1"
            />
          </div>

          <div className="mt-6 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            <ActivityCard activities={recentActivities} className="md:col-span-1 lg:col-span-1" />
            <AlertCard alerts={safetyAlerts} className="md:col-span-1 lg:col-span-1" />
            <LineChart
              title="Weekly Heart Rate"
              description="Average BPM during exercise"
              data={heartRateData}
              categories={['rate']}
              index="day"
              className="md:col-span-2 lg:col-span-1"
            />
          </div>

          <div className="mt-6">
            <BarChart
              title="Posture Analysis by Exercise"
              description="Average posture score by exercise type"
              data={postureScoreData}
              categories={['score']}
              index="exercise"
            />
          </div>
        </main>
      </div>
    </div>
  );
}
