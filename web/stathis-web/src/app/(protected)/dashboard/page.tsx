'use client';

import React, { useState, useEffect } from 'react';
import { Sidebar } from '@/components/dashboard/sidebar';
import { StatCard } from '@/components/dashboard/stat-card';
import { ActivityCard } from '@/components/dashboard/activity-card';
import { AlertCard } from '@/components/dashboard/alert-card';
import { LineChart } from '@/components/dashboard/line-chart';
import { BarChart } from '@/components/dashboard/bar-chart';
import { Button } from '@/components/ui/button';
import { Activity, Heart, Users, Video, Bell, Trophy, AlertTriangle } from 'lucide-react';
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
import { getUserDetails, signOut } from '@/services/api-auth-client';
import { getCurrentUserEmail } from '@/lib/utils/jwt';
import { useQuery } from '@tanstack/react-query';
import { getTeacherClassrooms } from '@/services/api-classroom';
import { Task, getTasksByClassroom } from '@/services/api-task-client';
import { 
  getTaskScores, 
  getTaskLeaderboard, 
  analyzeTaskScores,
  Score,
  TaskScoreAnalytics,
  LeaderboardResponseDTO,
  getCompletedTasksCount,
  getAverageQuizScore,
  getAverageExerciseScore
} from '@/services/analytics/api-analytics-client';
// Removed websocket and vital signs imports since we're not using them
import { useTaskScores, useTaskLeaderboard, useActiveTasks } from '@/hooks/analytics';
import { getAnalyticsClient } from '@/services/analytics/analytics-service';

// Define user interface to match API response
interface UserDetails {
  first_name: string;
  last_name: string;
  email: string;
  [key: string]: any; // For any additional properties
}

// Define classroom interface
interface Classroom {
  physicalId: string;
  name: string;
  createdAt?: string;
  updatedAt?: string;
  teacherId?: string;
  description?: string;
}

// Define alert interface to match the AlertCard component expectations
interface Alert {
  id: string;
  student: string;
  issue: string;
  time: string;
  severity: 'low' | 'medium' | 'high';
}

interface SafetyAlert {
  id: string;
  studentId: string;
  studentName: string;
  message: string;
  timestamp: string | Date;
  severity: 'warning' | 'error';
  type: 'heart_rate' | 'oxygen';
}

