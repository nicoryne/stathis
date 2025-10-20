'use client';

import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import { Loader2, Plus, Search, School2, ArrowRight, Bell, Users, Book, Calendar, Activity, Trash2, Power, PowerOff, Sparkles, HeartPulse } from 'lucide-react';
import Image from 'next/image';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { CreateClassroomForm } from '@/components/classroom/create-classroom-form';
import { EditClassroomForm } from '@/components/classroom/edit-classroom-form';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useToast } from "@/components/ui/use-toast";
import { ClassroomResponseDTO, ClassroomBodyDTO, createClassroom, deleteClassroom, activateClassroom, deactivateClassroom, updateClassroom, getTeacherClassrooms } from "@/services/api-classroom-client";
import { getCurrentUserPhysicalId, getCurrentUserEmail, getCurrentUserRole } from "@/lib/utils/jwt";
import { Sidebar } from '@/components/dashboard/sidebar';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import ThemeSwitcher from '@/components/theme-switcher';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { signOut } from '@/services/api-auth-client';

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
  <motion.div
    initial={{ opacity: 0, y: 20 }}
    animate={{ opacity: 1, y: 0 }}
    transition={{ duration: 0.6 }}
    whileHover={{ scale: 1.02 }}
    className="group"
  >
    <Card className={`overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300 ${className}`}>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
          <div className="p-2 rounded-full bg-primary/10 group-hover:bg-primary/20 transition-colors duration-200">
            <Icon className="h-4 w-4 text-primary" />
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">{value}</div>
        <p className="text-xs text-muted-foreground mt-1">{description}</p>
      </CardContent>
    </Card>
  </motion.div>
);

