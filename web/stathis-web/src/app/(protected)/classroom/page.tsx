'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2, Plus, Search, School2, ArrowRight, Bell, Users, Book, Calendar, Activity, Trash2, Power, PowerOff } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { CreateClassroomForm } from '@/components/classroom/create-classroom-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { getTeacherClassrooms, deleteClassroom, activateClassroom, deactivateClassroom } from '@/services/api-classroom-client';
import { ClassroomResponseDTO } from '@/services/api-classroom';
import { getCurrentUserEmail, getCurrentUserPhysicalId } from '@/lib/utils/jwt';
import { Sidebar } from '@/components/dashboard/sidebar';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import ThemeSwitcher from '@/components/theme-switcher';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { signOut } from '@/services/api-auth';

// StatCard Component for reuse
interface StatCardProps {
  title: string;
  value: string | number;
  description: string;
  icon: React.ElementType;
  className?: string;
}

const handlesignOut = async () => {
    await signOut();
}

const StatCard = ({ title, value, description, icon: Icon, className = '' }: StatCardProps) => (
  <Card className={`overflow-hidden ${className}`}>
    <CardHeader className="pb-2">
      <div className="flex items-center justify-between">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground" />
      </div>
    </CardHeader>
    <CardContent>
      <div className="text-2xl font-bold">{value}</div>
      <p className="text-xs text-muted-foreground mt-1">{description}</p>
    </CardContent>
  </Card>
);