export default function DashboardPage() {
  const router = useRouter();
  const [selectedClassroom, setSelectedClassroom] = useState('');
  const [selectedTask, setSelectedTask] = useState('');
  
  // Get user email from JWT token or localStorage using the utility function
  const userEmail = getCurrentUserEmail();
  const [userDetails, setUserDetails] = useState<UserDetails>({
    first_name: '',
    last_name: '',
    email: userEmail || ''
  });

  // Fetch user details
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const user = await getUserDetails();
        
        if (user && typeof user === 'object') {
          // Type assertion to allow property access
          const userObj = user as Record<string, any>;
          setUserDetails({
            first_name: userObj.first_name || '',
            last_name: userObj.last_name || '',
            email: userObj.email || '',
            ...userObj // Include any other properties
          });
        }
      } catch (error) {
        console.error('Error fetching user details:', error);
      }
    };
    fetchUser();
  }, []);

  // Fetch classrooms
  const { data: classroomsData } = useQuery<Classroom[]>({
    queryKey: ['teacher-classrooms'],
    queryFn: getTeacherClassrooms
  });
  
  // Set selected classroom when data is available
  React.useEffect(() => {
    if (classroomsData && classroomsData.length > 0 && !selectedClassroom) {
      setSelectedClassroom(classroomsData[0].physicalId);
    }
  }, [classroomsData, selectedClassroom]);

  // Fetch tasks for selected classroom
  const { data: tasksData } = useQuery<Task[]>({
    queryKey: ['classroom-tasks', selectedClassroom],
    queryFn: () => getTasksByClassroom(selectedClassroom),
    enabled: !!selectedClassroom
  });
  
  // Set selected task when data is available
  React.useEffect(() => {
    if (tasksData && tasksData.length > 0 && !selectedTask) {
      setSelectedTask(tasksData[0].physicalId);
    }
  }, [tasksData, selectedTask]);

  // Fetch task scores
  const { data: rawTaskScoresData, isLoading: isTaskScoresLoading } = useQuery<Score[]>({
    queryKey: ['task-scores', selectedTask],
    queryFn: () => getTaskScores(selectedTask),
    enabled: !!selectedTask,
  });

  // Get the selected task name
  const selectedTaskName = tasksData?.find((task: Task) => task.physicalId === selectedTask)?.name || 'Unknown Task';

  // Process raw task scores into analytics
  const taskScoresData: TaskScoreAnalytics | undefined = rawTaskScoresData ? 
    analyzeTaskScores(rawTaskScoresData, selectedTaskName) : undefined;

  // Fetch leaderboard data
  const { data: leaderboardData, isLoading: isLeaderboardLoading } = useQuery<LeaderboardResponseDTO[]>({
    queryKey: ['task-leaderboard', selectedTask],
    queryFn: () => getTaskLeaderboard(selectedTask),
    enabled: !!selectedTask,
  });

  const handlesignOut = async () => {
    await signOut();
  };

  // Define the Activity type to match what ActivityCard expects
  type Activity = {
    id: string;
    name: string;
    time: string;
    status: "completed" | "in-progress" | "scheduled";
  };

  // Activity data from task scores API - formatted to match the ActivityCard component's expected structure
  const recentActivities: Activity[] = taskScoresData && tasksData ? [
    {
      id: (() => {
        // Find the matching task
        const matchingTask = tasksData.find(t => t.physicalId === taskScoresData.taskId);
        return matchingTask?.name || selectedTaskName || 'Current Task';
      })(),
      name: (() => {
        // Log the task ID we're looking for
        console.log('Looking for task with ID:', taskScoresData.taskId);
        // Log all available task IDs for debugging
        console.log('Available task IDs:', tasksData.map(t => ({ id: t.physicalId, name: t.name })));
        // Find the matching task
        const matchingTask = tasksData.find(t => t.physicalId === taskScoresData.taskId);
        return matchingTask?.name || selectedTaskName || 'Current Task';
      })(),
      time: new Date().toLocaleString(),
      status: taskScoresData.completedStudents === taskScoresData.totalStudents ? "completed" as const : "in-progress" as const
    }
  ] : [];

  // Only real data from API - we're not generating weekly performance data
  const exerciseScoreData = (tasksData && taskScoresData) 
    ? tasksData.slice(0, 5).map((task: Task) => ({
        exercise: task.name || 'Unknown',
        score: taskScoresData.averageScore || 0
      })) 
    : [];
      
  // Generate task score metrics from real task data
  const taskScoreMetrics = (tasksData && taskScoresData) ? tasksData.map((task: Task) => ({
    name: task.name,
    status: task.active ? 'in-progress' as const : 
           task.started ? 'completed' as const : 'scheduled' as const,
    score: taskScoresData.averageScore
  })) : [];

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
                      {userDetails.first_name.charAt(0).toUpperCase() || userEmail?.charAt(0).toUpperCase() || 'U'}
                      {userDetails.last_name.charAt(0).toUpperCase() || ''}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56" align="end" forceMount>
                <DropdownMenuLabel className="font-normal">
                  <div className="flex flex-col space-y-1">
                    <p className="text-sm leading-none font-medium">
                      {userDetails.first_name || userEmail || 'User'}
                    </p>
                    <p className="text-muted-foreground text-xs leading-none">
                      {userDetails.email || userEmail || ''}
                    </p>
                  </div>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={() => router.push('/profile')}>Profile</DropdownMenuItem>
                <DropdownMenuItem onClick={() => router.push('/settings')}>Settings</DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handlesignOut}>Sign out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>

        <main className="p-6">
          <div className="mb-6 flex items-center justify-between">
            <h1 className="text-2xl font-bold">Dashboard</h1>
            <div className="flex gap-2">
              <Button onClick={() => router.push('/classroom')}>Manage Classrooms</Button>
              <Button variant="outline" onClick={() => router.push('/monitoring')}>
                <Activity className="mr-2 h-4 w-4" />
                View Monitoring
              </Button>
            </div>
          </div>

          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
              title="Active Students"
              value={taskScoresData?.totalStudents?.toString() || '0'}
              description="Students participating in tasks"
              icon={Users}
              trend={{ value: taskScoresData?.completedStudents || 0, positive: true }}
            />
            <StatCard
              title="Exercise Activities"
              value={(tasksData?.length || 0).toString()}
              description="Total activities available"
              icon={Video}
              trend={{ 
                value: tasksData?.filter((t: Task) => t.started && !t.active)?.length || 0, 
                positive: true 
              }}
            />
            <StatCard
              title="Average Score"
              value={`${taskScoresData?.averageScore || 0}%`}
              description="Class average score"
              icon={Heart}
              trend={{ 
                value: taskScoresData?.averageScore || 0, 
                positive: true
              }}
            />
            <StatCard
              title="Completion Rate"
              value={taskScoresData ? `${taskScoresData.completedStudents}/${taskScoresData.totalStudents}` : '0/0'}
              description="Tasks completed by students"
              icon={Activity}
              trend={{ 
                value: taskScoresData ? Math.round((taskScoresData.completedStudents / taskScoresData.totalStudents) * 100) : 0, 
                positive: true 
              }}
            />
          </div>

          <div className="mt-6 grid gap-6 md:grid-cols-2">
            <OverviewCard
              title="Class Performance Overview"
              description="Current metrics for today's PE session"
              metrics={[
                {
                  label: 'Average Task Score',
                  value: `${taskScoresData?.averageScore || 0}%`,
                  progress: taskScoresData?.averageScore || 0,
                  status: (taskScoresData?.averageScore || 0) > 85 ? 'positive' : 'warning'
                },
                {
                  label: 'Task Completion Rate',
                  value: taskScoresData ? `${taskScoresData.completedStudents}/${taskScoresData.totalStudents}` : '0/0',
                  progress: taskScoresData ? Math.round((taskScoresData.completedStudents / taskScoresData.totalStudents) * 100) : 0,
                  trend: { 
                    value: taskScoresData?.completionRate || 0, 
                    positive: true 
                  }
                },
                {
                  label: 'Leaderboard Entries',
                  value: `${leaderboardData?.length || 0}`,
                  progress: leaderboardData ? Math.min(100, leaderboardData.length * 10) : 0,
                  status: leaderboardData && leaderboardData.length > 5 ? 'positive' : 'warning'
                }
              ]}
              className="md:col-span-1"
            />
            <OverviewCard
              title="Leaderboard Overview"
              description={"Top students by performance"}
              metrics={[
                {
                  label: 'Top Student ID',
                  value: leaderboardData && leaderboardData.length > 0 ? leaderboardData[0].studentId || 'Unknown' : 'No data',
                  status: 'positive'
                },
                {
                  label: 'Top Score',
                  value: leaderboardData && leaderboardData.length > 0 ? `${leaderboardData[0].score}%` : 'No data',
                  status: 'positive'
                },
                {
                  label: 'Participants',
                  value: `${taskScoresData?.totalStudents || 0}`,
                  status: 'positive'
                },
                {
                  label: 'Average Score',
                  value: `${taskScoresData?.averageScore || 0}%`,
                  status: (taskScoresData?.averageScore || 0) > 80 ? 'positive' : 'warning'
                }
              ]}
              className="md:col-span-1"
            />
          </div>

          <div className="mt-6 grid gap-6 md:grid-cols-2">
            <ActivityCard activities={recentActivities} className="md:col-span-1" />
            <LineChart
              title="Task Performance"
              description="Scores by task"
              data={exerciseScoreData.map((item: {exercise: string; score: number}) => ({
                day: item.exercise.substring(0, 3),  // Use first 3 chars of exercise name as day
                score: item.score
              }))}
              categories={['score']}
              index="day"
              className="md:col-span-1"
            />
          </div>

          <div className="mt-6 grid gap-6 md:grid-cols-2">
            <BarChart
              title="Exercise Performance Analysis"
              description="Average score by exercise type"
              data={exerciseScoreData}
              categories={['score']}
              index="exercise"
              className="md:col-span-2"
            />
            {/* No safety alerts displayed since we're not using vital signs data */}
          </div>
        </main>
      </div>
    </div>
  );
}
