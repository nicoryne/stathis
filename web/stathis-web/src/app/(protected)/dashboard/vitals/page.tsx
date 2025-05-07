'use client';

import { useState, useEffect } from 'react';
import { Loader2, Heart, Thermometer, Wind, Activity, Droplet, ChevronDown } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Bell } from 'lucide-react';
import { Sidebar } from '@/components/dashboard/sidebar';
import { getUserDetails, logout } from '@/services/auth';
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
} from "@/components/ui/select";
import ThemeSwitcher from '@/components/theme-switcher';
import { createBrowserClient } from '@supabase/ssr';
import { SupabaseClient } from '@supabase/supabase-js';
import { getClassVitals } from '@/services/vitals';
import { getTeacherClassrooms } from '@/services/classroom';

type UserProfile = {
  id: string;
  first_name?: string;
  last_name?: string;
  picture_url?: string;
};

type VitalData = {
  id: string;
  bp_sys: number;
  bp_dia: number;
  heartrate: number;
  respirate: number;
  temp: number;
  o2stat: number;
  created_at: string;
  updated_at: string;
  class_id: string;
};

type StudentVital = VitalData & {
  first_name?: string;
  last_name?: string;
  picture_url?: string;
};

type Classroom = {
  id: string;
  name: string;
  description: string;
  teacher_id: string;
  is_active: boolean;
  created_at: string;
  updated_at: string;
};

