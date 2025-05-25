'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2, ArrowLeft, Users, Calendar, ClipboardCheck, Book } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Separator } from '@/components/ui/separator';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { getClassroomById, getClassroomStudents } from '@/services/api-classroom-client';
import { getCurrentUserEmail, getCurrentUserRole } from '@/lib/utils/jwt';

export default function ClassroomDetailPage({ params }: { params: { physicalId: string } }) {
  const router = useRouter();
  const { physicalId } = params;
  const userEmail = getCurrentUserEmail();
  const userRole = getCurrentUserRole();
  
  // Ensure we have a valid user email before proceeding
  useEffect(() => {
    if (!userEmail && typeof window !== 'undefined') {
      // Redirect to login if we don't have a user email
      router.push('/login');
      toast.error('User information not found. Please log in again.');
    }
  }, [userEmail, router]);
  
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
  
  // Fetch classroom students (if teacher)
  const { 
    data: students, 
    isLoading: isLoadingStudents 
  } = useQuery({
    queryKey: ['classroom-students', physicalId],
    queryFn: () => getClassroomStudents(physicalId),
    enabled: !!userEmail && !!physicalId && userRole === 'TEACHER',
  });

  // Handle error cases
  if (isErrorClassroom) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] p-6">
        <div className="text-red-500 mb-4 text-5xl">
          <ClipboardCheck />
        </div>
        <h1 className="text-2xl font-bold mb-2">Error Loading Classroom</h1>
        <p className="text-muted-foreground mb-6">
          {classroomError instanceof Error ? classroomError.message : 'Failed to load classroom details'}
        </p>
        <Button onClick={() => router.push('/classroom')} variant="outline">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Classrooms
        </Button>
      </div>
    );
  }

  // Show loading state
  if (isLoadingClassroom) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh]">
        <Loader2 className="h-8 w-8 animate-spin mb-4" />
        <p className="text-muted-foreground">Loading classroom details...</p>
      </div>
    );
  }

  if (!classroom) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] p-6">
        <div className="text-muted-foreground mb-4 text-5xl">
          <Book />
        </div>
        <h1 className="text-2xl font-bold mb-2">Classroom Not Found</h1>
        <p className="text-muted-foreground mb-6">
          The classroom you're looking for doesn't exist or you don't have permission to view it.
        </p>
        <Button onClick={() => router.push('/classroom')} variant="outline">
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Classrooms
        </Button>
      </div>
    );
  }

  return (
    <div className="container mx-auto py-6 space-y-6">
      {/* Back button and classroom status */}
      <div className="flex justify-between items-center">
        <Button 
          onClick={() => router.push('/classroom')} 
          variant="outline"
          className="mb-4"
        >
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Classrooms
        </Button>
        <Badge variant={classroom.active ? "default" : "secondary"}>
          {classroom.active ? 'Active' : 'Inactive'}
        </Badge>
      </div>
      
      {/* Classroom header */}
      <div className="space-y-1">
        <h1 className="text-3xl font-bold tracking-tight">{classroom.name}</h1>
        <p className="text-muted-foreground">{classroom.description}</p>
      </div>
      
      <Separator />
      
      {/* Classroom info cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-lg flex items-center">
              <Users className="mr-2 h-4 w-4" />
              Students
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-bold">{classroom.studentCount || 0}</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-lg flex items-center">
              <Calendar className="mr-2 h-4 w-4" />
              Created
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-md">{new Date(classroom.createdAt).toLocaleDateString()}</p>
          </CardContent>
        </Card>
        
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-lg flex items-center">
              <ClipboardCheck className="mr-2 h-4 w-4" />
              Classroom Code
            </CardTitle>
            <CardDescription>Share with students to join</CardDescription>
          </CardHeader>
          <CardContent>
            <p className="text-xl font-mono bg-muted p-2 rounded text-center">
              {classroom.classroomCode}
            </p>
          </CardContent>
        </Card>
      </div>
      
      <Separator />
      
      {/* Tabs for different classroom functions */}
      <Tabs defaultValue="overview" className="w-full">
        <TabsList className="grid w-full md:w-auto grid-cols-3 md:grid-cols-4">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="students">Students</TabsTrigger>
          <TabsTrigger value="tasks">Tasks</TabsTrigger>
          {userRole === 'TEACHER' && (
            <TabsTrigger value="settings">Settings</TabsTrigger>
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
              ) : !students || students.students.length === 0 ? (
                <p className="text-center py-8 text-muted-foreground">
                  No students enrolled in this classroom yet.
                </p>
              ) : (
                <div className="divide-y">
                  {students.students.map((student) => (
                    <div key={student.physicalId} className="py-3 flex justify-between items-center">
                      <div>
                        <p className="font-medium">{student.firstName} {student.lastName}</p>
                        <p className="text-sm text-muted-foreground">{student.email}</p>
                      </div>
                      <Badge variant={student.isVerified ? "default" : "outline"}>
                        {student.isVerified ? 'Verified' : 'Pending'}
                      </Badge>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
        
        <TabsContent value="tasks" className="space-y-4 mt-6">
          <Card>
            <CardHeader>
              <CardTitle>Tasks</CardTitle>
              <CardDescription>
                Assignments and activities for this classroom
              </CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-center py-8 text-muted-foreground">
                Task functionality will be implemented in the next phase.
              </p>
            </CardContent>
          </Card>
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
                  <Button variant={classroom.active ? "destructive" : "default"}>
                    {classroom.active ? 'Deactivate' : 'Activate'}
                  </Button>
                </div>
                <Separator />
                <div className="flex justify-between items-center">
                  <div>
                    <h3 className="font-medium">Delete Classroom</h3>
                    <p className="text-sm text-muted-foreground">
                      Permanently delete this classroom and all its data
                    </p>
                  </div>
                  <Button variant="destructive">Delete</Button>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        )}
      </Tabs>
    </div>
  );
}