export default function ClassroomPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState('all');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedClassroom, setSelectedClassroom] = useState<ClassroomResponseDTO | null>(null);
  const userPhysicalId = getCurrentUserPhysicalId();
  const userEmail = getCurrentUserEmail();
  const [userDetails, setUserDetails] = useState({
    first_name: '',
    last_name: '',
    email: userEmail || ''
  });
  
  // Ensure we have a valid user email before proceeding
  useEffect(() => {
    if (!userEmail && typeof window !== 'undefined') {
      // Redirect to login if we don't have a user email
      router.push('/login');
      toast.error('User information not found. Please log in again.');
    }
  }, [userEmail, router]);
  
  // Fetch classrooms for the current teacher
  // The backend will identify the teacher using the security context from the JWT token
  const { data: classrooms, isLoading, isError, error, refetch } = useQuery({
    queryKey: ['teacher-classrooms'],
    queryFn: () => getTeacherClassrooms(),
    enabled: !!userEmail // Only fetch if we have a user email
  });
  
  // Delete classroom mutation
  const deleteClassroomMutation = useMutation({
    mutationFn: (physicalId: string) => deleteClassroom(physicalId),
    onSuccess: () => {
      toast.success('Classroom deleted successfully');
      // Close the dialog first
      setDeleteDialogOpen(false);
      setSelectedClassroom(null);
      
      // Guaranteed full page refresh to reset all state
      setTimeout(() => {
        window.location.href = window.location.pathname;
      }, 500);
    },
    onError: (error) => {
      toast.error(`Failed to delete classroom: ${error instanceof Error ? error.message : 'Unknown error'}`);
      // Close the dialog first
      setDeleteDialogOpen(false);
      setSelectedClassroom(null);
      
      // Guaranteed full page refresh to reset all state
      setTimeout(() => {
        window.location.href = window.location.pathname;
      }, 500);
    }
  });
  
  // Activate classroom mutation
  const activateClassroomMutation = useMutation({
    mutationFn: (physicalId: string) => activateClassroom(physicalId),
    onSuccess: () => {
      toast.success('Classroom activated successfully');
      queryClient.invalidateQueries({ queryKey: ['teacher-classrooms'] });
    },
    onError: (error) => {
      toast.error(`Failed to activate classroom: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });
  
  // Deactivate classroom mutation
  const deactivateClassroomMutation = useMutation({
    mutationFn: (physicalId: string) => deactivateClassroom(physicalId),
    onSuccess: () => {
      toast.success('Classroom deactivated successfully');
      queryClient.invalidateQueries({ queryKey: ['teacher-classrooms'] });
    },
    onError: (error) => {
      toast.error(`Failed to deactivate classroom: ${error instanceof Error ? error.message : 'Unknown error'}`);
    }
  });
  
  // Filter classrooms based on search term and active tab
  const filteredClassrooms = classrooms?.filter(classroom => {
    const matchesSearch = classroom.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      classroom.description.toLowerCase().includes(searchTerm.toLowerCase());
    
    if (activeTab === 'all') return matchesSearch;
    if (activeTab === 'active') return matchesSearch && classroom.active;
    if (activeTab === 'inactive') return matchesSearch && !classroom.active;
    
    return matchesSearch;
  });
  
  // Handle classroom creation success
  const handleClassroomCreated = () => {
    setShowCreateDialog(false);
    refetch();
    toast.success('Classroom created successfully!');
  };
  
  // Calculate stats for the dashboard
  const activeClassrooms = classrooms?.filter(c => c.active).length || 0;
  const totalStudents = classrooms?.reduce((total, c) => total + (c.studentCount || 0), 0) || 0;
  const recentActivity = classrooms && classrooms.length > 0 ? 
    new Date(Math.max(...classrooms.map(c => new Date(c.updatedAt || c.createdAt).getTime()))).toLocaleDateString() : 
    'No activity';
  
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
                <DropdownMenuItem onClick={handlesignOut}>Sign out</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
            <ThemeSwitcher />
          </div>
        </header>
        
        <main className="p-6">
          <div className="mb-6 flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0">
            <div>
              <h1 className="text-2xl font-bold tracking-tight">Classrooms</h1>
              <p className="text-muted-foreground mt-1">Manage your physical education classrooms</p>
            </div>
            
            <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
              <DialogTrigger asChild>
                <Button className="md:w-auto">
                  <Plus className="mr-2 h-4 w-4" />
                  Create Classroom
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-[600px]">
                <DialogHeader>
                  <DialogTitle>Create New Classroom</DialogTitle>
                  <DialogDescription>
                    Fill in the details to create a new physical education classroom.
                  </DialogDescription>
                </DialogHeader>
                
                <CreateClassroomForm 
                  onSuccess={handleClassroomCreated} 
                  onCancel={() => setShowCreateDialog(false)}
                />
              </DialogContent>
            </Dialog>
          </div>
          
          {/* Dashboard Stats */}
          <div className="grid gap-6 mb-6 md:grid-cols-2 lg:grid-cols-4">
            <StatCard
              title="Total Classrooms"
              value={classrooms?.length || '0'}
              description="Registered classrooms"
              icon={School2}
            />
            <StatCard
              title="Active Classrooms"
              value={activeClassrooms.toString()}
              description="Currently active classrooms"
              icon={Activity}
            />
            <StatCard
              title="Total Students"
              value={totalStudents.toString()}
              description="Students enrolled"
              icon={Users}
            />
            <StatCard
              title="Last Activity"
              value={recentActivity}
              description="Most recent classroom update"
              icon={Calendar}
            />
          </div>
          
          {/* Search and Filter */}
          <div className="flex flex-col space-y-4 md:flex-row md:items-center md:justify-between md:space-y-0 mb-6">
            <div className="flex w-full max-w-sm items-center space-x-2">
              <Input
                placeholder="Search classrooms..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full"
              />
              <Button variant="outline" size="icon">
                <Search className="h-4 w-4" />
              </Button>
            </div>
            
            <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full max-w-xs">
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="all">All</TabsTrigger>
                <TabsTrigger value="active">Active</TabsTrigger>
                <TabsTrigger value="inactive">Inactive</TabsTrigger>
              </TabsList>
            </Tabs>
          </div>
          
          {/* Classroom Cards */}
          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-primary" />
              <span className="ml-2">Loading classrooms...</span>
            </div>
          ) : isError ? (
            <div className="bg-destructive/10 p-4 rounded-md">
              <p className="text-destructive">Error loading classrooms: {error?.message || 'Unknown error'}</p>
              <Button variant="outline" className="mt-2" onClick={() => refetch()}>
                Try Again
              </Button>
            </div>
          ) : filteredClassrooms?.length === 0 ? (
            <div className="text-center py-12 border rounded-lg bg-muted/20">
              <School2 className="mx-auto h-12 w-12 text-muted-foreground" />
              <h3 className="mt-4 text-lg font-medium">No classrooms found</h3>
              <p className="mt-1 text-muted-foreground">
                {searchTerm ? 'No results match your search criteria.' : 'Create your first classroom to get started.'}
              </p>
              {searchTerm && (
                <Button variant="outline" className="mt-4" onClick={() => setSearchTerm('')}>
                  Clear Search
                </Button>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredClassrooms?.map((classroom) => (
                <Card key={classroom.physicalId} className="overflow-hidden hover:shadow-md transition-shadow duration-300">
                  <CardHeader className="pb-3">
                    <div className="flex justify-between items-start">
                      <CardTitle className="text-xl">{classroom.name}</CardTitle>
                      <div className="flex items-center gap-2">
                        <Badge variant={classroom.active ? "default" : "secondary"}>
                          {classroom.active ? 'Active' : 'Inactive'}
                        </Badge>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="icon" className="h-8 w-8">
                              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4">
                                <circle cx="12" cy="12" r="1" />
                                <circle cx="12" cy="5" r="1" />
                                <circle cx="12" cy="19" r="1" />
                              </svg>
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuLabel>Classroom Actions</DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            {classroom.active ? (
                              <DropdownMenuItem
                                onClick={() => deactivateClassroomMutation.mutate(classroom.physicalId)}
                                disabled={deactivateClassroomMutation.isPending}
                              >
                                <PowerOff className="mr-2 h-4 w-4" />
                                Deactivate
                              </DropdownMenuItem>
                            ) : (
                              <DropdownMenuItem
                                onClick={() => activateClassroomMutation.mutate(classroom.physicalId)}
                                disabled={activateClassroomMutation.isPending}
                              >
                                <Power className="mr-2 h-4 w-4" />
                                Activate
                              </DropdownMenuItem>
                            )}
                            <DropdownMenuItem
                              onClick={() => {
                                setSelectedClassroom(classroom);
                                setDeleteDialogOpen(true);
                              }}
                              className="text-destructive focus:text-destructive"
                            >
                              <Trash2 className="mr-2 h-4 w-4" />
                              Delete
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </div>
                    </div>
                    <CardDescription className="line-clamp-2 h-10 mt-1">
                      {classroom.description}
                    </CardDescription>
                  </CardHeader>
                  
                  <CardContent>
                    <div className="space-y-3 text-sm">
                      <div className="flex justify-between items-center">
                        <div className="flex items-center">
                          <Users className="h-4 w-4 mr-2 text-muted-foreground" />
                          <span className="text-muted-foreground">Students:</span>
                        </div>
                        <span className="font-medium">{classroom.studentCount || 0} students</span>
                      </div>
                      <div className="flex justify-between items-center">
                        <div className="flex items-center">
                          <Book className="h-4 w-4 mr-2 text-muted-foreground" />
                          <span className="text-muted-foreground">Teacher:</span>
                        </div>
                        <span className="font-medium">{classroom.teacherName || 'Not assigned'}</span>
                      </div>
                      <div className="flex justify-between items-center">
                        <div className="flex items-center">
                          <Calendar className="h-4 w-4 mr-2 text-muted-foreground" />
                          <span className="text-muted-foreground">Created:</span>
                        </div>
                        <span className="font-medium">{new Date(classroom.createdAt).toLocaleDateString()}</span>
                      </div>
                    </div>
                  </CardContent>
                  
                  <CardFooter className="pt-3 flex justify-end border-t">
                    <Button 
                      variant="ghost" 
                      className="text-primary hover:text-primary/80" 
                      onClick={() => router.push(`/classroom/${classroom.physicalId}`)}
                    >
                      View Details
                      <ArrowRight className="ml-2 h-4 w-4" />
                    </Button>
                  </CardFooter>
                </Card>
              ))}
            </div>
          )}
        </main>
      </div>
      
      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Classroom</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete the classroom "{selectedClassroom?.name}"? This action cannot be undone 
              and will permanently remove all classroom data, including student enrollments and tasks.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button 
              variant="outline" 
              onClick={() => {
                setDeleteDialogOpen(false);
                setSelectedClassroom(null);
              }}
            >
              Cancel
            </Button>
            <Button 
              variant="destructive" 
              onClick={() => {
                if (selectedClassroom) {
                  try {
                    deleteClassroomMutation.mutate(selectedClassroom.physicalId);
                  } catch (error) {
                    // Ensure dialog closes even if there's an unexpected error
                    setDeleteDialogOpen(false);
                    setSelectedClassroom(null);
                    toast.error('An unexpected error occurred');
                    
                    // Guaranteed full page refresh to reset all state
                    setTimeout(() => {
                      window.location.href = window.location.pathname;
                    }, 500);
                  }
                }
              }}
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
  );
}