export default function VitalsPage() {
  const [classroomId, setClassroomId] = useState<string>('');
  const [classrooms, setClassrooms] = useState<Classroom[]>([]);
  const [vitals, setVitals] = useState<StudentVital[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [userDetails, setUserDetails] = useState({
    first_name: '',
    last_name: '',
    email: '',
    id: ''
  });
  
  // Fetch user details and classrooms
  useEffect(() => {
    const fetchUserAndClassrooms = async () => {
      try {
        // Get user details
        const userData = await getUserDetails();
        if (userData) {
          setUserDetails({
            first_name: userData.first_name || userData.given_name || '',
            last_name: userData.last_name || userData.family_name || '',
            email: userData.email || '',
            id: userData.sub || '' // Use sub from identity data as user ID
          });
          
          // Fetch teacher's classrooms
          const userId = userData.sub || '';
          if (userId) {
            const teacherClassrooms = await getTeacherClassrooms(userId);
            setClassrooms(teacherClassrooms);
            
            // Set default classroom to the first one if available
            if (teacherClassrooms && teacherClassrooms.length > 0) {
              setClassroomId(teacherClassrooms[0].id);
            } else {
              // No classrooms available, set loading to false
              setLoading(false);
            }
          } else {
            setLoading(false);
          }
        } else {
          setLoading(false);
        }
      } catch (err) {
        console.error('Error fetching user or classrooms:', err);
        setError(err instanceof Error ? err : new Error('Error fetching initial data'));
        setLoading(false);
      }
    };
    
    fetchUserAndClassrooms();
  }, []);

  // Set up initial data fetch and real-time subscription
  useEffect(() => {
    // Skip if no classroom is selected yet
    if (!classroomId) {
      setLoading(false);
      return;
    }
    
    const fetchVitalsData = async () => {
      try {
        setLoading(true);
        
        const supabase = createBrowserClient(
          process.env.NEXT_PUBLIC_SUPABASE_URL || '',
          process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY || ''
        );
        
        // Step 1: Get student IDs for the classroom
        const { data: students, error: studentsError } = await supabase
          .from('classroom_students')
          .select('user_id')
          .eq('classroom_id', classroomId);
        
        if (studentsError) {
          throw new Error(`Error fetching students: ${studentsError.message}`);
        }
        
        if (!students || students.length === 0) {
          setVitals([]);
          setLoading(false);
          return;
        }
        
        const studentIds = students.map(s => s.user_id);
        
        // Step 2: Fetch vitals for these students
        const { data: vitalsData, error: vitalsError } = await supabase
          .from('vitals')
          .select('*')
          .in('id', studentIds);
          
        if (vitalsError) {
          throw new Error(`Error fetching vitals: ${vitalsError.message}`);
        }
        
        if (!vitalsData || vitalsData.length === 0) {
          setVitals([]);
          setLoading(false);
          return;
        }
        
        // Step 3: Fetch user profiles for these students
        const { data: profiles, error: profilesError } = await supabase
          .from('user_profile') // Assuming this is the table for user profiles
          .select('id, first_name, last_name, picture_url')
          .in('id', studentIds);
          
        if (profilesError) {
          console.error(`Error fetching profiles: ${profilesError.message}`);
          // Continue with available vitals data if profiles can't be fetched
        }
        
        // Step 4: Combine the data
        const enrichedVitals = vitalsData.map(vital => {
          const profile = profiles?.find(p => p.id === vital.id) || {} as UserProfile;
          return {
            ...vital,
            first_name: profile.first_name || 'Unknown',
            last_name: profile.last_name || 'Student',
            picture_url: profile.picture_url
          };
        });
        
        setVitals(enrichedVitals);
        
        // Step 5: Set up real-time subscription
        const channel = supabase
          .channel('vitals-changes')
          .on('postgres_changes', 
            {
              event: '*', // Listen to all events (INSERT, UPDATE, DELETE)
              schema: 'public',
              table: 'vitals',
              filter: `id=in.(${studentIds.map(id => `'${id}'`).join(',')})`
            }, 
            (payload) => {
              console.log('Real-time update received:', payload);
              // Handle real-time updates
              if (payload.eventType === 'INSERT') {
                // Fetch user profile for this vital
                fetchProfileForVital(supabase, payload.new as VitalData)
                  .then(enrichedVital => {
                    setVitals(current => [...current, enrichedVital]);
                  });
              } else if (payload.eventType === 'UPDATE') {
                setVitals(current => current.map(v => 
                  v.id === (payload.new as VitalData).id ? { ...v, ...(payload.new as VitalData) } : v
                ));
              } else if (payload.eventType === 'DELETE') {
                setVitals(current => current.filter(v => v.id !== (payload.old as VitalData).id));
              }
            })
          .subscribe();
          
        return () => {
          supabase.removeChannel(channel);
        };
      } catch (err) {
        console.error('Error setting up vitals:', err);
        setError(err instanceof Error ? err : new Error('Unknown error fetching vitals'));
        setVitals([]);
        setLoading(false);
      } finally {
        setLoading(false);
      }
    };
    
    // Helper function to fetch profile data for a new vital
    const fetchProfileForVital = async (supabase: SupabaseClient, vital: VitalData): Promise<StudentVital> => {
      const { data: profile } = await supabase
        .from('user_profile')
        .select('first_name, last_name, picture_url')
        .eq('id', vital.id)
        .single();
        
      return {
        ...vital,
        first_name: profile?.first_name,
        last_name: profile?.last_name,
        picture_url: profile?.picture_url
      };
    };
    
    fetchVitalsData();
  }, [classroomId]);

  const handleLogout = async () => {
    await logout();
  };
  
  const handleClassroomChange = (value: string) => {
    setClassroomId(value);
  };

  // Helper function to determine vital status colors
  const getHeartRateStatus = (rate: number) => {
    if (rate < 60) return 'text-blue-500';
    if (rate > 100) return 'text-red-500';
    return 'text-green-500';
  };
  
  const getTemperatureStatus = (temp: number) => {
    if (temp < 36) return 'text-blue-500';
    if (temp > 37.5) return 'text-red-500';
    return 'text-green-500';
  };
  
  const getO2Status = (o2: number) => {
    if (o2 < 95) return 'text-red-500';
    return 'text-green-500';
  };
  
  const getBPStatus = (sys: number, dia: number) => {
    if (sys > 140 || dia > 90) return 'text-red-500';
    if (sys < 90 || dia < 60) return 'text-blue-500';
    return 'text-green-500';
  };

  const getRespirateStatus = (rate: number) => {
    if (rate < 12) return 'text-blue-500';
    if (rate > 20) return 'text-red-500';
    return 'text-green-500';
  };

  // Display loading state if classrooms are still being fetched or no classroom has been selected yet
  if (loading || (!error && !classroomId)) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <span className="ml-2">Loading vital signs...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Card className="max-w-md">
          <CardHeader>
            <CardTitle className="text-red-500">Error</CardTitle>
          </CardHeader>
          <CardContent>
            <p>Failed to load data: {error.message}</p>
            <Button 
              className="mt-4" 
              onClick={() => window.location.reload()}
            >
              Retry
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-muted/10">
      <Sidebar className="w-64 flex-shrink-0" />
      
      <div className="flex-1">
        <header className="bg-background border-b sticky top-0 z-10">
          <div className="flex h-16 items-center justify-between px-6">
            <h1 className="text-2xl font-bold">Vitals Dashboard</h1>
            <div className="flex items-center gap-4">
              <Button variant="outline" size="icon" className="rounded-full">
                <Bell className="h-5 w-5" />
                <span className="sr-only">Notifications</span>
              </Button>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" className="relative h-8 w-8 rounded-full">
                    <Avatar className="h-8 w-8 border border-muted">
                      <AvatarImage src="/placeholder.svg" alt="User" />
                      <AvatarFallback className="bg-primary/10 text-primary">
                        {userDetails.first_name.charAt(0).toUpperCase()}
                        {userDetails.last_name.charAt(0).toUpperCase()}
                      </AvatarFallback>
                    </Avatar>
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent className="w-56" align="end" forceMount>
                  <DropdownMenuLabel className="font-normal">
                    <div className="flex flex-col space-y-1">
                      <p className="text-sm leading-none font-medium">
                        {userDetails.first_name} {userDetails.last_name}
                      </p>
                      <p className="text-muted-foreground text-xs leading-none">
                        {userDetails.email}
                      </p>
                    </div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem onClick={handleLogout}>Log out</DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
              <ThemeSwitcher />
            </div>
          </div>
        </header>

        <div className="p-6 md:p-8">
          <div className="mb-8">
            <Card>
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between">
                  <div>
                    <CardTitle>Class Vitals Overview</CardTitle>
                    <CardDescription>Real-time vitals monitoring for all students</CardDescription>
                  </div>
                  <Badge variant="outline" className="ml-auto">
                    <span className="mr-1 h-2 w-2 rounded-full bg-green-500 inline-block"></span> 
                    Live Data
                  </Badge>
                </div>
              </CardHeader>
              <CardContent>
                {/* Classroom selector */}
                <div className="mb-6">
                  <div className="flex items-center mb-4">
                    <h3 className="text-sm font-medium mr-3">Select Classroom:</h3>
                    <Select value={classroomId} onValueChange={handleClassroomChange}>
                      <SelectTrigger className="w-[280px]">
                        <SelectValue placeholder="Select a classroom" />
                      </SelectTrigger>
                      <SelectContent>
                        {classrooms.length === 0 ? (
                          <SelectItem value="no-classrooms" disabled>
                            No classrooms available
                          </SelectItem>
                        ) : (
                          classrooms.map((classroom) => (
                            <SelectItem key={classroom.id} value={classroom.id}>
                              {classroom.name}
                            </SelectItem>
                          ))
                        )}
                      </SelectContent>
                    </Select>
                  </div>
                  {classrooms.length === 0 && (
                    <p className="text-sm text-amber-500">
                      You don't have any classrooms yet. Please create a classroom first.
                    </p>
                  )}
                </div>
                
                <Tabs defaultValue="grid" className="w-full">
                  <div className="flex justify-between items-center mb-4">
                    <TabsList>
                      <TabsTrigger value="grid">Grid View</TabsTrigger>
                      <TabsTrigger value="list">List View</TabsTrigger>
                    </TabsList>
                    <div className="text-sm text-muted-foreground">
                      {vitals.length} students monitored
                    </div>
                  </div>
                  
                  {vitals.length === 0 ? (
                    <div className="text-center py-8">
                      <p className="text-muted-foreground">No vitals data available for students in this classroom.</p>
                    </div>
                  ) : (
                    <>
                      <TabsContent value="grid" className="mt-0">
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                          {vitals.map(student => (
                            <Card key={student.id} className="overflow-hidden border-2 hover:border-primary/50 transition-colors">
                              <div className="relative h-36">
                                <div 
                                  className="absolute inset-0 bg-gradient-to-t from-black/70 to-black/10 z-10"
                                ></div>
                                <div 
                                  className="absolute inset-0 bg-cover bg-center"
                                  style={{ 
                                    backgroundImage: `url(${student.picture_url || '/placeholder.svg'})`,
                                    opacity: 0.7
                                  }}
                                ></div>
                                <div className="absolute bottom-3 left-3 z-20 flex items-end">
                                  <Avatar className="h-12 w-12 border-2 border-white mr-2">
                                    <AvatarImage src={student.picture_url || '/placeholder.svg'} alt={student.first_name || 'Student'} />
                                    <AvatarFallback className="bg-primary text-primary-foreground">
                                      {student.first_name?.charAt(0) || '?'}{student.last_name?.charAt(0) || '?'}
                                    </AvatarFallback>
                                  </Avatar>
                                  <div>
                                    <p className="text-white font-medium">{student.first_name || 'Unknown'} {student.last_name || 'Student'}</p>
                                    <p className="text-xs text-white/80">Updated {
                                      new Date(student.updated_at).toLocaleTimeString([], {
                                        hour: '2-digit',
                                        minute: '2-digit'
                                      })
                                    }</p>
                                  </div>
                                </div>
                              </div>
                              <CardContent className="pt-4">
                                <div className="grid grid-cols-3 gap-2">
                                  <div className="flex flex-col items-center p-1.5 rounded-lg bg-muted/50">
                                    <div className="flex items-center mb-1">
                                      <Heart className={`h-4 w-4 mr-1 ${getHeartRateStatus(student.heartrate)}`} />
                                      <span className="text-xs font-medium">HR</span>
                                    </div>
                                    <p className="text-sm font-bold">{student.heartrate || '--'}</p>
                                    <p className="text-xs text-muted-foreground">bpm</p>
                                  </div>
                                  
                                  <div className="flex flex-col items-center p-1.5 rounded-lg bg-muted/50">
                                    <div className="flex items-center mb-1">
                                      <Thermometer className={`h-4 w-4 mr-1 ${getTemperatureStatus(student.temp)}`} />
                                      <span className="text-xs font-medium">Temp</span>
                                    </div>
                                    <p className="text-sm font-bold">{student.temp?.toFixed(1) || '--'}</p>
                                    <p className="text-xs text-muted-foreground">°C</p>
                                  </div>
                                  
                                  <div className="flex flex-col items-center p-1.5 rounded-lg bg-muted/50">
                                    <div className="flex items-center mb-1">
                                      <Wind className={`h-4 w-4 mr-1 ${getRespirateStatus(student.respirate)}`} />
                                      <span className="text-xs font-medium">Resp</span>
                                    </div>
                                    <p className="text-sm font-bold">{student.respirate || '--'}</p>
                                    <p className="text-xs text-muted-foreground">/min</p>
                                  </div>
                                </div>
                                
                                <div className="mt-2 grid grid-cols-2 gap-2">
                                  <div className="flex flex-col p-1.5 rounded-lg bg-muted/50">
                                    <div className="flex items-center mb-1">
                                      <Droplet className={`h-4 w-4 mr-1 ${getBPStatus(student.bp_sys, student.bp_dia)}`} />
                                      <span className="text-xs font-medium">BP</span>
                                    </div>
                                    <p className="text-sm font-bold">{student.bp_sys || '--'}/{student.bp_dia || '--'}</p>
                                    <p className="text-xs text-muted-foreground">mmHg</p>
                                  </div>
                                  
                                  <div className="flex flex-col p-1.5 rounded-lg bg-muted/50">
                                    <div className="flex items-center mb-1">
                                      <Activity className={`h-4 w-4 mr-1 ${getO2Status(student.o2stat)}`} />
                                      <span className="text-xs font-medium">O₂</span>
                                    </div>
                                    <p className="text-sm font-bold">{student.o2stat || '--'}%</p>
                                    <div className="w-full mt-1">
                                      <Progress value={student.o2stat} className="h-1" />
                                    </div>
                                  </div>
                                </div>
                              </CardContent>
                            </Card>
                          ))}
                        </div>
                      </TabsContent>
                      
                      <TabsContent value="list" className="mt-0">
                        <div className="rounded-md border">
                          <div className="grid grid-cols-7 bg-muted/50 p-3 text-sm font-medium">
                            <div>Student</div>
                            <div className="text-center">Heart Rate</div>
                            <div className="text-center">Blood Pressure</div>
                            <div className="text-center">Temperature</div>
                            <div className="text-center">Respiration</div>
                            <div className="text-center">Oxygen</div>
                            <div className="text-center">Last Updated</div>
                          </div>
                          {vitals.map(student => (
                            <div key={student.id} className="grid grid-cols-7 border-t p-3 text-sm">
                              <div className="flex items-center gap-2">
                                <Avatar className="h-8 w-8">
                                  <AvatarImage src={student.picture_url || '/placeholder.svg'} alt={student.first_name || 'Student'} />
                                  <AvatarFallback className="bg-primary/10">
                                    {student.first_name?.charAt(0) || '?'}{student.last_name?.charAt(0) || '?'}
                                  </AvatarFallback>
                                </Avatar>
                                <div>{student.first_name || 'Unknown'} {student.last_name || 'Student'}</div>
                              </div>
                              <div className="flex items-center justify-center">
                                <Heart className={`h-4 w-4 mr-1 ${getHeartRateStatus(student.heartrate)}`} />
                                <span>{student.heartrate || '--'} bpm</span>
                              </div>
                              <div className="flex items-center justify-center">
                                <Droplet className={`h-4 w-4 mr-1 ${getBPStatus(student.bp_sys, student.bp_dia)}`} />
                                <span>{student.bp_sys || '--'}/{student.bp_dia || '--'}</span>
                              </div>
                              <div className="flex items-center justify-center">
                                <Thermometer className={`h-4 w-4 mr-1 ${getTemperatureStatus(student.temp)}`} />
                                <span>{student.temp?.toFixed(1) || '--'} °C</span>
                              </div>
                              <div className="flex items-center justify-center">
                                <Wind className={`h-4 w-4 mr-1 ${getRespirateStatus(student.respirate)}`} />
                                <span>{student.respirate || '--'} /min</span>
                              </div>
                              <div className="flex items-center justify-center">
                                <Activity className={`h-4 w-4 mr-1 ${getO2Status(student.o2stat)}`} />
                                <span>{student.o2stat || '--'}%</span>
                              </div>
                              <div className="text-center text-muted-foreground">
                                {new Date(student.updated_at).toLocaleTimeString([], {
                                  hour: '2-digit',
                                  minute: '2-digit',
                                  second: '2-digit'
                                })}
                              </div>
                            </div>
                          ))}
                        </div>
                      </TabsContent>
                    </>
                  )}
                </Tabs>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
