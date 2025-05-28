'use client';

import React from 'react';
import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import { 
  getStudentScores, 
  getStudentBadges, 
  getStudentLeaderboardPosition 
} from '@/services/progress/api-progress-client';
import { DashboardShell } from '@/components/dashboard/dashboard-shell';
import { DashboardHeader } from '@/components/dashboard/dashboard-header';
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
  XCircle
} from 'lucide-react';

export default function StudentProgressDetailPage() {
  const router = useRouter();
  const params = useParams<{ studentId: string }>();
  const studentId = params.studentId;

  // Fetch student scores
  const { 
    data: studentScores, 
    isLoading: isScoresLoading 
  } = useQuery({
    queryKey: ['student-scores', studentId],
    queryFn: () => getStudentScores(studentId),
    enabled: !!studentId,
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

  // Mock student data - in a real app, this would come from an API
  const student = {
    physicalId: studentId,
    firstName: 'Alex',
    lastName: 'Johnson',
    email: 'alex.johnson@example.com',
    profileImage: '',
    enrollmentDate: '2024-01-15',
  };

  // Calculate overall statistics
  const overallScore = studentScores?.length 
    ? (studentScores.reduce((sum, score) => sum + (score.scoreValue || 0), 0) / studentScores.length).toFixed(1)
    : 'N/A';
  
  const completedTasks = studentScores?.filter(score => score.isCompleted)?.length || 0;
  const totalTasks = studentScores?.length || 0;

  return (
    <DashboardShell>
      <DashboardHeader heading={`${student.firstName} ${student.lastName}'s Progress`} text="View detailed performance metrics">
        <Button variant="outline" size="sm" onClick={() => router.back()}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Students
        </Button>
      </DashboardHeader>

      <div className="grid gap-8">
        {/* Student profile summary */}
        <Card>
          <CardContent className="p-6">
            <div className="flex flex-col md:flex-row gap-6 items-start">
              <div className="flex-shrink-0">
                <Avatar className="h-20 w-20">
                  <AvatarImage src={student.profileImage} alt={`${student.firstName} ${student.lastName}`} />
                  <AvatarFallback className="text-xl">
                    {student.firstName.charAt(0)}{student.lastName.charAt(0)}
                  </AvatarFallback>
                </Avatar>
              </div>
              <div className="flex-grow space-y-2">
                <div>
                  <h2 className="text-2xl font-bold">{student.firstName} {student.lastName}</h2>
                  <p className="text-muted-foreground">{student.email}</p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Badge variant="outline" className="flex items-center gap-1">
                    <Calendar className="h-3 w-3" />
                    Enrolled: {new Date(student.enrollmentDate).toLocaleDateString()}
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
                    <CardTitle className="text-sm">Overall Performance</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="text-3xl font-bold">{overallScore === 'N/A' ? overallScore : `${overallScore}%`}</div>
                    <Progress 
                      value={overallScore === 'N/A' ? 0 : parseFloat(overallScore as string)} 
                      className="h-2 mt-2" 
                    />
                    <p className="text-xs text-muted-foreground mt-2">
                      Tasks: {completedTasks}/{totalTasks} completed
                    </p>
                  </CardContent>
                </Card>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Performance tabs */}
        <Tabs defaultValue="scores">
          <TabsList className="grid w-full grid-cols-3 lg:w-[400px]">
            <TabsTrigger value="scores">Task Scores</TabsTrigger>
            <TabsTrigger value="badges">Badges</TabsTrigger>
            <TabsTrigger value="ranking">Ranking</TabsTrigger>
          </TabsList>

          {/* Task Scores Tab */}
          <TabsContent value="scores" className="space-y-4 mt-6">
            <Card>
              <CardHeader>
                <div className="flex justify-between items-center">
                  <div>
                    <CardTitle className="text-xl">Task Performance</CardTitle>
                    <CardDescription>Scores for all completed tasks</CardDescription>
                  </div>
                  <BookOpen className="h-6 w-6 text-muted-foreground" />
                </div>
              </CardHeader>
              <CardContent>
                {isScoresLoading ? (
                  <div className="space-y-4">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <div key={index} className="flex justify-between items-center">
                        <Skeleton className="h-4 w-[250px]" />
                        <Skeleton className="h-4 w-[100px]" />
                      </div>
                    ))}
                  </div>
                ) : !studentScores?.length ? (
                  <div className="text-center py-6 text-muted-foreground">
                    No task scores available for this student yet
                  </div>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>Task Name</TableHead>
                        <TableHead>Type</TableHead>
                        <TableHead>Completed</TableHead>
                        <TableHead className="text-right">Score</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {studentScores.map((score) => (
                        <TableRow key={score.physicalId}>
                          <TableCell className="font-medium">
                            {score.taskName || `Task ${score.taskId}`}
                          </TableCell>
                          <TableCell>
                            <Badge variant="outline">
                              {score.taskType}
                            </Badge>
                          </TableCell>
                          <TableCell>
                            {score.isCompleted ? (
                              <span className="flex items-center text-green-600">
                                <CheckCircle2 className="h-4 w-4 mr-1" />
                                Yes
                              </span>
                            ) : (
                              <span className="flex items-center text-red-600">
                                <XCircle className="h-4 w-4 mr-1" />
                                No
                              </span>
                            )}
                          </TableCell>
                          <TableCell className="text-right">
                            <span className={`font-medium ${
                              (score.scoreValue || 0) >= 70 
                                ? 'text-green-600' 
                                : (score.scoreValue || 0) >= 50 
                                ? 'text-amber-600' 
                                : 'text-red-600'
                            }`}>
                              {score.scoreValue ? `${score.scoreValue}%` : 'N/A'}
                            </span>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </CardContent>
            </Card>
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
                            <h3 className="font-bold text-lg">{student.firstName} {student.lastName}</h3>
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
                            <TableHead>Rank</TableHead>
                            <TableHead>Student</TableHead>
                            <TableHead className="text-right">Score</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {/* Just showing mock data here */}
                          <TableRow>
                            <TableCell className="font-medium">#1</TableCell>
                            <TableCell>Emma Wilson</TableCell>
                            <TableCell className="text-right">950</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell className="font-medium">#2</TableCell>
                            <TableCell>Carlos Rodriguez</TableCell>
                            <TableCell className="text-right">920</TableCell>
                          </TableRow>
                          {leaderboardData[0] && (
                            <TableRow className="bg-muted">
                              <TableCell className="font-medium">#{leaderboardData[0].rank}</TableCell>
                              <TableCell>
                                <span className="font-medium">{student.firstName} {student.lastName}</span>
                              </TableCell>
                              <TableCell className="text-right">{leaderboardData[0].score}</TableCell>
                            </TableRow>
                          )}
                          <TableRow>
                            <TableCell className="font-medium">#4</TableCell>
                            <TableCell>Sarah Ahmed</TableCell>
                            <TableCell className="text-right">880</TableCell>
                          </TableRow>
                          <TableRow>
                            <TableCell className="font-medium">#5</TableCell>
                            <TableCell>Michael Patel</TableCell>
                            <TableCell className="text-right">850</TableCell>
                          </TableRow>
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
    </DashboardShell>
  );
}