export default function ClassroomPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [activeTab, setActiveTab] = useState('all');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  
  // Function to safely close the edit dialog
  const closeEditDialog = useCallback(() => {
    setEditDialogOpen(false);
    // Reset selected classroom after a short delay to avoid UI flicker
    setTimeout(() => {
      setSelectedClassroom(null);
    }, 100);
  }, []);
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
      queryClient.invalidateQueries({ queryKey: ['teacher-classrooms'] });
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
  
  // Update classroom mutation
  const updateClassroomMutation = useMutation({
    mutationFn: ({ physicalId, updates }: { physicalId: string, updates: Partial<ClassroomBodyDTO> }) => 
      updateClassroom(physicalId, updates),
    onSuccess: () => {
      toast.success('Classroom updated successfully');
      // Close the dialog first
      setEditDialogOpen(false);
      setSelectedClassroom(null);
      
      // Guaranteed full page refresh to reset all state
      setTimeout(() => {
        window.location.href = window.location.pathname;
      }, 500);
    },
    onError: (error) => {
      toast.error(`Failed to update classroom: ${error instanceof Error ? error.message : 'Unknown error'}`);
      // Close the dialog and reset state
      setEditDialogOpen(false);
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
  const filteredClassrooms = classrooms
    ? classrooms
        .filter((classroom: ClassroomResponseDTO) => 
          classroom.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
          classroom.description.toLowerCase().includes(searchTerm.toLowerCase())
        )
        .filter((classroom: ClassroomResponseDTO) => {
          if (activeTab === 'all') return true;
          if (activeTab === 'active') return classroom.active;
          if (activeTab === 'inactive') return !classroom.active;
          return true;
        })
    : [];
  
  // Handle classroom creation success
  const handleClassroomCreated = () => {
    setShowCreateDialog(false);
    refetch();
    toast.success('Classroom created successfully!');
  };
  
  // Calculate statistics
  const totalClassrooms = classrooms?.length || 0;
  const activeClassrooms = classrooms?.filter((c: ClassroomResponseDTO) => c.active).length || 0;
  const totalStudents = classrooms?.reduce((total: number, c: ClassroomResponseDTO) => total + (c.studentCount || 0), 0) || 0;
  
  // Find the latest classroom
  const latestClassroom = classrooms
    ? [...classrooms].sort((a: ClassroomResponseDTO, b: ClassroomResponseDTO) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())[0]
    : null;
  
  // Calculate stats for the dashboard
  const activeClassroomsCount = classrooms?.filter(c => c.active).length || 0;
  const totalStudentsCount = classrooms?.reduce((total, c) => total + (c.studentCount || 0), 0) || 0;
  const recentActivity = classrooms && classrooms.length > 0 ? 
    new Date(Math.max(...classrooms.map(c => new Date(c.updatedAt || c.createdAt).getTime()))).toLocaleDateString() : 
    'No activity';
  
  return (
    <div className="flex min-h-screen relative overflow-hidden">
      {/* Animated Background Particles */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-secondary/5 to-accent/5" />
        {[...Array(20)].map((_, i) => (
          <motion.div
            key={i}
            className="absolute w-2 h-2 bg-primary/20 rounded-full"
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
            }}
            animate={{
              y: [0, -20, 0],
              opacity: [0.3, 0.8, 0.3],
            }}
            transition={{
              duration: 3 + Math.random() * 2,
              repeat: Infinity,
              delay: Math.random() * 2,
            }}
          />
        ))}
      </div>

      <Sidebar className="w-64 flex-shrink-0" />
      
      <div className="flex-1 md:ml-64">
        <header className="bg-background/80 backdrop-blur-xl border-b border-border/50 sticky top-0 z-30">
          <div className="flex h-16 items-center justify-end gap-4 px-4">
            <Button variant="outline" size="icon" className="bg-card/80 backdrop-blur-xl border-border/50 hover:bg-card/90">
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
        
        <main className="p-6 relative">
          {/* Welcome Section */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            className="mb-8"
          >
            <div className="flex items-center gap-6 mb-4">
              <div className="relative">
                <div className="absolute -inset-4 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-2xl" />
                <motion.div
                  animate={{ y: [0, -8, 0] }}
                  transition={{ duration: 2, repeat: Number.POSITIVE_INFINITY }}
                  className="relative"
                >
                  <Image
                    src="/images/mascots/mascot_muscles.png"
                    alt="Stathis Muscles Mascot"
                    width={80}
                    height={80}
                    className="drop-shadow-lg"
                  />
                </motion.div>
              </div>
              <div>
                <h1 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                  Classrooms
                </h1>
                <p className="text-muted-foreground mt-1">Manage your physical education classrooms</p>
              </div>
            </div>
            
             <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
               <DialogTrigger asChild>
                 <Button className="md:w-auto gradient-hero hover:opacity-90 shadow-lg hover:shadow-xl transition-all duration-200 text-white">
                   <Plus className="mr-2 h-4 w-4" />
                   Create Classroom
                 </Button>
               </DialogTrigger>
              <DialogContent className="sm:max-w-[600px] bg-card/80 backdrop-blur-xl border-border/50">
                <DialogHeader>
                  <DialogTitle className="flex items-center gap-3">
                    <div className="p-2 rounded-full bg-primary/10">
                      <Plus className="h-5 w-5 text-primary" />
                    </div>
                    Create New Classroom
                  </DialogTitle>
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
          </motion.div>
          
          {/* Dashboard Stats */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 }}
            className="grid gap-6 mb-8 md:grid-cols-2 lg:grid-cols-4"
          >
            <StatCard
              title="Total Classrooms"
              value={totalClassrooms.toString()}
              description="Registered classrooms"
              icon={School2}
            />
            <StatCard
              title="Active Classrooms"
              value={activeClassroomsCount.toString()}
              description="Currently active classrooms"
              icon={Activity}
            />
            <StatCard
              title="Total Students"
              value={totalStudentsCount.toString()}
              description="Students enrolled"
              icon={Users}
            />
            <StatCard
              title="Last Activity"
              value={recentActivity}
              description="Most recent classroom update"
              icon={Calendar}
            />
          </motion.div>
          
          {/* Search and Filter */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="flex flex-col space-y-4 md:flex-row md:items-center md:justify-between md:space-y-0 mb-8"
          >
            <div className="flex w-full max-w-sm items-center space-x-2">
              <div className="relative flex-1">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                <Input
                  placeholder="Search classrooms..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10 bg-card/80 backdrop-blur-xl border-border/50 rounded-xl"
                />
              </div>
            </div>
            
            <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full max-w-xs">
              <TabsList className="grid w-full grid-cols-3 bg-card/80 backdrop-blur-xl border-border/50 rounded-xl">
                <TabsTrigger value="all" className="rounded-lg data-[state=active]:bg-primary/10 data-[state=active]:text-primary data-[state=active]:border data-[state=active]:border-primary/20">All</TabsTrigger>
                <TabsTrigger value="active" className="rounded-lg data-[state=active]:bg-primary/10 data-[state=active]:text-primary data-[state=active]:border data-[state=active]:border-primary/20">Active</TabsTrigger>
                <TabsTrigger value="inactive" className="rounded-lg data-[state=active]:bg-primary/10 data-[state=active]:text-primary data-[state=active]:border data-[state=active]:border-primary/20">Inactive</TabsTrigger>
              </TabsList>
            </Tabs>
          </motion.div>
          
          {/* Classroom Cards */}
          {isLoading ? (
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="flex justify-center items-center py-12"
            >
              <div className="flex flex-col items-center gap-4">
                <div className="relative">
                  <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg animate-pulse" />
                  <Loader2 className="relative h-8 w-8 animate-spin text-primary" />
                </div>
                <span className="text-muted-foreground font-medium">Loading classrooms...</span>
              </div>
            </motion.div>
          ) : isError ? (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="bg-destructive/10 p-6 rounded-2xl border border-destructive/20"
            >
              <p className="text-destructive font-medium">Error loading classrooms: {error?.message || 'Unknown error'}</p>
              <Button variant="outline" className="mt-4 bg-card/80 backdrop-blur-xl border-border/50" onClick={() => refetch()}>
                Try Again
              </Button>
            </motion.div>
          ) : filteredClassrooms?.length === 0 ? (
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              className="text-center py-12 border rounded-2xl bg-card/80 backdrop-blur-xl border-border/50"
            >
              <div className="relative mx-auto w-16 h-16 mb-4">
                <div className="absolute -inset-2 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-lg" />
                <Image
                  src="/images/mascots/mascot_teacher.png"
                  alt="Stathis Teacher Mascot"
                  width={64}
                  height={64}
                  className="relative mx-auto drop-shadow-lg"
                />
              </div>
              <h3 className="mt-4 text-lg font-medium">No classrooms found</h3>
              <p className="mt-1 text-muted-foreground">
                {searchTerm ? 'No results match your search criteria.' : 'Create your first classroom to get started.'}
              </p>
              {searchTerm && (
                <Button variant="outline" className="mt-4 bg-card/80 backdrop-blur-xl border-border/50" onClick={() => setSearchTerm('')}>
                  Clear Search
                </Button>
              )}
            </motion.div>
          ) : (
            <motion.div 
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 0.6, delay: 0.3 }}
              className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6"
            >
              {filteredClassrooms.map((classroom: ClassroomResponseDTO, index: number) => (
                <motion.div
                  key={classroom.physicalId}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: 0.1 * index }}
                  whileHover={{ scale: 1.02 }}
                  className="group"
                >
                  <Card className="overflow-hidden rounded-2xl border-border/50 bg-card/90 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300">
                    <CardHeader className="pb-3">
                      <div className="flex justify-between items-start">
                        <CardTitle className="text-xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                          {classroom.name}
                        </CardTitle>
                        <div className="flex items-center gap-2">
                          <Badge 
                            variant={classroom.active ? "default" : "secondary"}
                            className={classroom.active ? "bg-green-500/10 text-green-600 border-green-500/20" : "bg-muted/50"}
                          >
                            {classroom.active ? 'Active' : 'Inactive'}
                          </Badge>
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="icon" className="h-8 w-8 hover:bg-primary/10">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-4 w-4">
                                  <circle cx="12" cy="12" r="1" />
                                  <circle cx="12" cy="5" r="1" />
                                  <circle cx="12" cy="19" r="1" />
                                </svg>
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="bg-card/80 backdrop-blur-xl border-border/50">
                              <DropdownMenuLabel>Classroom Actions</DropdownMenuLabel>
                              <DropdownMenuSeparator />
                              <DropdownMenuItem
                                onClick={() => {
                                  setSelectedClassroom(classroom);
                                  setEditDialogOpen(true);
                                }}
                              >
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="mr-2 h-4 w-4">
                                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                </svg>
                                Edit Classroom
                              </DropdownMenuItem>
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
                      <CardDescription className="line-clamp-2 h-10 mt-1 text-muted-foreground">
                        {classroom.description}
                      </CardDescription>
                    </CardHeader>
                    
                    <CardContent>
                      <div className="space-y-3 text-sm">
                        <div className="flex justify-between items-center">
                          <div className="flex items-center">
                            <div className="p-1 rounded-full bg-primary/10 mr-2">
                              <Users className="h-3 w-3 text-primary" />
                            </div>
                            <span className="text-muted-foreground">Students:</span>
                          </div>
                          <span className="font-medium">{classroom.studentCount || 0} students</span>
                        </div>
                        <div className="flex justify-between items-center">
                          <div className="flex items-center">
                            <div className="p-1 rounded-full bg-secondary/10 mr-2">
                              <Book className="h-3 w-3 text-secondary" />
                            </div>
                            <span className="text-muted-foreground">Teacher:</span>
                          </div>
                          <span className="font-medium">{classroom.teacherName || 'Not assigned'}</span>
                        </div>
                        <div className="flex justify-between items-center">
                          <div className="flex items-center">
                            <div className="p-1 rounded-full bg-accent/10 mr-2">
                              <Calendar className="h-3 w-3 text-accent" />
                            </div>
                            <span className="text-muted-foreground">Created:</span>
                          </div>
                          <span className="font-medium">{new Date(classroom.createdAt).toLocaleDateString()}</span>
                        </div>
                      </div>
                    </CardContent>
                    
                    <CardFooter className="pt-3 flex justify-end border-t border-border/50">
                      <Button 
                        variant="ghost" 
                        className="text-primary hover:text-primary/80 hover:bg-primary/10 transition-all duration-200" 
                        onClick={() => router.push(`/classroom/${classroom.physicalId}`)}
                      >
                        View Details
                        <ArrowRight className="ml-2 h-4 w-4" />
                      </Button>
                    </CardFooter>
                  </Card>
                </motion.div>
              ))}
            </motion.div>
          )}
        </main>
      </div>
      
      {/* Delete Confirmation Dialog */}
      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent className="bg-card/80 backdrop-blur-xl border-border/50">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-destructive/10">
                <Trash2 className="h-5 w-5 text-destructive" />
              </div>
              Delete Classroom
            </DialogTitle>
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
              disabled={deleteClassroomMutation.isPending}
              className="bg-card/80 backdrop-blur-xl border-border/50"
            >
              Cancel
            </Button>
            <Button 
              variant="destructive" 
              onClick={() => selectedClassroom && deleteClassroomMutation.mutate(selectedClassroom.physicalId)}
              disabled={deleteClassroomMutation.isPending}
              className="bg-destructive hover:bg-destructive/90"
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

      {/* Edit Classroom Dialog */}
      <Dialog 
        open={editDialogOpen} 
        onOpenChange={(open) => {
          if (!open) {
            // Close the dialog
            setEditDialogOpen(false);
            setSelectedClassroom(null);
            
            // Force a page refresh after a brief delay
            setTimeout(() => {
              window.location.href = window.location.pathname;
            }, 100);
          } else {
            setEditDialogOpen(true);
          }
        }}
      >
        <DialogContent className="sm:max-w-[600px] bg-card/80 backdrop-blur-xl border-border/50">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-3">
              <div className="p-2 rounded-full bg-primary/10">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-5 w-5 text-primary">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </div>
              Edit Classroom
            </DialogTitle>
            <DialogDescription>
              Update the details of your classroom.
            </DialogDescription>
          </DialogHeader>
          
          {selectedClassroom && (
            <EditClassroomForm 
              classroom={selectedClassroom}
              onSuccess={() => {
                setEditDialogOpen(false);
                // The component will handle the page refresh
              }}
              onCancel={() => {
                setEditDialogOpen(false);
                // Only reset selected classroom after dialog closes
                setTimeout(() => setSelectedClassroom(null), 100);
              }}
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}