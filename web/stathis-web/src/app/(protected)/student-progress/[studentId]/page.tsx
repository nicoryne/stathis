'use client';

import React from 'react';
import Link from 'next/link';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { 
  getStudentBadges, 
  getStudentLeaderboardPosition,
  fetchStudentProgressItems,
  StudentDTO,
  StudentProgressItemDTO
} from '@/services/progress/api-progress-client';
import { Sidebar } from '@/components/dashboard/sidebar';

import { AuthNavbar } from '@/components/auth-navbar';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { 
  User, 
  ArrowLeft, 
  BookOpen, 
  Award,
  Trophy,
  BarChart,
  Calendar,
  CheckCircle2,
  XCircle,
  FileText,
  Dumbbell,
  GraduationCap
} from 'lucide-react';

export default function StudentProgressDetailPage() {
  const router = useRouter();
  const params = useParams<{ studentId: string }>();
  const studentId = params.studentId;

  // Get the classroom ID from the URL query parameter
  const searchParams = useSearchParams();
  const classroomId = searchParams.get('classroomId') || undefined;
  console.log(`Got classroom ID from URL: ${classroomId || 'none'} for student ID: ${studentId}`);
    
  // Fetch student progress items using the new API endpoint with classroom context
  const {
    data: progressItems,
    isLoading: isProgressItemsLoading,
    isError: isProgressError,
    error: progressError
  } = useQuery({
    queryKey: ['student-progress-items', studentId, classroomId],
    queryFn: () => fetchStudentProgressItems(studentId, classroomId),
    enabled: !!studentId && !!classroomId, // Only run if we have both IDs
    retry: 2, // Retry failed requests up to 2 times
    staleTime: 1000 * 60 * 5, // Cache for 5 minutes
  });

  // Fetch student badges
  const { 
    data: studentBadges, 
    isLoading: isBadgesLoading 
  } = useQuery({
    queryKey: ['student-badges', studentId],
    queryFn: () => getStudentBadges(studentId),
    enabled: !!studentId,
  });

  // Fetch student leaderboard position
  const { 
    data: leaderboardData, 
    isLoading: isLeaderboardLoading 
  } = useQuery({
    queryKey: ['student-leaderboard', studentId],
    queryFn: () => getStudentLeaderboardPosition(studentId),
    enabled: !!studentId,
  });

  // Fetch classroom students to get student details and enrollment date
  const { 
    data: classroomStudentsData,
    isLoading: isClassroomStudentsLoading 
  } = useQuery({
    queryKey: ['classroom-students-for-enrollment', classroomId],
    queryFn: async () => {
      if (!classroomId) return null;
      const { getClassroomStudents } = await import('@/services/api-classroom');
      return getClassroomStudents(classroomId);
    },
    enabled: !!classroomId,
  });
  
  // Extract student ID parts for display in case we can't load full details
  const idParts = studentId.split('-');
  const studentNumber = idParts.length >= 3 ? idParts[2] : studentId;
  
  // Find the student in the classroom students list
  const studentFromClassroom = classroomStudentsData?.students?.find(
    s => s.physicalId === studentId
  );
  
  // Get enrollment date from classroom data
  const enrollmentDate = studentFromClassroom?.joinedAt;
  
  // Use student data from classroom if available, otherwise create a fallback
  const studentData: StudentDTO = studentFromClassroom ? {
    physicalId: studentFromClassroom.physicalId,
    firstName: studentFromClassroom.firstName,
    lastName: studentFromClassroom.lastName,
    email: studentFromClassroom.email,
    profilePictureUrl: studentFromClassroom.profilePictureUrl,
    isVerified: (studentFromClassroom as any).verified || (studentFromClassroom as any).isVerified || true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  } : {
    // Fallback student data if details aren't available
    physicalId: studentId,
    firstName: `Student ${studentNumber}`,
    lastName: '',
    email: '',
    profilePictureUrl: '',
    isVerified: true,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString()
  };
  
  // Create computed statistics from progress items
  const computeStatistics = () => {
    if (!progressItems || progressItems.length === 0) {
      return {
        lessonCompleted: false,
        exerciseCompleted: false,
        quizCompleted: false,
        quizScore: 0,
        maxQuizScore: 100,
        quizAttempts: 0,
        totalTimeTaken: 0,
        kpis: {
          averageScore: 0,
          recentActivityDays: 0,
          timeSpentMinutes: 0,
          currentStreakDays: 0
        },
        performanceHistory: []
      };
    }
    
    // Calculate task completion stats
    const lessonCompleted = progressItems.some(item => 
      item.taskType.toUpperCase() === 'LESSON' && item.completed);
      
    const exerciseCompleted = progressItems.some(item => 
      item.taskType.toUpperCase() === 'EXERCISE' && item.completed);
      
    const quizCompleted = progressItems.some(item => 
      item.taskType.toUpperCase() === 'QUIZ' && item.completed);
    
    // Calculate quiz stats
    const quizItems = progressItems.filter(item => 
      item.taskType.toUpperCase() === 'QUIZ');
      
    const quizScore = quizItems.length > 0 
      ? Math.round(quizItems.reduce((sum, item) => sum + (item.score || 0), 0) / quizItems.length)
      : 0;
      
    const maxQuizScore = quizItems.length > 0
      ? Math.max(...quizItems.map(item => item.maxScore || 100))
      : 100;
      
    const quizAttempts = quizItems.length;
    
    // Calculate KPIs
    // Average score across all tasks - only consider items with actual scores (exclude lessons)
    const scoredItems = progressItems.filter(item => 
      item.score !== null && item.taskType?.toUpperCase() !== 'LESSON'
    );
    const totalPoints = scoredItems.reduce((sum, item) => sum + (item.score || 0), 0);
    const totalPossible = scoredItems.reduce((sum, item) => sum + (item.maxScore || 100), 0);
    
    // Calculate percentage only if we have valid scored items
    const averageScore = scoredItems.length > 0 && totalPossible > 0
      ? Math.round(totalPoints / totalPossible * 100)
      : 0;
    
    // Calculate recent activity
    const completedItems = progressItems.filter(item => item.completed && item.completedAt);
    const latestActivity = completedItems.length > 0
      ? new Date(Math.max(...completedItems.map(item => 
          item.completedAt ? new Date(item.completedAt).getTime() : 0)))
      : null;
    
    const recentActivityDays = latestActivity
      ? Math.round((new Date().getTime() - latestActivity.getTime()) / (1000 * 60 * 60 * 24))
      : 7;
    
    // Estimate time spent based on completed tasks
    const timeSpentMinutes = completedItems.length * 20; // 20 minutes per task
    
    // Consider active if activity in last 3 days
    const currentStreakDays = recentActivityDays < 3 ? 1 : 0;
    
    // Create performance history
    const performanceHistory = progressItems
      .filter(item => item.taskName) // Only include items with names
      .slice(0, 5) // Take top 5
      .map(item => ({
        period: item.taskType,
        score: item.score || 0,
        maxScore: item.maxScore || 100,
        taskName: item.taskName,
        taskType: item.taskType
      }));
    
    return {
      lessonCompleted,
      exerciseCompleted,
      quizCompleted,
      quizScore,
      maxQuizScore,
      quizAttempts,
      totalTimeTaken: timeSpentMinutes * 60, // Convert to seconds
      kpis: {
        averageScore,
        recentActivityDays,
        timeSpentMinutes,
        currentStreakDays
      },
      performanceHistory
    };
  };
  
  // Generate computed progress data
  const progressStats = computeStatistics();
  
  // Loading state - show skeleton if data is loading but not if there's an error
  const isLoading = (isProgressItemsLoading && !isProgressError) || isClassroomStudentsLoading;
  
  // Error state - show error message if progress data fails to load and isn't loading
  const isError = isProgressError && !isProgressItemsLoading;
  
  // Handle 403 errors gracefully by using fallback data
  const hasValidData = !!progressItems || !!studentFromClassroom;
  
  // Use progress statistics for KPIs when available
  const overallScore = progressStats.kpis.averageScore.toFixed(1);
  
  // Calculate task statistics from progress items
  const completedTasks = progressItems?.filter(item => item.completed)?.length || 0;
  const totalTasks = progressItems?.length || 0;
  const timeSpent = progressStats.kpis.timeSpentMinutes;
  const streakDays = progressStats.kpis.currentStreakDays;
  const recentActivity = progressStats.kpis.recentActivityDays;

  return (
    <div className="flex min-h-screen">
      <Sidebar className="w-64 flex-shrink-0" />
      
      <div className="flex-1 md:ml-64">
        <AuthNavbar />
        
        <main className="p-6">
          <div className="mb-6 flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0">
            <div>
              <h1 className="text-2xl font-bold tracking-tight">{`${studentData.firstName} ${studentData.lastName}'s Progress`}</h1>
              <p className="text-muted-foreground mt-1">View detailed performance metrics</p>
            </div>
            <div>
              <Button variant="outline" size="sm" onClick={() => router.back()}>
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Students
              </Button>
            </div>
          </div>
          
          {/* Error state */}
          {isError && (
            <Card className="mb-6 border-red-200 bg-red-50 dark:bg-red-950/30">
              <CardContent className="p-6">
                <div className="flex items-center gap-3">
                  <XCircle className="h-8 w-8 text-red-500" />
                  <div>
                    <h2 className="text-xl font-semibold">Failed to load student progress</h2>
                    <p className="mt-1 text-muted-foreground">
                      There was an error loading the student progress data. Please try again later or contact support if the problem persists.
                    </p>
                    <Button variant="outline" size="sm" className="mt-3" onClick={() => window.location.reload()}>
                      Retry
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Loading state */}
          {isLoading && (
            <Card className="mb-6">
              <CardContent className="p-6">
                <div className="flex flex-col gap-6">
                  <div className="flex items-center gap-4">
                    <Skeleton className="h-20 w-20 rounded-full" />
                    <div className="space-y-2">
                      <Skeleton className="h-6 w-60" />
                      <Skeleton className="h-4 w-40" />
                    </div>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    <Skeleton className="h-8 w-32" />
                    <Skeleton className="h-8 w-24" />
                    <Skeleton className="h-8 w-28" />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <Skeleton className="h-28 w-full" />
                    <Skeleton className="h-28 w-full" />
                    <Skeleton className="h-28 w-full" />
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
          
          {!isLoading && !isError && (
            <div className="grid gap-8">
              {/* Student profile summary */}
              <Card>
                <CardContent className="p-6">
                  <div className="flex flex-col md:flex-row gap-6 items-start">
                    <div className="flex-shrink-0">
                      <Avatar className="h-20 w-20">
                        <AvatarImage src={studentData.profilePictureUrl || ''} alt={`${studentData.firstName} ${studentData.lastName}`} />
                        <AvatarFallback className="text-xl">
                          {studentData.firstName.charAt(0)}{studentData.lastName.charAt(0)}
                        </AvatarFallback>
                      </Avatar>
                    </div>
                    <div className="flex-grow space-y-2">
                      <div>
                        <h1 className="text-2xl font-bold mb-1">
                          {studentData.firstName} {studentData.lastName}'s Progress
                        </h1>
                        <p className="text-muted-foreground">{studentData.email}</p>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        <Badge variant="outline" className="flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          Enrolled: {enrollmentDate ? new Date(enrollmentDate).toLocaleDateString() : 'N/A'}
                        </Badge>
                        {leaderboardData && leaderboardData[0] && (
                          <Badge variant="outline" className="flex items-center gap-1">
                            <Trophy className="h-3 w-3" />
                            Rank: #{leaderboardData[0].rank}
                          </Badge>
                        )}
                        <Badge variant="outline" className="flex items-center gap-1">
                          <Award className="h-3 w-3" />
                          Badges: {studentBadges?.length || 0}
                        </Badge>
                      </div>
                    </div>
              <div className="flex-shrink-0 w-full md:w-auto">
                <Card className="bg-muted">
                  <CardHeader className="pb-2">
                    <CardTitle className="text-sm flex items-center">
                      <BarChart className="h-4 w-4 mr-2" />
                      Overall Performance
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="text-3xl font-bold flex items-baseline">
                      <span className={
                        overallScore === 'N/A' ? 'text-muted-foreground' :
                        parseFloat(overallScore as string) >= 70 ? 'text-green-600' :
                        parseFloat(overallScore as string) >= 50 ? 'text-amber-600' : 'text-red-600'
                      }>
                        {overallScore === 'N/A' ? overallScore : `${overallScore}`}
                      </span>
                      <span className="text-muted-foreground text-lg ml-1">/100</span>
                    </div>
                    <div className="w-full bg-gray-200 dark:bg-gray-700 rounded-full h-2.5 mt-2">
                      <div 
                        className={`h-2.5 rounded-full ${overallScore === 'N/A' ? 'bg-gray-400' :
                          parseFloat(overallScore as string) >= 70 ? 'bg-green-500' :
                          parseFloat(overallScore as string) >= 50 ? 'bg-amber-500' : 'bg-red-500'}`}
                        style={{ width: `${overallScore === 'N/A' ? 0 : Math.min(100, parseFloat(overallScore as string))}%` }}
                      ></div>
                    </div>
                    <div className="flex justify-between items-center mt-2">
                      <p className="text-xs text-muted-foreground">
                        <span className="font-medium">{completedTasks}</span>/{totalTasks} tasks
                      </p>
                      <Badge variant="outline" className="text-xs">
                        {overallScore === 'N/A' ? 'Not Rated' :
                          parseFloat(overallScore as string) >= 90 ? 'Excellent' :
                          parseFloat(overallScore as string) >= 80 ? 'Very Good' :
                          parseFloat(overallScore as string) >= 70 ? 'Good' :
                          parseFloat(overallScore as string) >= 60 ? 'Fair' :
                          parseFloat(overallScore as string) >= 50 ? 'Needs Improvement' : 'Poor'}
                      </Badge>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>

            {/* Additional KPI metrics */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
              <Card>
                <CardContent className="pt-6">
                  <div className="flex flex-col items-center">
                    <span className="text-muted-foreground text-sm mb-1">Time Spent</span>
                    <div className="flex items-baseline">
                      <span className="text-3xl font-bold mr-1">{progressStats.kpis.timeSpentMinutes}</span>
                      <span className="text-muted-foreground">minutes</span>
                    </div>
                    <span className="text-xs text-muted-foreground mt-2">Total time on tasks</span>
                  </div>
                </CardContent>
              </Card>
              
              <Card>
                <CardContent className="pt-6">
                  <div className="flex flex-col items-center">
                    <span className="text-muted-foreground text-sm mb-1">Current Streak</span>
                    <div className="flex items-baseline">
                      <span className="text-3xl font-bold mr-1">{progressStats.kpis.currentStreakDays}</span>
                      <span className="text-muted-foreground">days</span>
                    </div>
                    <span className="text-xs text-muted-foreground mt-2">Consecutive days of activity</span>
                  </div>
                </CardContent>
              </Card>
              
              <Card>
                <CardContent className="pt-6">
                  <div className="flex flex-col items-center">
                    <span className="text-muted-foreground text-sm mb-1">Recent Activity</span>
                    <div className="flex items-baseline">
                      <span className="text-3xl font-bold mr-1">{progressStats.kpis.recentActivityDays}</span>
                      <span className="text-muted-foreground">days ago</span>
                    </div>
                    <span className="text-xs text-muted-foreground mt-2">Last interaction with the platform</span>
                  </div>
                </CardContent>
              </Card>
            </div>
          </CardContent>
        </Card>

        {/* Status Messages Section has been removed as it's not part of the new API */}

        {/* Performance tabs */}
        <Tabs defaultValue="scores">
          <TabsList className="grid w-full grid-cols-3 lg:w-[400px]">
            <TabsTrigger value="scores">Task Scores</TabsTrigger>
            <TabsTrigger value="badges">Badges</TabsTrigger>
            <TabsTrigger value="ranking">Ranking</TabsTrigger>
          </TabsList>

          {/* Task Scores Tab */}
          <TabsContent value="scores" className="space-y-4 mt-6">
            {isProgressItemsLoading ? (
              <div className="space-y-4">
                {Array.from({ length: 3 }).map((_, index) => (
                  <Card key={index}>
                    <CardHeader>
                      <Skeleton className="h-6 w-[150px]" />
                    </CardHeader>
                    <CardContent>
                      <Skeleton className="h-[200px] w-full" />
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : !progressItems?.length ? (
              <Card>
                <CardContent className="py-6">
                  <div className="text-center text-muted-foreground">
                    No task progress available for this student yet
                  </div>
                </CardContent>
              </Card>
            ) : (
              <>
                {/* Quizzes Section */}
                {(() => {
                  const quizItems = progressItems
                    .filter(item => item.taskType?.toUpperCase() === 'QUIZ')
                    .sort((a, b) => {
                      const dateA = a.completedAt ? new Date(a.completedAt).getTime() : 0;
                      const dateB = b.completedAt ? new Date(b.completedAt).getTime() : 0;
                      return dateB - dateA;
                    });
                  
                  return quizItems.length > 0 ? (
                    <Card>
                      <CardHeader>
                        <div className="flex items-center gap-2">
                          <BarChart className="h-5 w-5 text-blue-600" />
                          <CardTitle className="text-lg">Quizzes</CardTitle>
                        </div>
                        <CardDescription>{quizItems.length} total {quizItems.length === 1 ? 'quiz' : 'quizzes'}</CardDescription>
                      </CardHeader>
                      <CardContent>
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead className="w-[40%]">Task Name</TableHead>
                              <TableHead className="w-[30%]">Completed</TableHead>
                              <TableHead className="w-[30%] text-right">Score</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {quizItems.map((item) => (
                              <TableRow key={item.taskId}>
                                <TableCell className="font-medium">{item.taskName}</TableCell>
                                <TableCell>
                                  {item.completed ? (
                                    <span className="inline-flex items-center text-green-600">
                                      <CheckCircle2 className="h-4 w-4 mr-1" />
                                      <span className="font-medium">Yes</span>
                                    </span>
                                  ) : (
                                    <span className="inline-flex items-center text-red-600">
                                      <XCircle className="h-4 w-4 mr-1" />
                                      <span className="font-medium">No</span>
                                    </span>
                                  )}
                                </TableCell>
                                <TableCell className="text-right">
                                  <span className="font-medium">
                                    {item.score !== null ? 
                                      item.maxScore !== null ? 
                                        `${item.score}/${item.maxScore}` : 
                                        item.score 
                                      : (
                                        <span className="text-gray-500 italic">Not Answered</span>
                                      )
                                    }
                                  </span>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </CardContent>
                    </Card>
                  ) : null;
                })()}

                {/* Exercises Section */}
                {(() => {
                  const exerciseItems = progressItems
                    .filter(item => item.taskType?.toUpperCase() === 'EXERCISE')
                    .sort((a, b) => {
                      const dateA = a.completedAt ? new Date(a.completedAt).getTime() : 0;
                      const dateB = b.completedAt ? new Date(b.completedAt).getTime() : 0;
                      return dateB - dateA;
                    });
                  
                  return exerciseItems.length > 0 ? (
                    <Card>
                      <CardHeader>
                        <div className="flex items-center gap-2">
                          <Dumbbell className="h-5 w-5 text-orange-600" />
                          <CardTitle className="text-lg">Exercises</CardTitle>
                        </div>
                        <CardDescription>{exerciseItems.length} total {exerciseItems.length === 1 ? 'exercise' : 'exercises'}</CardDescription>
                      </CardHeader>
                      <CardContent>
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead className="w-[40%]">Task Name</TableHead>
                              <TableHead className="w-[30%]">Completed</TableHead>
                              <TableHead className="w-[30%] text-right">Score</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {exerciseItems.map((item) => (
                              <TableRow key={item.taskId}>
                                <TableCell className="font-medium">{item.taskName}</TableCell>
                                <TableCell>
                                  {item.completed ? (
                                    <span className="inline-flex items-center text-green-600">
                                      <CheckCircle2 className="h-4 w-4 mr-1" />
                                      <span className="font-medium">Yes</span>
                                    </span>
                                  ) : (
                                    <span className="inline-flex items-center text-red-600">
                                      <XCircle className="h-4 w-4 mr-1" />
                                      <span className="font-medium">No</span>
                                    </span>
                                  )}
                                </TableCell>
                                <TableCell className="text-right">
                                  <span className="font-medium">
                                    {item.score !== null ? 
                                      item.maxScore !== null ? 
                                        `${item.score}/${item.maxScore}` : 
                                        item.score 
                                      : (
                                        <span className="text-gray-500 italic">Not Answered</span>
                                      )
                                    }
                                  </span>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </CardContent>
                    </Card>
                  ) : null;
                })()}

                {/* Lessons Section */}
                {(() => {
                  const lessonItems = progressItems
                    .filter(item => item.taskType?.toUpperCase() === 'LESSON')
                    .sort((a, b) => {
                      const dateA = a.completedAt ? new Date(a.completedAt).getTime() : 0;
                      const dateB = b.completedAt ? new Date(b.completedAt).getTime() : 0;
                      return dateB - dateA;
                    });
                  
                  return lessonItems.length > 0 ? (
                    <Card>
                      <CardHeader>
                        <div className="flex items-center gap-2">
                          <BookOpen className="h-5 w-5 text-green-600" />
                          <CardTitle className="text-lg">Lessons</CardTitle>
                        </div>
                        <CardDescription>{lessonItems.length} total {lessonItems.length === 1 ? 'lesson' : 'lessons'}</CardDescription>
                      </CardHeader>
                      <CardContent>
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead className="w-[40%]">Task Name</TableHead>
                              <TableHead className="w-[30%]">Completed</TableHead>
                              <TableHead className="w-[30%] text-right">Score</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {lessonItems.map((item) => (
                              <TableRow key={item.taskId}>
                                <TableCell className="font-medium">{item.taskName}</TableCell>
                                <TableCell>
                                  {item.completed ? (
                                    <span className="inline-flex items-center text-green-600">
                                      <CheckCircle2 className="h-4 w-4 mr-1" />
                                      <span className="font-medium">Yes</span>
                                    </span>
                                  ) : (
                                    <span className="inline-flex items-center text-red-600">
                                      <XCircle className="h-4 w-4 mr-1" />
                                      <span className="font-medium">No</span>
                                    </span>
                                  )}
                                </TableCell>
                                <TableCell className="text-right">
                                  <span className="text-gray-400">—</span>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      </CardContent>
                    </Card>
                  ) : null;
                })()}
              </>
            )}
          </TabsContent>

          {/* Badges Tab */}
          <TabsContent value="badges" className="space-y-4 mt-6">
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <div>
                    <CardTitle className="text-xl">Achievement Badges</CardTitle>
                    <CardDescription>Badges earned for completing tasks and challenges</CardDescription>
                  </div>
                  <Award className="h-6 w-6 text-muted-foreground" />
                </div>
              </CardHeader>
              <CardContent>
                {isBadgesLoading ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {Array.from({ length: 6 }).map((_, index) => (
                      <Skeleton key={index} className="h-32 w-full rounded-md" />
                    ))}
                  </div>
                ) : !studentBadges?.length ? (
                  <div className="text-center py-10 text-muted-foreground">
                    No badges earned yet
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    {studentBadges.map((badge) => (
                      <Card key={badge.id} className="overflow-hidden border-2 hover:border-primary/50 transition-all">
                        <div className="p-2 flex items-center gap-3">
                          <div className="bg-muted rounded-full p-2">
                            <img 
                              src={badge.imageUrl || '/placeholder-badge.svg'} 
                              alt={badge.name}
                              className="h-12 w-12 rounded-full object-cover"
                              onError={(e) => {
                                (e.target as HTMLImageElement).src = 'https://placehold.co/200x200?text=Badge';
                              }}
                            />
                          </div>
                          <div>
                            <h3 className="font-medium">{badge.name}</h3>
                            <p className="text-sm text-muted-foreground">{badge.description}</p>
                            <p className="text-xs mt-1">
                              Awarded: {new Date(badge.acquiredDate).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
                      </Card>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>

          {/* Ranking Tab */}
          <TabsContent value="ranking" className="space-y-4 mt-6">
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <div>
                    <CardTitle className="text-xl">Leaderboard Position</CardTitle>
                    <CardDescription>Student's ranking compared to peers</CardDescription>
                  </div>
                  <Trophy className="h-6 w-6 text-muted-foreground" />
                </div>
              </CardHeader>
              <CardContent>
                {isLeaderboardLoading ? (
                  <div className="space-y-4">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <div key={index} className="flex justify-between items-center">
                        <Skeleton className="h-4 w-[50px]" />
                        <Skeleton className="h-4 w-[150px]" />
                        <Skeleton className="h-4 w-[100px]" />
                      </div>
                    ))}
                  </div>
                ) : !leaderboardData?.length ? (
                  <div className="text-center py-6 text-muted-foreground">
                    No leaderboard data available
                  </div>
                ) : (
                  <div className="space-y-6">
                    {/* Main student ranking card */}
                    {leaderboardData[0] && (
                      <div className="bg-muted rounded-lg p-6 flex items-center justify-between">
                        <div className="flex items-center gap-4">
                          <div className="bg-primary/10 text-primary rounded-full h-12 w-12 flex items-center justify-center font-bold text-lg">
                            #{leaderboardData[0].rank}
                          </div>
                          <div>
                            <h3 className="font-bold text-lg">{studentData.firstName} {studentData.lastName}</h3>
                            <p className="text-sm text-muted-foreground">Current Ranking</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-2xl font-bold">{leaderboardData[0].score}</div>
                          <div className="text-sm text-muted-foreground">Total Points</div>
                          {leaderboardData[0].change && (
                            <div className={`text-xs ${leaderboardData[0].change > 0 ? 'text-green-600' : 'text-red-600'}`}>
                              {leaderboardData[0].change > 0 ? '↑' : '↓'} 
                              {Math.abs(leaderboardData[0].change)} since last week
                            </div>
                          )}
                        </div>
                      </div>
                    )}

                    {/* Nearby students in ranking */}
                    <div>
                      <h3 className="text-sm font-medium mb-3">Nearby Students in Ranking</h3>
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>#</TableHead>
                            <TableHead>Task/Period</TableHead>
                            <TableHead className="text-right">Score</TableHead>
                            <TableHead className="text-right">Max</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {/* Use performance history from progress stats */}
                      {progressStats.performanceHistory.map((entry, index) => (
                        <TableRow key={`performance-${index}`}>
                          <TableCell className="font-medium">#{index + 1}</TableCell>
                          <TableCell>
                            <div className="flex items-center gap-2">
                              {entry.taskType?.toLowerCase().includes('quiz') && <BarChart className="h-3 w-3 text-blue-600" />}
                              {entry.taskType?.toLowerCase().includes('test') && <FileText className="h-3 w-3 text-purple-600" />}
                              {entry.taskType?.toLowerCase().includes('assess') && <GraduationCap className="h-3 w-3 text-indigo-600" />}
                              {entry.taskType?.toLowerCase().includes('lesson') && <BookOpen className="h-3 w-3 text-green-600" />}
                              {entry.taskType?.toLowerCase().includes('exercise') && <Dumbbell className="h-3 w-3 text-orange-600" />}
                              <span className="font-medium">{entry.period}</span>
                            </div>
                            {entry.taskName && <span className="text-xs text-muted-foreground ml-5">{entry.taskName}</span>}
                          </TableCell>
                          <TableCell className="text-right">
                            <div>
                              <div className="flex items-center justify-end">
                                <span className={`font-medium ${
                                  (entry.score / (entry.maxScore || 100)) >= 0.7 
                                    ? 'text-green-600' 
                                    : (entry.score / (entry.maxScore || 100)) >= 0.5 
                                    ? 'text-amber-600' 
                                    : 'text-red-600'
                                }`}>
                                  {entry.score}
                                </span>
                              </div>
                              {/* Score bar visualization */}
                              <div className="w-full bg-gray-200 rounded-full h-1.5 mt-1">
                                <div 
                                  className={`h-1.5 rounded-full ${
                                    (entry.score / (entry.maxScore || 100)) >= 0.7 
                                      ? 'bg-green-500' 
                                      : (entry.score / (entry.maxScore || 100)) >= 0.5 
                                      ? 'bg-amber-500' 
                                      : 'bg-red-500'
                                  }`}
                                  style={{ width: `${Math.min(100, (entry.score / (entry.maxScore || 100)) * 100)}%` }}
                                ></div>
                              </div>
                            </div>
                          </TableCell>
                          <TableCell className="text-right text-muted-foreground">{entry.maxScore || 100}</TableCell>
                        </TableRow>
                      ))}
                      
                      {/* Show the current student's rank */}
                      {leaderboardData && leaderboardData[0] && (
                        <TableRow className="bg-muted">
                          <TableCell className="font-medium">#{leaderboardData[0].rank}</TableCell>
                          <TableCell>
                            <span className="font-medium">{studentData.firstName} {studentData.lastName}</span>
                            <span className="text-xs text-muted-foreground ml-2">(Current ranking)</span>
                          </TableCell>
                          <TableCell className="text-right font-medium">{leaderboardData[0].score}</TableCell>
                          <TableCell className="text-right text-muted-foreground">100</TableCell>
                        </TableRow>
                      )}
                      
                      {/* If no performance history data is available, show placeholder message */}
                      {!progressStats.performanceHistory.length && !leaderboardData?.length && (
                        <TableRow>
                          <TableCell colSpan={4} className="text-center text-muted-foreground py-4">
                            No performance history available
                          </TableCell>
                        </TableRow>
                      )}
                        </TableBody>
                      </Table>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
