'use client';

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { 
  getStudentScores,
  getStudentBadges, 
  ScoreResponseDTO,
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
import { 
  Users, 
  Search, 
  ArrowUpDown, 
  Eye, 
  BarChart,
  ArrowRight,
  BookOpen,
  Award,
  Bell
} from 'lucide-react';
import { AuthNavbar } from '@/components/auth-navbar';

export default function StudentProgressPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedClassroom, setSelectedClassroom] = useState('');

  
  // Fetch teacher's classrooms from API
  const { data: classroomsData, isLoading: isClassroomsLoading } = useQuery({
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

  // Get classroom students using the actual classroom API
  const { data: studentsData, isLoading: isStudentsLoading, error: studentsError } = useQuery({
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
  
  // Get all student scores for the selected classroom
  const { data: allScoresData, isLoading: isScoresLoading } = useQuery({
    queryKey: ['classroom-scores', selectedClassroom, studentsData?.students],
    queryFn: async () => {
      try {
        if (!selectedClassroom || !studentsData?.students.length) return [];
        
        // Only fetch scores for verified students to prevent 403 errors
        const scorePromises = studentsData.students
          .filter(student => student.verified) // Skip unverified students
          .map(student => getStudentScores(student.physicalId));
        
        const results = await Promise.allSettled(scorePromises);
        
        // Combine all successful results
        return results
          .filter((result): result is PromiseFulfilledResult<ScoreResponseDTO[]> => 
            result.status === 'fulfilled')
          .flatMap(result => result.value);
      } catch (error) {
        console.error('Error fetching classroom scores:', error);
        return [];
      }
    },
    enabled: !!selectedClassroom && !!studentsData?.students.length,
  });
  
  // Get all badges for the selected classroom
  const { data: allBadgesData, isLoading: isBadgesLoading } = useQuery({
    queryKey: ['classroom-badges', selectedClassroom, studentsData?.students],
    queryFn: async () => {
      try {
        if (!selectedClassroom || !studentsData?.students.length) return [];
        
        // Only fetch badges for verified students to prevent 403 errors
        const badgePromises = studentsData.students
          .filter(student => student.verified) // Skip unverified students
          .map(student => getStudentBadges(student.physicalId));
        
        const results = await Promise.allSettled(badgePromises);
        
        // Combine all successful results
        return results
          .filter((result): result is PromiseFulfilledResult<BadgeDTO[]> => 
            result.status === 'fulfilled')
          .flatMap(result => result.value);
      } catch (error) {
        console.error('Error fetching classroom badges:', error);
        return [];
      }
    },
    enabled: !!selectedClassroom && !!studentsData?.students.length,
  });

  // Filter students based on search term - with proper null checking
  const filteredStudents = studentsData && studentsData.students
    ? studentsData.students.filter(student => 
        student.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        student.lastName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        student.email?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    : [];

  // Handle view student details
  const handleViewStudent = (studentId: string) => {
    router.push(`/student-progress/${studentId}`);
  };
  
  // Handle export report
  const handleExportReport = () => {
    if (!allScoresData || !studentsData?.students) {
      alert('No data available to export');
      return;
    }
    
    try {
      // Export the report
      exportStudentScoresReport(
        allScoresData,
        studentsData.students,
        selectedClassroomName
      );
    } catch (error) {
      console.error('Error exporting report:', error);
      alert('Error generating report. Please try again.');
    }
  };

  return (
    <div className="flex min-h-screen">
      <Sidebar className="w-64 flex-shrink-0" />

      <div className="flex-1">
        <AuthNavbar />
        
        <main className="p-6">
          <div className="mb-6 flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0">
            <div>
              <h1 className="text-2xl font-bold tracking-tight">Student Progress</h1>
              <p className="text-muted-foreground mt-1">View and track the progress of all students</p>
            </div>
            <div className="flex items-center gap-2">
              <Button 
                variant="outline" 
                size="sm"
                onClick={handleExportReport}
                disabled={isStudentsLoading || isScoresLoading || !selectedClassroom || !allScoresData?.length}
              >
                <BarChart className="mr-2 h-4 w-4" />
                Export Report
              </Button>
            </div>
          </div>

          <div className="grid gap-8">
        {/* Filters and search */}
        <Card>
          <CardHeader>
            <CardTitle>Filters</CardTitle>
            <CardDescription>Filter and search for specific students</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col md:flex-row gap-4 items-start md:items-center">
              <div className="w-full md:w-1/3">
                <Select 
                  value={selectedClassroom} 
                  onValueChange={setSelectedClassroom}
                >
                  <SelectTrigger>
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
                <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
                <Input
                  type="search"
                  placeholder="Search students..."
                  className="pl-8"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Students list */}
        <Card>
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
                No students found for this search criteria
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Email</TableHead>
                    <TableHead className="text-center">Avg. Score</TableHead>
                    <TableHead className="text-center">Tasks Completed</TableHead>
                    <TableHead className="text-center">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {filteredStudents.map((student) => {
                    // Get student's scores if they exist
                    const studentScores = allScoresData?.filter((score: ScoreResponseDTO) => score.studentId === student.physicalId) || [];
                    
                    // Calculate average score if student is verified
                    const avgScore = student.verified && studentScores.length > 0 ?
                      Math.round(studentScores.reduce((sum: number, score: ScoreResponseDTO) => sum + score.scoreValue, 0) / studentScores.length) : null;
                    
                    // Count completed tasks if student is verified
                    const completedTasks = student.verified ? studentScores.filter((score: ScoreResponseDTO) => score.isCompleted).length : null;
                    const totalTasks = studentScores.length;
                    
                    return (
                      <TableRow key={student.physicalId}>
                        <TableCell className="font-medium">
                          {/* Show (Unverified) label for unverified students */}
                          {!student.verified && <span className="text-red-500 font-normal">(Unverified) </span>}
                          {student.firstName} {student.lastName}
                        </TableCell>
                        <TableCell>{student.email}</TableCell>
                        <TableCell className="text-center">
                          <div className="flex items-center justify-center">
                            {student.verified ? (
                              avgScore !== null ? (
                                <span className={`font-medium ${avgScore >= 70 ? 'text-green-600' : 'text-amber-600'}`}>
                                  {avgScore}%
                                </span>
                              ) : 'No data'
                            ) : (
                              <span className="text-muted-foreground">-</span>
                            )}
                          </div>
                        </TableCell>
                        <TableCell className="text-center">
                          {student.verified ? (
                            totalTasks > 0 ? `${completedTasks || 0}/${totalTasks}` : 'No tasks'
                          ) : (
                            <span className="text-muted-foreground">-</span>
                          )}
                        </TableCell>
                        <TableCell className="text-center">
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => handleViewStudent(student.physicalId)}
                          >
                            <Eye className="h-4 w-4 mr-2" />
                            View
                          </Button>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            )}
          </CardContent>
          <CardFooter className="border-t px-6 py-4">
            <div className="flex items-center justify-between w-full">
              <p className="text-sm text-muted-foreground">
                Showing {filteredStudents.length} of {studentsData?.totalCount || 0} students
              </p>
              <div className="flex items-center gap-2">
                <Button variant="outline" size="sm" disabled>
                  Previous
                </Button>
                <Button variant="outline" size="sm" disabled>
                  Next
                </Button>
              </div>
            </div>
          </CardFooter>
        </Card>

        {/* Overview statistics */}
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                Total Students
              </CardTitle>
              <Users className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">
                {selectedClassroom ? studentsData?.totalCount || 0 : '-'}
              </div>
              <p className="text-xs text-muted-foreground">
                Enrolled in selected classroom
              </p>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                Average Score
              </CardTitle>
              <BarChart className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              {isStudentsLoading || isScoresLoading ? (
                <Skeleton className="h-9 w-16" />
              ) : (
                <>
                  <div className="text-2xl font-bold">
                    {selectedClassroom ? 
                      allScoresData?.length ? 
                        `${Math.round((allScoresData.reduce((sum, score) => 
                          sum + (score.scoreValue || 0), 0) / allScoresData.length) * 10) / 10}%` : 
                        '0%' : 
                      '-'}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {allScoresData?.length ? 
                      `Based on ${allScoresData.length} score${allScoresData.length === 1 ? '' : 's'}` : 
                      'No scores available'}
                  </p>
                </>
              )}
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                Badges Awarded
              </CardTitle>
              <Award className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              {isStudentsLoading || isBadgesLoading ? (
                <Skeleton className="h-9 w-16" />
              ) : (
                <>
                  <div className="text-2xl font-bold">
                    {selectedClassroom ? 
                      allBadgesData?.length ? 
                        allBadgesData.length.toString() : 
                        '0' : 
                      '-'}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    {studentsData?.students.length ? 
                      `Across ${studentsData.students.length} student${studentsData.students.length === 1 ? '' : 's'}` : 
                      'No badges awarded yet'}
                  </p>
                </>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
          </main>
      </div>
    </div>
  );
}
