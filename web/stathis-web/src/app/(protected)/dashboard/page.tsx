'use client';

import React, { useState, useEffect } from 'react';
import { Sidebar } from '@/components/dashboard/sidebar';
import { StatCard } from '@/components/dashboard/stat-card';
import { ActivityCard } from '@/components/dashboard/activity-card';
// Removed charts per request
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
import { Activity, Heart, Users, Video, Bell, HeartPulse } from 'lucide-react';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { OverviewCard } from '@/components/dashboard/overview-card';
import ThemeSwitcher from '@/components/theme-switcher';
import { signOut } from '@/services/api-auth-client';
import { getCurrentUserEmail } from '@/lib/utils/jwt';
import { useQuery } from '@tanstack/react-query';
import { getTeacherClassrooms, ClassroomResponseDTO } from '@/services/api-classroom-client';
import { Task, getTasksByClassroom } from '@/services/api-task-client';
import { 
  getTaskScores, 
  analyzeTaskScores,
  Score,
  TaskScoreAnalytics
} from '@/services/analytics/api-analytics-client';
// Removed unused analytics hooks and client imports
import { motion, useReducedMotion } from 'framer-motion';
import Image from 'next/image';

interface UserDetails {
  first_name: string;
  last_name: string;
  email: string;
  profilePictureUrl?: string;
  [key: string]: any; // For any additional properties
}

