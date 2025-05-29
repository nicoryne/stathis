'use client';

import React, { useState, useEffect, use } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { Loader2, ArrowLeft, Users, Calendar, ClipboardCheck, Book, FileText, Bell, Activity, PlusCircle, Settings2, ChevronRight, ExternalLink, Clock, Award, Check, ChevronDown, ClipboardList, MoreHorizontal, Plus, Settings, UserPlus, CheckCircle } from "lucide-react";
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Separator } from '@/components/ui/separator';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { getClassroomStudents, StudentDTO } from "@/services/api-classroom";
import { getClassroomById, deleteClassroom, activateClassroom, deactivateClassroom, verifyClassroomStudent } from '@/services/api-classroom-client';
import { getClassroomTasks, TaskResponseDTO } from '@/services/tasks/api-task-client';
import { getCurrentUserEmail, getCurrentUserRole } from '@/lib/utils/jwt';
import { signOut } from '@/services/api-auth-client';
import { TemplateCreationTab } from '@/components/templates/template-creation-tab';
import { TaskCreationTab } from '@/components/tasks/task-creation-tab';
import { TaskScoresTab } from '@/components/scores/task-scores-tab';
import { ApiDebugger } from '@/components/debug/api-test';
import { Sidebar } from '@/components/dashboard/sidebar';
import { AuthNavbar } from '@/components/auth-navbar';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import ThemeSwitcher from '@/components/theme-switcher';
import { Progress } from '@/components/ui/progress';

// We'll use the standard Dialog component since AlertDialog isn't available

// StatCard Component for reuse
interface StatCardProps {
  title: string;
  value: string | number;
  description: string | React.ReactNode;
  icon: React.ReactNode;
  className?: string;
}

const StatCard = ({ title, value, description, icon, className = '' }: StatCardProps) => (
  <Card className={`overflow-hidden ${className}`}>
    <CardHeader className="pb-2">
      <div className="flex items-center justify-between">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        {icon}
      </div>
    </CardHeader>
    <CardContent>
      <div className="text-2xl font-bold">{value}</div>
      <p className="text-xs text-muted-foreground mt-1">{description}</p>
    </CardContent>
  </Card>
);

