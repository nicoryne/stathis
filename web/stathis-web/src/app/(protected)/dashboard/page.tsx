'use client';

import React, { useState, useEffect } from 'react';
import { Sidebar } from '@/components/dashboard/sidebar';
import { StatCard } from '@/components/dashboard/stat-card';
import { ActivityCard } from '@/components/dashboard/activity-card';
import { AlertCard } from '@/components/dashboard/alert-card';
import { LineChart } from '@/components/dashboard/line-chart';
import { BarChart } from '@/components/dashboard/bar-chart';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/card';
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { OverviewCard } from '@/components/dashboard/overview-card';
import ThemeSwitcher from '@/components/theme-switcher';
import { getUserDetails, signOut } from '@/services/api-auth-client';
import { getCurrentUserEmail } from '@/lib/utils/jwt';
import { useQuery } from '@tanstack/react-query';
import { getTeacherClassrooms, ClassroomResponseDTO } from '@/services/api-classroom-client';
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
  profilePictureUrl?: string;
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
  const userEmail = getCurrentUserEmail();
  
  // Add state for selected classroom
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

  // Fetch all classrooms for the current teacher
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
  
  // Set the first classroom as default when classrooms data changes
  useEffect(() => {
    if (classrooms && classrooms.length > 0 && !selectedClassroomId) {
      setSelectedClassroomId(classrooms[0].physicalId);
    }
  }, [classrooms, selectedClassroomId]);

  // Fetch tasks for selected classroom
  const { data: tasksData, isLoading: isLoadingTasks } = useQuery<Task[]>({
    queryKey: ['tasks', selectedClassroomId],
    queryFn: async () => {
      if (!selectedClassroomId) return [];
      return await getTasksByClassroom(selectedClassroomId);
    },
    enabled: !!userEmail && !!selectedClassroomId,
    staleTime: 1000 * 60 * 5, // 5 minutes
  });

  // Set selected task when data is available
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

  // Fetch task scores for selected task (for detailed view if needed)
  const { data: rawTaskScoresData } = useQuery<Score[]>({
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
    status: "completed" | "ongoing" | "not-started";
    score?: number;
  };

  // Activity data from tasks API - formatted to match the ActivityCard component's expected structure
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

  // Check if we have any score data across all tasks
  const hasStudentScores = allTasksScores && Object.values(allTasksScores).some(
    taskData => taskData.analytics && taskData.analytics.averageScore > 0
  );
  
  // Exercise/Task score data for charts - shows class-wide average for each task
  // Each score is the average of ALL students' scores for that specific task
  const exerciseScoreData = (tasksData && allTasksScores) 
    ? tasksData.slice(0, 5).map((task: Task) => {
        const taskScore = allTasksScores[task.physicalId];
        // analytics.averageScore is the mean of all student scores for this task
        return {
          exercise: task.name || 'Unknown',
          score: taskScore?.analytics?.averageScore || 0,
          hasData: !!(taskScore?.analytics && taskScore.analytics.averageScore > 0)
        };
      })
    : [];
      
  // Generate task score metrics from real task data
  const taskScoreMetrics = (tasksData && taskScoresData) ? tasksData.map((task: Task) => ({
    name: task.name,
    status: !task.started ? 'not-started' as const : 
           (task.started && task.active) ? 'ongoing' as const : 
           'completed' as const,
    score: taskScoresData.averageScore
  })) : [];

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
                    <AvatarImage src={userDetails.profilePictureUrl || undefined} alt={`${userDetails.first_name} ${userDetails.last_name}`} />
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
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handlesignOut}>Sign out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>

        <main className="p-6">
          <div className="mb-6 flex items-center justify-between">
            <div className="flex items-center gap-4">
              <h1 className="text-2xl font-bold">Dashboard</h1>
              {/* Classroom Selector */}
              {isLoadingClassrooms ? (
                <div className="h-10 w-64 animate-pulse rounded bg-muted"></div>
              ) : classrooms && classrooms.length > 0 ? (
                <Select
                  value={selectedClassroomId || undefined}
                  onValueChange={(value) => setSelectedClassroomId(value)}
                >
                  <SelectTrigger className="w-64">
                    <SelectValue placeholder="Select a classroom" />
                  </SelectTrigger>
                  <SelectContent>
                    {classrooms.map((classroom) => (
                      <SelectItem key={classroom.physicalId} value={classroom.physicalId}>
                        {classroom.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              ) : (
                <div className="flex items-center gap-2">
                  <p className="text-sm text-muted-foreground">No classrooms available</p>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => router.push('/classroom')}
                  >
                    Create Classroom
                  </Button>
                </div>
              )}
            </div>
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

          <div className="mt-6 grid gap-6 md:grid-cols-2">
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

          <div className="mt-6 grid gap-6 md:grid-cols-2">
            <ActivityCard activities={recentActivities} className="md:col-span-1" />
            {hasStudentScores ? (
              <LineChart
                title="Task Performance"
                description="Scores by task"
                data={exerciseScoreData.map((item: {exercise: string; score: number}) => ({
                  exercise: item.exercise,  // Use full exercise name
                  score: item.score
                }))}
                categories={['score']}
                index="exercise"
                className="md:col-span-1"
              />
            ) : (
              <Card className="md:col-span-1">
                <CardHeader>
                  <CardTitle>Task Performance</CardTitle>
                  <CardDescription>Scores by task</CardDescription>
                </CardHeader>
                <CardContent className="flex items-center justify-center min-h-[220px] text-muted-foreground">
                  No score data available yet
                </CardContent>
              </Card>
            )}
          </div>

          <div className="mt-6 grid gap-6 md:grid-cols-2">
            {hasStudentScores ? (
              <BarChart
                title="Exercise Performance Analysis"
                description="Average score by exercise type"
                data={exerciseScoreData}
                categories={['score']}
                index="exercise"
                className="md:col-span-2"
              />
            ) : (
              <Card className="md:col-span-2">
                <CardHeader>
                  <CardTitle>Exercise Performance Analysis</CardTitle>
                  <CardDescription>Average score by exercise type</CardDescription>
                </CardHeader>
                <CardContent className="flex items-center justify-center min-h-[220px] text-muted-foreground">
                  No score data available yet
                </CardContent>
              </Card>
            )}
            {/* No safety alerts displayed since we're not using vital signs data */}
          </div>
        </main>
      </div>
    </div>
  );
}