interface Classroom {
  physicalId: string;
  name: string;
  createdAt?: string;
  updatedAt?: string;
  teacherId?: string;
  description?: string;
}

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
  const userEmail = getCurrentUserEmail();
  const prefersReducedMotion = useReducedMotion();
  
  const [selectedClassroomId, setSelectedClassroomId] = useState<string | null>(null);
  const [selectedTask, setSelectedTask] = useState('');
  
  // Fetch teacher profile with all details including profile picture
  const { data: teacherProfile } = useQuery({
    queryKey: ['teacher-profile'],
    queryFn: async () => {
      const response = await fetch('https://api-stathis.ryne.dev/api/users/profile/teacher', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
          'Content-Type': 'application/json'
        }
      });
      if (!response.ok) throw new Error('Failed to fetch teacher profile');
      return response.json();
    },
    enabled: !!userEmail,
    staleTime: 1000 * 60 * 10 // 10 minutes
  });

  // Map teacher profile to user details format
  const userDetails: UserDetails = {
    first_name: teacherProfile?.firstName || '',
    last_name: teacherProfile?.lastName || '',
    email: teacherProfile?.email || userEmail || '',
    profilePictureUrl: teacherProfile?.profilePictureUrl
  };

  const { data: classrooms, isLoading: isLoadingClassrooms } = useQuery<ClassroomResponseDTO[]>({
    queryKey: ['teacher-classrooms'],
    queryFn: () => getTeacherClassrooms(),
    enabled: !!userEmail,
    staleTime: 1000 * 60 * 5 // 5 minutes
  });

  // Fetch students in the selected classroom to get student names
  const { data: classroomStudentsData } = useQuery({
    queryKey: ['classroom-students', selectedClassroomId],
    queryFn: async () => {
      if (!selectedClassroomId) return null;
      const { getClassroomStudents } = await import('@/services/api-classroom-client');
      return getClassroomStudents(selectedClassroomId);
    },
    enabled: !!selectedClassroomId,
    staleTime: 1000 * 60 * 5 // 5 minutes
  });
  
  useEffect(() => {
    if (classrooms && classrooms.length > 0 && !selectedClassroomId) {
      setSelectedClassroomId(classrooms[0].physicalId);
    }
  }, [classrooms, selectedClassroomId]);

  const { data: tasksData, isLoading: isLoadingTasks } = useQuery<Task[]>({
    queryKey: ['tasks', selectedClassroomId],
    queryFn: async () => {
      if (!selectedClassroomId) return [];
      return await getTasksByClassroom(selectedClassroomId);
    },
    enabled: !!userEmail && !!selectedClassroomId,
    staleTime: 1000 * 60 * 5,
  });

  React.useEffect(() => {
    if (tasksData && tasksData.length > 0 && !selectedTask) {
      setSelectedTask(tasksData[0].physicalId);
    }
  }, [tasksData, selectedTask]);

  // Fetch scores for all tasks in the classroom
  const { data: allTasksScores, isLoading: isTaskScoresLoading } = useQuery({
    queryKey: ['all-tasks-scores', selectedClassroomId, tasksData?.length],
    queryFn: async () => {
      if (!tasksData || tasksData.length === 0) return {};
      
      const scoresPromises = tasksData.map(async (task: Task) => {
        try {
          const scores = await getTaskScores(task.physicalId);
          const analytics = analyzeTaskScores(scores, task.name);
          return { taskId: task.physicalId, scores, analytics };
        } catch (error) {
          console.error(`Error fetching scores for task ${task.physicalId}:`, error);
          return { taskId: task.physicalId, scores: [], analytics: null };
        }
      });
      
      const results = await Promise.all(scoresPromises);
      const scoresMap: Record<string, { scores: Score[], analytics: TaskScoreAnalytics | null }> = {};
      results.forEach(result => {
        scoresMap[result.taskId] = { scores: result.scores, analytics: result.analytics };
      });
      
      return scoresMap;
    },
    enabled: !!selectedClassroomId && !!tasksData && tasksData.length > 0,
    staleTime: 1000 * 60 * 2 // 2 minutes
  });

  // Removed per request: detailed task scores and leaderboard data fetching

  const handlesignOut = async () => {
    await signOut();
  };

  type Activity = {
    id: string;
    name: string;
    time: string;
    status: "completed" | "ongoing" | "not-started";
    score?: number;
  };

  const recentActivities: Activity[] = tasksData ? tasksData
    .sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()) // Sort by most recent
    .slice(0, 5) // Get top 5 most recent tasks
    .map(task => {
      // Determine status based on task state (teacher-managed)
      // Completed = deactivated by teacher, Ongoing = active, Not Started = not started yet
      const status: "completed" | "ongoing" | "not-started" = 
        !task.started ? "not-started" : 
        (task.started && task.active) ? "ongoing" : 
        "completed";
      
      return {
        id: task.physicalId,
        name: task.name,
        time: new Date(task.updatedAt || task.createdAt).toLocaleString(),
        status
      };
    }) : [];

  // Removed per request: chart data and helpers for task performance sections

  // Calculate aggregated statistics from all tasks
  const totalActiveStudents = allTasksScores ? (() => {
    const uniqueStudents = new Set<string>();
    Object.values(allTasksScores).forEach(taskData => {
      taskData.scores.forEach((score: Score) => uniqueStudents.add(score.studentId));
    });
    return uniqueStudents.size;
  })() : 0;

  const studentCompletionRate = allTasksScores ? (() => {
    let totalStudents = 0;
    let completedStudents = 0;
    Object.values(allTasksScores).forEach(taskData => {
      if (taskData.analytics) {
        totalStudents = Math.max(totalStudents, taskData.analytics.totalStudents);
        completedStudents = Math.max(completedStudents, taskData.analytics.completedStudents);
      }
    });
    return totalStudents > 0 ? Math.round((completedStudents / totalStudents) * 100) : 0;
  })() : 0;

  const overallAverageScore = allTasksScores ? (() => {
    const validAnalytics = Object.values(allTasksScores)
      .map(taskData => taskData.analytics)
      .filter(a => a && a.averageScore > 0);
    if (validAnalytics.length === 0) return 0;
    const sum = validAnalytics.reduce((acc, a) => acc + (a?.averageScore || 0), 0);
    return Math.round(sum / validAnalytics.length);
  })() : 0;

  // Calculate task completion (deactivated tasks)
  const taskCompletionStats = tasksData ? (() => {
    const completedTasks = tasksData.filter(t => t.started && !t.active).length;
    const totalTasks = tasksData.length;
    const percentage = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
    
    return {
      completed: completedTasks,
      total: totalTasks,
      percentage
    };
  })() : { completed: 0, total: 0, percentage: 0 };

  // Calculate top student based on overall performance across all tasks
  const topStudent = allTasksScores ? (() => {
    const studentPerformance: Record<string, { totalScore: number, count: number }> = {};
    
    // Aggregate scores for each student across all tasks
    Object.values(allTasksScores).forEach(taskData => {
      taskData.scores.forEach((score: Score) => {
        if (score.completed) {
          if (!studentPerformance[score.studentId]) {
            studentPerformance[score.studentId] = { totalScore: 0, count: 0 };
          }
          studentPerformance[score.studentId].totalScore += score.score;
          studentPerformance[score.studentId].count += 1;
        }
      });
    });
    
    // Find student with highest average score
    let topStudentId = '';
    let highestAverage = 0;
    
    Object.entries(studentPerformance).forEach(([studentId, data]) => {
      const average = data.totalScore / data.count;
      if (average > highestAverage) {
        highestAverage = average;
        topStudentId = studentId;
      }
    });
    
    // Get student name from classroom students data
    let topStudentName = 'No data';
    if (topStudentId && classroomStudentsData?.students) {
      const student = classroomStudentsData.students.find(s => s.physicalId === topStudentId);
      if (student) {
        topStudentName = `${student.firstName} ${student.lastName}`;
      }
    }
    
    return { 
      studentId: topStudentId, 
      studentName: topStudentName,
      averageScore: Math.round(highestAverage) 
    };
  })() : { studentId: '', studentName: 'No data', averageScore: 0 };

  return (
    <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-background to-muted/20">
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <motion.div className="absolute left-6 top-6 h-32 w-32 rounded-full bg-primary/5" animate={prefersReducedMotion ? undefined : { scale: [1, 1.05, 1] }} transition={{ duration: 6, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute right-8 top-10 h-24 w-24 rounded-full bg-secondary/5" animate={prefersReducedMotion ? undefined : { y: [0, -10, 0] }} transition={{ duration: 5, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute bottom-8 left-8 h-40 w-40 rounded-full bg-primary/5" animate={prefersReducedMotion ? undefined : { scale: [1, 1.08, 1] }} transition={{ duration: 7, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute bottom-10 right-12 h-28 w-28 rounded-full bg-secondary/5" animate={prefersReducedMotion ? undefined : { y: [0, -12, 0] }} transition={{ duration: 6, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute top-1/2 left-1/4 h-16 w-16 rounded-full bg-primary/3" animate={prefersReducedMotion ? undefined : { scale: [1, 1.2, 1] }} transition={{ duration: 8, repeat: Number.POSITIVE_INFINITY }} />
        <motion.div className="absolute top-1/3 right-1/3 h-20 w-20 rounded-full bg-secondary/3" animate={prefersReducedMotion ? undefined : { y: [0, -15, 0] }} transition={{ duration: 9, repeat: Number.POSITIVE_INFINITY }} />
      </div>

      <div className="flex min-h-screen relative z-10">
        <Sidebar className="w-64 flex-shrink-0" />

      <div className="flex-1 md:ml-64">
        <header className="bg-background/80 backdrop-blur-xl border-b border-border/50 sticky top-0 z-30">
          <div className="flex h-16 items-center justify-end gap-4 px-4">
            
            <Button variant="outline" size="icon" aria-label="Notifications" className="bg-card/80 backdrop-blur-xl border-border/50 hover:bg-card/90">
              <Bell className="h-5 w-5" />
            </Button>
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" aria-label="Open user menu" className="relative h-8 w-8 rounded-full">
                  <Avatar className="h-8 w-8">
                    <AvatarImage src={userDetails.profilePictureUrl || undefined} alt={`${userDetails.first_name} ${userDetails.last_name}`} />
                    <AvatarFallback>
                      {userDetails.first_name.charAt(0).toUpperCase() || userEmail?.charAt(0).toUpperCase() || 'U'}
                      {userDetails.last_name.charAt(0).toUpperCase() || ''}
                    </AvatarFallback>
                  </Avatar>
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent className="w-56 bg-card/80 backdrop-blur-xl border-border/50" align="end" forceMount>
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
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handlesignOut}>Sign out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>

          <main className="p-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              className="mb-8 flex items-center justify-between"
            >
              <div className="flex items-center gap-6">
                <div className="relative">
                  <div className="absolute -inset-4 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-2xl" />
                  <motion.div
                    animate={prefersReducedMotion ? undefined : { y: [0, -8, 0] }}
                    transition={{ duration: 2, repeat: Number.POSITIVE_INFINITY }}
                    className="relative"
                  >
                  <Image
                    src="/images/mascots/mascot_cheer.png"
                    alt="Stathis Cheer Mascot"
                    width={80}
                    height={80}
                    className="drop-shadow-lg"
                  />
                  </motion.div>
                </div>
                
                <div>
                  <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                    Welcome back, {userDetails.first_name || 'Teacher'}!
                  </h1>
                  <p className="text-muted-foreground mt-2">Monitor your students' progress and manage your classrooms</p>
                </div>
              </div>
              
              <div className="flex gap-3">
                <Button 
                  onClick={() => router.push('/classroom')}
                  className="rounded-xl gradient-hero shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105 text-white"
                >
                  <HeartPulse className="mr-2 h-4 w-4" />
                  Manage Classrooms
                </Button>
                <Button 
                  variant="outline" 
                  onClick={() => router.push('/monitoring')}
                  className="rounded-xl bg-card/90 backdrop-blur-xl border-border/50 hover:bg-card/95 transition-all duration-300"
                >
                  <Activity className="mr-2 h-4 w-4" />
                  View Monitoring
                </Button>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
              className="mb-8"
            >
              {isLoadingClassrooms ? (
                <div className="h-12 w-80 animate-pulse rounded-xl bg-muted/50"></div>
              ) : classrooms && classrooms.length > 0 ? (
                <div className="flex items-center gap-4">
                  <span className="text-sm font-medium text-muted-foreground">Current Classroom:</span>
                  <Select
                    value={selectedClassroomId || undefined}
                    onValueChange={(value) => setSelectedClassroomId(value)}
                  >
                    <SelectTrigger className="w-80 rounded-xl bg-card/80 border-border/50">
                      <SelectValue placeholder="Select a classroom" />
                    </SelectTrigger>
                    <SelectContent className="rounded-xl border-border/50 bg-card/80 backdrop-blur-xl">
                      {classrooms.map((classroom) => (
                        <SelectItem key={classroom.physicalId} value={classroom.physicalId} className="rounded-lg">
                          {classroom.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              ) : (
                <div className="flex items-center gap-4">
                  <p className="text-sm text-muted-foreground">No classrooms available</p>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => router.push('/classroom')}
                    className="rounded-xl bg-background/50 border-border/50 hover:bg-background/80 transition-all duration-300"
                  >
                    <HeartPulse className="mr-2 h-4 w-4" />
                    Create Classroom
                  </Button>
                </div>
              )}
            </motion.div>
          

          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
              title="Active Students"
              value={totalActiveStudents.toString()}
              description="Students participating in tasks"
              icon={Users}
              trend={{ 
                value: studentCompletionRate, 
                positive: true 
              }}
            />
            <StatCard
              title="Exercise Activities"
              value={(tasksData?.length || 0).toString()}
              description="Total activities available"
              icon={Video}
              trend={{ 
                // Calculate percentage of activities that are in progress or completed
                value: tasksData && tasksData.length > 0 ? 
                  Math.round((tasksData.filter((t: Task) => t.started).length / tasksData.length) * 100) : 0, 
                positive: true 
              }}
            />
            <StatCard
              title="Average Score"
              value={overallAverageScore > 0 ? `${overallAverageScore}%` : 'N/A'}
              description="Class average score"
              icon={Heart}
              trend={overallAverageScore > 0 ? { 
                value: overallAverageScore, 
                positive: true
              } : undefined}
            />
            <StatCard
              title="Completion Rate"
              value={`${taskCompletionStats.completed}/${taskCompletionStats.total}`}
              description="Tasks completed by students"
              icon={Activity}
              trend={{ 
                value: taskCompletionStats.percentage, 
                positive: true 
              }}
            />
          </div>

          <div className="mt-6 grid gap-8 md:grid-cols-2">
            <OverviewCard
              title="Class Performance Overview"
              description="Current metrics for all classroom tasks"
              metrics={[
                {
                  label: 'Average Task Score',
                  value: overallAverageScore > 0 ? `${overallAverageScore}%` : 'N/A',
                  progress: overallAverageScore || 0,
                  status: overallAverageScore === 0 ? 'neutral' : 
                          overallAverageScore >= 70 ? 'positive' : 'warning'
                },
                {
                  label: 'Task Completion Rate',
                  value: `${taskCompletionStats.completed}/${taskCompletionStats.total}`,
                  progress: taskCompletionStats.percentage,
                  trend: { 
                    value: taskCompletionStats.percentage, 
                    positive: true 
                  }
                },
                {
                  label: 'Active Students',
                  value: totalActiveStudents.toString(),
                  progress: studentCompletionRate,
                  status: totalActiveStudents === 0 ? 'neutral' : 'positive'
                }
              ]}
              className="md:col-span-1"
            />
            <OverviewCard
              title="Leaderboard Overview"
              description={"Top students by overall performance"}
              metrics={[
                {
                  label: 'Top Student',
                  value: topStudent.studentName,
                  // Only show positive status if we have data
                  status: topStudent.studentId ? 'positive' : 'neutral'
                },
                {
                  label: 'Participants',
                  value: `${totalActiveStudents}`,
                  // Show neutral if no participants, positive if there are participants
                  status: totalActiveStudents > 0 ? 'positive' : 'neutral'
                },
                {
                  label: 'Average Score',
                  value: overallAverageScore > 0 ? `${overallAverageScore}%` : 'N/A',
                  // Always show neutral when no data is available
                  status: overallAverageScore === 0 ? 'neutral' : 
                           overallAverageScore > 80 ? 'positive' : 'warning'
                }
              ]}
              className="md:col-span-1"
            />
          </div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 1.0 }}
              className="mt-8 grid gap-8 md:grid-cols-1 mb-8"
            >
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 1.1 }}
              >
                <ActivityCard activities={recentActivities} className="md:col-span-1" />
              </motion.div>
            </motion.div>
          </main>
        </div>
      </div>
    </div>
  );
}