export default function ClassroomDetailPage() {
  const router = useRouter();
  const params = useParams<{ physicalId: string }>();
  const physicalId = params.physicalId;
  const queryClient = useQueryClient();
  const userEmail = getCurrentUserEmail();
  const userRole = getCurrentUserRole();
  
  // State for dialog visibility
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  
  // User details for the header
  const [userDetails, setUserDetails] = useState({
    first_name: '',
    last_name: '',
    email: userEmail || ''
  });
  
  // Tab state for the main content - initialize from URL hash if present
  const [activeTab, setActiveTab] = useState(() => {
    // Check if we're in a browser environment
    if (typeof window !== 'undefined') {
      // Get tab from URL hash if it exists
      const hash = window.location.hash.replace('#', '');
      if (['overview', 'students', 'tasks', 'scores', 'templates', 'settings'].includes(hash)) {
        return hash;
      }
    }
    return 'overview';
  });
  
  // Update URL hash when tab changes
  useEffect(() => {
    if (typeof window !== 'undefined') {
      window.location.hash = activeTab;
    }
  }, [activeTab]);
  
  // Ensure we have a valid user email before proceeding
  useEffect(() => {
    if (!userEmail && typeof window !== 'undefined') {
      // Redirect to login if we don't have a user email
      router.push('/login');
      toast.error('User information not found. Please log in again.');
    }
  }, [userEmail, router]);
  
  // Mutation for deleting a classroom
  const deleteClassroomMutation = useMutation({
    mutationFn: () => deleteClassroom(physicalId),
    onSuccess: () => {
      toast.success('Classroom deleted successfully');
      router.push('/classroom');
    },
    onError: (error) => {
      toast.error(`Failed to delete classroom: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });
  
  // Mutation for activating a classroom
  const activateClassroomMutation = useMutation({
    mutationFn: () => activateClassroom(physicalId),
    onSuccess: () => {
      toast.success('Classroom activated successfully');
      queryClient.invalidateQueries({ queryKey: ['classroom', physicalId] });
    },
    onError: (error) => {
      toast.error(`Failed to activate classroom: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });
  
  // Mutation for deactivating a classroom
  const deactivateClassroomMutation = useMutation({
    mutationFn: () => deactivateClassroom(physicalId),
    onSuccess: () => {
      toast.success('Classroom deactivated successfully');
      queryClient.invalidateQueries({ queryKey: ['classroom', physicalId] });
    },
    onError: (error) => {
      toast.error(`Failed to deactivate classroom: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });
  
  // Handle sign out function
  const handleSignOut = async () => {
    await signOut();
    router.push('/login');
  };
  
  // Add student mutation
  const [addingStudent, setAddingStudent] = useState(false);

  const addStudentMutation = useMutation({
    mutationFn: async (email: string) => {
      // TODO: Call API to add student
      console.log('Adding student with email:', email);
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API call
      return { success: true };
    },
    onSuccess: () => {
      toast.success('Student invitation sent!');
      setAddingStudent(false);
      // Refresh students list
      queryClient.invalidateQueries({ queryKey: ['classroom-students', physicalId] });
    },
    onError: () => {
      toast.error('Failed to invite student');
    }
  });
  
  // Verify student mutation
  const verifyStudentMutation = useMutation({
    mutationFn: async (studentId: string) => {
      // Call API to verify student
      console.log('Verifying student:', studentId);
      
      // Use the proper API client function that handles auth correctly
      return await verifyClassroomStudent(physicalId, studentId);
    },
    onSuccess: () => {
      toast.success('Student verified successfully');
      // Refresh students list
      queryClient.invalidateQueries({ queryKey: ['classroom-students', physicalId] });
    },
    onError: (error) => {
      console.error('Error in verify mutation:', error);
      toast.error('Failed to verify student. Make sure you have teacher permissions.');
    }
  });
  
  // Fetch classroom details
  const { 
    data: classroom, 
    isLoading: isLoadingClassroom, 
    isError: isErrorClassroom,
    error: classroomError
  } = useQuery({
    queryKey: ['classroom', physicalId],
    queryFn: () => getClassroomById(physicalId),
    enabled: !!userEmail && !!physicalId,
    retry: 1,
  });
  
  // Debug classroom query result
  useEffect(() => {
    if (classroom) {
      console.log('CLASSROOM DATA:', classroom);
      console.log('Student count from classroom:', classroom.studentCount);
    }
  }, [classroom]);

  // Fetch classroom students (for all users, not just teachers)
  const { 
    data: students, 
    isLoading: isLoadingStudents,
    error: studentsError,
    refetch: refetchStudents
  } = useQuery({
    queryKey: ['classroom-students', physicalId],
    queryFn: async () => {
      console.log('DIRECT API CALL: Fetching students for classroom:', physicalId);
      try {
        // Make a direct fetch to the API for debugging
        const apiUrl = `https://stathis.onrender.com/api/classrooms/${physicalId}/students`;
        console.log('DIRECT API CALL: URL:', apiUrl);
        
        // Get the auth token
        const token = localStorage.getItem('auth_token');
        
        // Make a direct fetch with proper headers
        const response = await fetch(apiUrl, {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });
        
        // Log the raw response
        console.log('DIRECT API CALL: Status:', response.status);
        
        // Try to parse JSON
        let data;
        try {
          data = await response.json();
          console.log('DIRECT API CALL: Parsed data:', data);
          
          // If we get valid data from direct fetch, use it
          if (data) {
            // Create a proper student list structure if needed
            if (Array.isArray(data)) {
              return { students: data };
            } else if (typeof data === 'object' && data !== null) {
              if (Array.isArray(data.students)) {
                return data;
              } else {
                // Try to extract any students property or create empty array
                return { students: data.students || [] };
              }
            }
          }
          
          // Fallback to empty array
          return { students: [] };
        } catch (parseError) {
          console.error('DIRECT API CALL: Failed to parse JSON:', parseError);
          const text = await response.text();
          console.log('DIRECT API CALL: Raw response:', text);
          return { students: [] };
        }
      } catch (error) {
        console.error('DIRECT API CALL: Error:', error);
        return { students: [] };
      }
    },
    // Enable for all users, not just teachers
    enabled: !!userEmail && !!physicalId,
  });

  // Fetch tasks for this classroom
  const { data: tasks, isLoading: isLoadingTasks } = useQuery({
    queryKey: ['classroom-tasks', physicalId],
    queryFn: () => getClassroomTasks(physicalId),
    enabled: !!physicalId,
    staleTime: 1000 * 60 * 5, // 5 minutes
  });

  // Render basic layout for error and loading states
  const renderErrorOrLoadingState = (content: React.ReactNode) => (
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
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleSignOut}>Sign out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>
        <main className="p-6 flex items-center justify-center min-h-[calc(100vh-4rem)]">
          {content}
        </main>
      </div>
    </div>
  );

  // Handle error cases
  if (isErrorClassroom) {
    return renderErrorOrLoadingState(
      <div className="flex flex-col items-center justify-center text-center max-w-md">
        <div className="text-destructive mb-4 text-5xl">
          <ClipboardCheck className="h-12 w-12 mx-auto" />
        </div>
        <h1 className="text-2xl font-bold mb-2">Error Loading Classroom</h1>
        <p className="text-muted-foreground mb-6">
          {classroomError instanceof Error ? classroomError.message : 'Failed to load classroom details'}
        </p>
        <Button onClick={() => router.push('/classroom')} className="mt-2">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Classrooms
        </Button>
      </div>
    );
  }

  // Show loading state
  if (isLoadingClassroom) {
    return renderErrorOrLoadingState(
      <div className="flex flex-col items-center justify-center text-center">
        <Loader2 className="h-12 w-12 animate-spin mb-4 text-primary" />
        <p className="text-muted-foreground text-lg">Loading classroom details...</p>
      </div>
    );
  }

  if (!classroom) {
    return renderErrorOrLoadingState(
      <div className="flex flex-col items-center justify-center text-center max-w-md">
        <div className="text-muted-foreground mb-4 text-5xl">
          <Book className="h-12 w-12 mx-auto" />
        </div>
        <h1 className="text-2xl font-bold mb-2">Classroom Not Found</h1>
        <p className="text-muted-foreground mb-6">
          The classroom you're looking for doesn't exist or you don't have permission to view it.
        </p>
        <Button onClick={() => router.push('/classroom')} className="mt-2">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Classrooms
        </Button>
      </div>
    );
  }

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
                <DropdownMenuSeparator />
                <DropdownMenuItem onClick={handleSignOut}>Sign out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>
        
        <main className="p-6">
          {/* Breadcrumb and classroom status */}
          <div className="flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0 mb-6">
            <div className="flex items-center space-x-1 text-sm text-muted-foreground">
              <Button 
                onClick={() => router.push('/classroom')} 
                variant="ghost"
                size="sm"
                className="hover:bg-transparent p-0 hover:text-primary"
              >
                Classrooms
              </Button>
              <ChevronRight className="h-4 w-4" />
              <span className="font-medium text-foreground">{classroom.name}</span>
              <Badge variant={classroom.active ? "default" : "secondary"} className="ml-2">
                {classroom.active ? 'Active' : 'Inactive'}
              </Badge>
            </div>
          </div>
          
          {/* Classroom header */}
          <div className="mb-6">
            <h1 className="text-2xl font-bold tracking-tight">{classroom.name}</h1>
            <p className="text-muted-foreground mt-1">{classroom.description}</p>
          </div>
          
          {/* Classroom stats */}
          <div className="grid gap-6 mb-6 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
              title="Total Students"
              value={classroom?.studentCount || 0}
              description={
                <>
                  <span>Students enrolled</span>
                  {students && students.students && (
                    <span className="block text-xs text-muted-foreground font-medium mt-1">
                      <span className="text-red-500">{students.students.filter((student: StudentDTO) => !student.verified).length}</span> unverified student/s
                    </span>
                  )}
                </>
              }
              icon={<Users className="h-4 w-4 text-muted-foreground" />}
            />
            <StatCard
              title="Completion Rate"
              value={(() => {
                // Calculate completion rate based on task status
                if (!tasks || tasks.length === 0) return '0%';
                
                // Define criteria for a completed task
                // For this implementation, we'll consider tasks that are started but not active as completed
                // This is an assumption that may need adjustment based on your actual business logic
                const completedTasks = tasks.filter(task => 
                  task.started === true && task.active === false
                ).length;
                
                // Calculate percentage
                const percentage = Math.round((completedTasks / tasks.length) * 100);
                return `${percentage}%`;
              })()}
              description={`${tasks ? tasks.filter(task => 
                task.started === true && task.active === false
              ).length : 0}/${tasks?.length || 0} tasks completed`}
              icon={<ClipboardCheck className="h-4 w-4 text-muted-foreground" />}
            />
            <StatCard
              title="Total Tasks"
              value={tasks?.length || 0}
              description="Assigned tasks"
              icon={<ClipboardList className="h-4 w-4 text-muted-foreground" />}
            />
            <StatCard
              title="Classroom Code"
              value={classroom.classroomCode || 'N/A'}
              description="Share with students to join"
              icon={<UserPlus className="h-4 w-4 text-muted-foreground" />}
            />
          </div>
          
          {/* Tabs for different classroom functions */}
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="w-full md:w-auto inline-flex h-10 items-center justify-center rounded-md bg-muted p-1 text-muted-foreground mb-6">
              <TabsTrigger value="overview" className="rounded-sm px-3 py-1.5 text-sm font-medium transition-all">Overview</TabsTrigger>
              <TabsTrigger value="students" className="rounded-sm px-3 py-1.5 text-sm font-medium transition-all">Students</TabsTrigger>
              <TabsTrigger value="tasks" className="rounded-sm px-3 py-1.5 text-sm font-medium transition-all">Tasks</TabsTrigger>
              {userRole === 'TEACHER' && (
                <TabsTrigger value="scores" className="rounded-sm px-3 py-1.5 text-sm font-medium transition-all">Scores</TabsTrigger>
              )}
              {userRole === 'TEACHER' && (
                <TabsTrigger value="templates" className="rounded-sm px-3 py-1.5 text-sm font-medium transition-all">Templates</TabsTrigger>
              )}
              {userRole === 'TEACHER' && (
                <TabsTrigger value="settings" className="rounded-sm px-3 py-1.5 text-sm font-medium transition-all">Settings</TabsTrigger>
              )}
            </TabsList>
            
            <TabsContent value="overview" className="space-y-4 mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>Classroom Details</CardTitle>
                  <CardDescription>
                    Complete information about this classroom
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <h3 className="font-medium text-sm text-muted-foreground">Name</h3>
                      <p>{classroom.name}</p>
                    </div>
                    <div>
                      <h3 className="font-medium text-sm text-muted-foreground">Teacher</h3>
                      <p>{classroom.teacherName || 'Not assigned'}</p>
                    </div>
                    <div>
                      <h3 className="font-medium text-sm text-muted-foreground">Created At</h3>
                      <p>{new Date(classroom.createdAt).toLocaleString()}</p>
                    </div>
                    <div>
                      <h3 className="font-medium text-sm text-muted-foreground">Last Updated</h3>
                      <p>{new Date(classroom.updatedAt).toLocaleString()}</p>
                    </div>
                    <div className="md:col-span-2">
                      <h3 className="font-medium text-sm text-muted-foreground">Description</h3>
                      <p>{classroom.description}</p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="students" className="space-y-4 mt-6">
              
              <Card>
                <CardHeader>
                  <CardTitle>Students</CardTitle>
                  <CardDescription>
                    Students enrolled in this classroom
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {isLoadingStudents ? (
                    <div className="flex justify-center py-8">
                      <Loader2 className="h-6 w-6 animate-spin" />
                    </div>
                  ) : !students || !students.students || students.students.length === 0 ? (
                    <p className="text-center py-8 text-muted-foreground">
                      No students enrolled in this classroom yet.
                    </p>
                  ) : (
                    <div className="space-y-4">
                      {/* Grouping students by verification status */}
                      <div>
                        <h3 className="text-sm font-medium mb-2">Verified Students</h3>
                        {students.students.filter((student: StudentDTO) => student.verified).length === 0 ? (
                          <p className="text-sm text-muted-foreground py-2">No verified students yet.</p>
                        ) : (
                          <div className="divide-y rounded-md border">
                            {students.students
                              .filter((student: StudentDTO) => student.verified)
                              .map((student: StudentDTO) => (
                                <div key={student.physicalId} className="py-3 px-4 flex justify-between items-center">
                                  <div>
                                    <p className="font-medium">{student.firstName} {student.lastName}</p>
                                    <p className="text-sm text-muted-foreground">{student.email}</p>
                                  </div>
                                  <Badge variant="default">Verified</Badge>
                                </div>
                              ))}
                          </div>
                        )}
                      </div>
                      
                      <div>
                        <h3 className="text-sm font-medium mb-2">Pending Verification</h3>
                        {students.students.filter((student: StudentDTO) => !student.verified).length === 0 ? (
                          <p className="text-sm text-muted-foreground py-2">No pending students.</p>
                        ) : (
                          <div className="divide-y rounded-md border">
                            {students.students
                              .filter((student: StudentDTO) => !student.verified)
                              .map((student: StudentDTO) => (
                                <div key={student.physicalId} className="py-3 px-4 flex justify-between items-center">
                                  <div>
                                    <p className="font-medium">{student.firstName} {student.lastName}</p>
                                    <p className="text-sm text-muted-foreground">{student.email}</p>
                                  </div>
                                  <div className="flex items-center gap-2">
                                    {/* Always show verify button regardless of role for testing */}
                                    <Button
                                      size="sm"
                                      variant="default"
                                      onClick={() => {
                                        // Use the student's physical ID as expected by the backend
                                        console.log('Verifying student with physicalId:', student.physicalId);
                                        verifyStudentMutation.mutate(student.physicalId);
                                      }}
                                      disabled={verifyStudentMutation.isPending}
                                    >
                                      {verifyStudentMutation.isPending ? (
                                        <>
                                          <Loader2 className="mr-2 h-3 w-3 animate-spin" />
                                          Verifying...
                                        </>
                                      ) : (
                                        <>
                                          <CheckCircle className="mr-2 h-3 w-3" />
                                          Verify
                                        </>
                                      )}
                                    </Button>
                                    <Badge variant="outline">Pending</Badge>
                                  </div>
                                </div>
                              ))}
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="tasks" className="space-y-4 mt-6">
              <TaskCreationTab classroomId={physicalId} />
            </TabsContent>

            <TabsContent value="scores" className="space-y-4 mt-6">
              {/* API Debugger for testing */}
              <ApiDebugger />
              
              <div className="grid gap-6 mb-6">
                <Card>
                  <CardHeader>
                    <div className="flex justify-between items-center">
                      <div>
                        <CardTitle className="text-xl">Student Scores</CardTitle>
                        <CardDescription>
                          View and manage scores for tasks in this classroom
                        </CardDescription>
                      </div>
                      <Award className="h-6 w-6 text-muted-foreground" />
                    </div>
                  </CardHeader>
                  <CardContent>
                    {isLoadingTasks ? (
                      <div className="flex justify-center items-center py-12">
                        <Loader2 className="h-8 w-8 animate-spin text-primary" />
                        <span className="ml-2">Loading tasks...</span>
                      </div>
                    ) : tasks && tasks.length > 0 ? (
                      <Tabs defaultValue={tasks[0].physicalId} className="w-full">
                        <TabsList className="mb-4 w-full overflow-x-auto flex-nowrap justify-start">
                          {tasks.map((task: TaskResponseDTO) => (
                            <TabsTrigger key={task.physicalId} value={task.physicalId} className="whitespace-nowrap">
                              {task.name}
                            </TabsTrigger>
                          ))}
                        </TabsList>
                        {tasks.map((task: TaskResponseDTO) => (
                          <TabsContent key={task.physicalId} value={task.physicalId}>
                            <TaskScoresTab 
                              taskId={task.physicalId} 
                              taskType={
                                task.exerciseTemplateId ? 'EXERCISE' : 
                                task.quizTemplateId ? 'QUIZ' : 
                                task.lessonTemplateId ? 'LESSON' : undefined
                              }
                              templateId={
                                task.exerciseTemplateId || 
                                task.quizTemplateId || 
                                task.lessonTemplateId
                              }
                            />
                          </TabsContent>
                        ))}
                      </Tabs>
                    ) : (
                      <div className="text-center py-12">
                        <div className="flex flex-col items-center justify-center text-center">
                          <FileText className="h-12 w-12 text-muted-foreground mb-4" />
                          <h3 className="text-lg font-medium">No tasks found</h3>
                          <p className="text-muted-foreground mt-1">
                            Create tasks to start tracking student scores
                          </p>
                          <Button onClick={() => setActiveTab('tasks')} className="mt-4">
                            <PlusCircle className="h-4 w-4 mr-2" />
                            Create a Task
                          </Button>
                        </div>
                      </div>
                    )}
                  </CardContent>
                </Card>
              </div>
            </TabsContent>
            
            <TabsContent value="templates" className="space-y-4 mt-6">
              <TemplateCreationTab classroomId={physicalId} />
            </TabsContent>

            {userRole === 'TEACHER' && (
              <TabsContent value="settings" className="space-y-4 mt-6">
                <Card>
                  <CardHeader>
                    <CardTitle>Classroom Settings</CardTitle>
                    <CardDescription>
                      Manage classroom settings and configuration
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-4">
                    <div className="flex justify-between items-center">
                      <div>
                        <h3 className="font-medium">Classroom Status</h3>
                        <p className="text-sm text-muted-foreground">
                          {classroom.active 
                            ? 'This classroom is currently active and visible to students' 
                            : 'This classroom is currently inactive and hidden from students'
                          }
                        </p>
                      </div>
                      {classroom.active ? (
                        <Button 
                          variant="destructive" 
                          onClick={() => deactivateClassroomMutation.mutate()}
                          disabled={deactivateClassroomMutation.isPending}
                        >
                          {deactivateClassroomMutation.isPending ? (
                            <>
                              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                              Deactivating...
                            </>
                          ) : 'Deactivate'}
                        </Button>
                      ) : (
                        <Button 
                          variant="default" 
                          onClick={() => activateClassroomMutation.mutate()}
                          disabled={activateClassroomMutation.isPending}
                        >
                          {activateClassroomMutation.isPending ? (
                            <>
                              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                              Activating...
                            </>
                          ) : 'Activate'}
                        </Button>
                      )}
                    </div>
                    <Separator />
                    <div className="flex justify-between items-center">
                      <div>
                        <h3 className="font-medium">Delete Classroom</h3>
                        <p className="text-sm text-muted-foreground">
                          Permanently delete this classroom and all its data
                        </p>
                      </div>
                      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                        <DialogTrigger asChild>
                          <Button variant="destructive">Delete</Button>
                        </DialogTrigger>
                        <DialogContent>
                          <DialogHeader>
                            <DialogTitle>Delete Classroom</DialogTitle>
                            <DialogDescription>
                              Are you sure you want to delete this classroom? This action cannot be undone 
                              and will permanently remove all classroom data, including student enrollments and tasks.
                            </DialogDescription>
                          </DialogHeader>
                          <DialogFooter>
                            <Button 
                              variant="outline" 
                              onClick={() => setDeleteDialogOpen(false)}
                            >
                              Cancel
                            </Button>
                            <Button 
                              variant="destructive" 
                              onClick={() => deleteClassroomMutation.mutate()}
                              disabled={deleteClassroomMutation.isPending}
                            >
                              {deleteClassroomMutation.isPending ? (
                                <>
                                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                  Deleting...
                                </>
                              ) : 'Delete Classroom'}
                            </Button>
                          </DialogFooter>
                        </DialogContent>
                      </Dialog>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>
            )}
          </Tabs>
        </main>
      </div>
    </div>
  );
}