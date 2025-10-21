'use client';

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { 
  getStudentBadges, 
  fetchStudentProgressItems,
  StudentProgressItemDTO,
  BadgeDTO
} from '@/services/progress/api-progress-client';
import { exportStudentScoresReport } from '@/lib/utils/export-utils';
import { getTeacherClassrooms, getClassroomStudents, ClassroomResponseDTO } from '@/services/api-classroom';
import { DashboardShell } from '@/components/dashboard/dashboard-shell';
import { DashboardHeader } from '@/components/dashboard/dashboard-header';
import { Sidebar } from '@/components/dashboard/sidebar';
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
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Skeleton } from '@/components/ui/skeleton';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { 
  Users, 
  Search, 
  ArrowUpDown, 
  Eye, 
  BarChart,
  ArrowRight,
  BookOpen,
  Award,
  Bell,
  Download,
  TrendingUp,
  GraduationCap
} from 'lucide-react';
import { AuthNavbar } from '@/components/auth-navbar';
import { motion, useReducedMotion } from 'framer-motion';
import Image from 'next/image';

export default function StudentProgressPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const prefersReducedMotion = useReducedMotion();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedClassroom, setSelectedClassroom] = useState('');

  
  // Fetch teacher's classrooms from API
  const { data: classroomsData, isLoading: isClassroomsLoading } = useQuery<ClassroomResponseDTO[]>({
    queryKey: ['teacher-classrooms'],
    queryFn: async () => {
      try {
        return await getTeacherClassrooms();
      } catch (error) {
        console.error('Error fetching teacher classrooms:', error);
        return [] as ClassroomResponseDTO[];
      }
    },
  });
  
  // Get the selected classroom name
  const selectedClassroomName = classroomsData?.find(c => c.physicalId === selectedClassroom)?.name || 'Classroom';

  // Define interfaces for student data
  interface ClassroomStudentDTO {
    physicalId: string;
    firstName: string;
    lastName: string;
    email: string;
    profilePictureUrl?: string;
    verified?: boolean;
    isVerified?: boolean;
    joinedAt?: string;
    createdAt?: string;
    updatedAt?: string;
  }
  
  // Define the type for the student data response
  interface StudentDataResponse {
    students: ClassroomStudentDTO[];
    totalCount: number;
  }

  // Get classroom students using the actual classroom API
  const { data: studentsData, isLoading: isStudentsLoading, error: studentsError } = useQuery<StudentDataResponse>({
    queryKey: ['classroom-students', selectedClassroom],
    queryFn: async () => {
      try {
        if (!selectedClassroom) return { students: [], totalCount: 0 };
        
        // Use the classroom API's getClassroomStudents function
        const studentsResponse = await getClassroomStudents(selectedClassroom);
        
        // Format response to match expected structure
        return { 
          students: studentsResponse.students || [],
          totalCount: studentsResponse.students?.length || 0
        };
      } catch (error) {
        console.error('Error fetching classroom students:', error);
        return { students: [], totalCount: 0 };
      }
    },
    enabled: !!selectedClassroom,
  });
  
  // Get all student progress for the selected classroom
  const { data: allProgressData, isLoading: isProgressLoading } = useQuery<(StudentProgressItemDTO & { studentId: string })[]>({
    queryKey: ['classroom-progress', selectedClassroom, studentsData?.totalCount],
    queryFn: async () => {
      try {
        if (!selectedClassroom || !studentsData?.students.length) return [];
        
        // Log classroom information
        console.log(`Selected classroom for progress: ${selectedClassroom}`);
        
        // Only fetch progress for verified students to prevent 403 errors
        const progressPromises = studentsData.students
          .filter((student: ClassroomStudentDTO) => student.verified || student.isVerified) // Skip unverified students
          .map(async (student: ClassroomStudentDTO) => {
            console.log(`Fetching progress for student ${student.physicalId} in classroom ${selectedClassroom}`);
            // Pass the explicit classroom ID to ensure consistent access pattern
            const progress = await fetchStudentProgressItems(student.physicalId, selectedClassroom);
            // Add studentId to each progress item
            return progress.map(item => ({ ...item, studentId: student.physicalId }));
          });
        
        console.log(`Starting ${progressPromises.length} progress fetch operations for classroom ${selectedClassroom}`);
        const results = await Promise.allSettled(progressPromises);
        
        // Log any failures to help with debugging
        results.forEach((result, index) => {
          if (result.status === 'rejected') {
            console.error(`Failed to fetch progress for student at index ${index}:`, result.reason);
          }
        });
        
        // Combine all successful results
        const successfulResults = results
          .filter((result): result is PromiseFulfilledResult<(StudentProgressItemDTO & { studentId: string })[]> => 
            result.status === 'fulfilled')
          .flatMap(result => result.value);
          
        console.log(`Successfully fetched ${successfulResults.length} progress items from classroom ${selectedClassroom}`);
        return successfulResults;
      } catch (error) {
        console.error(`Error fetching progress data for classroom ${selectedClassroom}:`, error);
        return [];
      }
    },
    enabled: !!selectedClassroom && !!studentsData?.students.length,
  });
  
  // Get all badges for the selected classroom
  const { data: allBadgesData, isLoading: isBadgesLoading } = useQuery<BadgeDTO[]>({
    queryKey: ['classroom-badges', selectedClassroom, studentsData?.totalCount],
    queryFn: async () => {
      try {
        if (!selectedClassroom || !studentsData?.students.length) return [];
        
        // Only fetch badges for verified students to prevent 403 errors
        const badgePromises = studentsData.students
          .filter((student: ClassroomStudentDTO) => student.verified || student.isVerified) // Skip unverified students
          .map((student: ClassroomStudentDTO) => getStudentBadges(student.physicalId));
        
        const results = await Promise.allSettled(badgePromises);
        
        // Combine all successful results
        return results
          .filter((result: PromiseSettledResult<BadgeDTO[]>): result is PromiseFulfilledResult<BadgeDTO[]> => 
            result.status === 'fulfilled')
          .flatMap((result: PromiseFulfilledResult<BadgeDTO[]>) => result.value);
      } catch (error) {
        console.error('Error fetching classroom badges:', error);
        return [];
      }
    },
    enabled: !!selectedClassroom && !!studentsData?.students.length,
  });

  // Filter students based on search term - with proper null checking
  const filteredStudents = studentsData && studentsData.students
    ? studentsData.students
        .filter((student: ClassroomStudentDTO) => 
          student.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          student.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          student.email?.toLowerCase().includes(searchTerm.toLowerCase())
        )
        .sort((a: ClassroomStudentDTO, b: ClassroomStudentDTO) => {
          // First, prioritize verified students
          const aVerified = a.verified || a.isVerified || false;
          const bVerified = b.verified || b.isVerified || false;
          
          if (aVerified !== bVerified) {
            return aVerified ? -1 : 1; // Verified students come first
          }
          
          // Then sort alphabetically by last name
          const lastNameA = a.lastName?.toLowerCase() || '';
          const lastNameB = b.lastName?.toLowerCase() || '';
          return lastNameA.localeCompare(lastNameB);
        })
    : [];

  // Handle view student details
  const handleViewStudent = (studentId: string) => {
    // Pass the selected classroom ID as a query parameter
    router.push(`/student-progress/${studentId}?classroomId=${selectedClassroom}`);
    console.log(`Navigating to student ${studentId} with classroom ${selectedClassroom}`);
  };
  
  // Handle export report
  const handleExportReport = () => {
    if (!allProgressData || !studentsData?.students || studentsData.students.length === 0) {
      alert('No data available to export');
      return;
    }
    
    try {
      // Convert progress data to the format expected by the export function
      const exportableData = allProgressData.map((progress: StudentProgressItemDTO & { studentId: string }) => ({
        physicalId: `progress-${progress.taskId}-${progress.studentId}`,
        studentId: progress.studentId,
        taskId: progress.taskId,
        taskName: progress.taskName,
        taskType: progress.taskType,
        scoreValue: progress.score || 0,
        maxScore: progress.maxScore || 100,
        isCompleted: progress.completed,
        createdAt: progress.completedAt || new Date().toISOString(),
        updatedAt: progress.completedAt || new Date().toISOString()
      }));
      
      // Export the report
      exportStudentScoresReport(
        exportableData,
        studentsData.students,
        selectedClassroomName
      );
    } catch (error) {
      console.error('Error exporting report:', error);
      alert('Error generating report. Please try again.');
    }
  };

  return (
    <div className="relative min-h-screen overflow-hidden bg-gradient-to-br from-background to-muted/20">
      {/* Animated background blobs */}
      <div className="pointer-events-none absolute inset-0 overflow-hidden">
        <motion.div className="absolute left-6 top-6 h-32 w-32 rounded-full bg-primary/5" animate={prefersReducedMotion ? undefined : { scale: [1, 1.05, 1] }} transition={{ duration: 6, repeat: Infinity }} />
        <motion.div className="absolute right-8 top-10 h-24 w-24 rounded-full bg-secondary/5" animate={prefersReducedMotion ? undefined : { y: [0, -10, 0] }} transition={{ duration: 5, repeat: Infinity }} />
        <motion.div className="absolute bottom-8 left-8 h-40 w-40 rounded-full bg-primary/5" animate={prefersReducedMotion ? undefined : { scale: [1, 1.08, 1] }} transition={{ duration: 7, repeat: Infinity }} />
        <motion.div className="absolute bottom-10 right-12 h-28 w-28 rounded-full bg-secondary/5" animate={prefersReducedMotion ? undefined : { y: [0, -12, 0] }} transition={{ duration: 6, repeat: Infinity }} />
      </div>

      <div className="flex min-h-screen relative z-10">
        <Sidebar className="w-64 flex-shrink-0" />

        <div className="flex-1 md:ml-64">
          <AuthNavbar />
          
          <main className="p-6">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              className="mb-8 flex flex-col space-y-4 md:flex-row md:items-center md:justify-between md:space-y-0"
            >
              <div className="flex items-center gap-6">
                <div className="relative">
                  <div className="absolute -inset-4 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-2xl" />
                  <motion.div
                    animate={prefersReducedMotion ? undefined : { y: [0, -8, 0] }}
                    transition={{ duration: 2, repeat: Infinity }}
                    className="relative"
                  >
                    <Image
                      src="/images/mascots/mascot_celebrate.png"
                      alt="Stathis Study Mascot"
                      width={80}
                      height={80}
                      className="drop-shadow-lg"
                    />
                  </motion.div>
                </div>
                
                <div>
                  <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                    Student Progress
                  </h1>
                  <p className="text-muted-foreground mt-2">View and track the progress of all students</p>
                </div>
              </div>
              
              <div className="flex items-center gap-3">
                <Button 
                  onClick={handleExportReport}
                  disabled={isStudentsLoading || isProgressLoading || !selectedClassroom || !allProgressData?.length}
                  className="rounded-xl bg-gradient-to-r from-primary to-secondary hover:opacity-90 shadow-lg hover:shadow-xl transition-all duration-300 hover:scale-105 text-white"
                >
                  <Download className="mr-2 h-4 w-4" />
                  Export Report
                </Button>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
              className="grid gap-8"
            >
              {/* Filters and search */}
              <Card className="overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg">
                <CardHeader>
                  <CardTitle className="text-xl">Filters</CardTitle>
                  <CardDescription>Filter and search for specific students</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-col md:flex-row gap-4 items-start md:items-center">
                    <div className="w-full md:w-1/3">
                      <Select 
                        value={selectedClassroom} 
                        onValueChange={setSelectedClassroom}
                      >
                        <SelectTrigger className="h-12 rounded-xl border-border/30 bg-background/60 backdrop-blur-sm">
                          <SelectValue placeholder="Select classroom" />
                        </SelectTrigger>
                      <SelectContent>
                        {isClassroomsLoading ? (
                          <SelectItem value="loading" disabled>Loading classrooms...</SelectItem>
                        ) : classroomsData && classroomsData.length > 0 ? (
                          classroomsData.map(classroom => (
                            <SelectItem key={classroom.physicalId} value={classroom.physicalId}>
                              {classroom.name}
                            </SelectItem>
                          ))
                        ) : (
                          <SelectItem value="no-classrooms" disabled>No classrooms found</SelectItem>
                        )}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="w-full md:w-2/3 relative">
                      <Search className="absolute left-3.5 top-3.5 h-4 w-4 text-muted-foreground" />
                      <Input
                        type="search"
                        placeholder="Search students..."
                        className="h-12 pl-10 rounded-xl border-border/30 bg-background/60 backdrop-blur-sm"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                      />
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Students list */}
              <Card className="overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg">
                <CardHeader>
                  <div className="flex justify-between items-center">
                    <div>
                      <CardTitle className="text-xl">Students</CardTitle>
                      <CardDescription>
                        {selectedClassroom 
                          ? `Showing students for selected classroom` 
                          : "Select a classroom to see students"}
                      </CardDescription>
                    </div>
                    <Users className="h-6 w-6 text-muted-foreground" />
                  </div>
                </CardHeader>
                <CardContent>
                {!selectedClassroom ? (
                  <div className="text-center py-6 text-muted-foreground">
                    Please select a classroom to view students
                  </div>
                ) : isStudentsLoading ? (
                  <div className="space-y-2">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <div key={index} className="flex items-center space-x-4">
                        <Skeleton className="h-12 w-12 rounded-full" />
                        <div className="space-y-2">
                          <Skeleton className="h-4 w-[250px]" />
                          <Skeleton className="h-4 w-[200px]" />
                        </div>
                      </div>
                    ))}
                  </div>
                ) : studentsError ? (
                  <div className="text-center py-6 text-red-500">
                    Error loading students. Please try again.
                  </div>
                ) : filteredStudents.length === 0 ? (
                  <div className="text-center py-6 text-muted-foreground">
                    No students found in this classroom.
                  </div>
                ) : (
                  <>
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Name</TableHead>
                          <TableHead>Email</TableHead>
                          <TableHead>Avg. Score</TableHead>
                          <TableHead className="text-center">Tasks Completed</TableHead>
                          <TableHead className="text-right">Actions</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {filteredStudents.map((student: ClassroomStudentDTO) => {
                          // Calculate student statistics - filter by student ID
                          const studentProgress = allProgressData?.filter(item => 
                            item.studentId === student.physicalId
                          ) || [];

                          const completedTasks = studentProgress.filter(item => item.completed).length;
                          const totalTasks = studentProgress.length;
                          
                          const scoreValues = studentProgress
                            .filter(item => item.score !== null)
                            .map(item => item.score || 0);
                          
                          const avgScore = scoreValues.length > 0 
                            ? scoreValues.reduce((a, b) => a + b, 0) / scoreValues.length 
                            : 0;
                            
                          return (
                            <TableRow key={student.physicalId}>
                              <TableCell>
                                <div className="flex items-center gap-3">
                                  <Avatar className="h-8 w-8">
                                    <AvatarImage src={student.profilePictureUrl || ''} alt={`${student.firstName} ${student.lastName}`} />
                                    <AvatarFallback>{student.firstName?.[0]}{student.lastName?.[0]}</AvatarFallback>
                                  </Avatar>
                                  <div>
                                    <div className="font-medium">{student.lastName}, {student.firstName}</div>
                                    {student.verified || student.isVerified ? (
                                      <div className="text-xs text-green-600">Verified</div>
                                    ) : (
                                      <div className="text-xs text-amber-600">Pending verification</div>
                                    )}
                                  </div>
                                </div>
                              </TableCell>
                              <TableCell>{student.email}</TableCell>
                              <TableCell>
                                {isProgressLoading ? (
                                  <Skeleton className="h-4 w-16" />
                                ) : (
                                  <div>
                                    {scoreValues.length > 0 ? (
                                      <div className="font-medium">{Math.round(avgScore)}%</div>
                                    ) : (
                                      <div className="text-muted-foreground">0%</div>
                                    )}
                                    <Progress 
                                      value={avgScore} 
                                      max={100} 
                                      className="h-1.5 mt-1"
                                    />
                                  </div>
                                )}
                              </TableCell>
                              <TableCell className="text-center">
                                {isProgressLoading ? (
                                  <Skeleton className="h-4 w-8 mx-auto" />
                                ) : (
                                  <Badge variant={completedTasks > 0 ? "default" : "outline"}>
                                    {completedTasks}/{totalTasks}
                                  </Badge>
                                )}
                              </TableCell>
                              <TableCell className="text-right">
                                <Button 
                                  variant="ghost" 
                                  size="icon" 
                                  onClick={() => handleViewStudent(student.physicalId)}
                                >
                                  <Eye className="h-4 w-4" />
                                  <span className="sr-only">View</span>
                                </Button>
                              </TableCell>
                            </TableRow>
                          );
                        })}
                      </TableBody>
                    </Table>
                    <div className="mt-3 text-xs text-muted-foreground">
                      Showing {filteredStudents.length} of {studentsData?.totalCount || 0} students
                    </div>
                  </>
                )}
              </CardContent>
            </Card>

              {/* Summary Cards for classroom statistics */}
              {selectedClassroom && !isStudentsLoading && !isProgressLoading && (
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: 0.2 }}
                  className="grid grid-cols-1 md:grid-cols-3 gap-6"
                >
                  <Card className="overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300 group">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-3">
                      <CardTitle className="text-sm font-medium text-muted-foreground">
                        Total Students
                      </CardTitle>
                      <div className="p-2 rounded-full bg-primary/10 group-hover:bg-primary/20 transition-colors duration-200">
                        <Users className="h-4 w-4 text-primary" />
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                        {studentsData?.totalCount || 0}
                      </div>
                      <p className="text-xs text-muted-foreground mt-2">
                        Enrolled in selected classroom
                      </p>
                    </CardContent>
                  </Card>
                  <Card className="overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300 group">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-3">
                      <CardTitle className="text-sm font-medium text-muted-foreground">
                        Average Score
                      </CardTitle>
                      <div className="p-2 rounded-full bg-primary/10 group-hover:bg-primary/20 transition-colors duration-200">
                        <TrendingUp className="h-4 w-4 text-primary" />
                      </div>
                    </CardHeader>
                  <CardContent>
                    {isStudentsLoading || isProgressLoading ? (
                      <Skeleton className="h-9 w-16" />
                    ) : (
                      <>
                        <div className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                          {allProgressData?.length ? 
                            `${Math.round((allProgressData
                              .filter(progress => progress.score !== null)
                              .reduce((sum: number, progress: StudentProgressItemDTO) => sum + (progress.score || 0), 0) / 
                              Math.max(1, allProgressData.filter((p: StudentProgressItemDTO) => p.score !== null).length)) * 10) / 10}%` : 
                            '0%'}
                        </div>
                        <p className="text-xs text-muted-foreground mt-2">
                          {allProgressData?.length ? 
                            (() => {
                              const uniqueTasks = new Set(allProgressData.filter(p => p.score !== null).map(p => p.taskId));
                              return `Based on ${uniqueTasks.size} unique task${uniqueTasks.size === 1 ? '' : 's'}`;
                            })() : 
                            'No tasks available'}
                        </p>
                      </>
                    )}
                  </CardContent>
                </Card>
                  <Card className="overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300 group">
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-3">
                      <CardTitle className="text-sm font-medium text-muted-foreground">
                        Badges Awarded
                      </CardTitle>
                      <div className="p-2 rounded-full bg-primary/10 group-hover:bg-primary/20 transition-colors duration-200">
                        <Award className="h-4 w-4 text-primary" />
                      </div>
                    </CardHeader>
                    <CardContent>
                      {isStudentsLoading || isBadgesLoading ? (
                        <Skeleton className="h-9 w-16" />
                      ) : (
                        <>
                          <div className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                            {allBadgesData?.length || 0}
                          </div>
                          <p className="text-xs text-muted-foreground mt-2">
                            Across {studentsData ? studentsData.students.filter((s: ClassroomStudentDTO) => s.verified || s.isVerified).length : 0} student{studentsData && studentsData.students.filter((s: ClassroomStudentDTO) => s.verified || s.isVerified).length === 1 ? '' : 's'}
                          </p>
                        </>
                      )}
                    </CardContent>
                  </Card>
                </motion.div>
              )}
            </motion.div>
          </main>
        </div>
      </div>
    </div>
  );
}
