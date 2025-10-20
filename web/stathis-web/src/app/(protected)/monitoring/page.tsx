'use client';

import React, { useState, useEffect, useCallback, useMemo, useRef, memo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Sidebar } from '@/components/dashboard/sidebar';
import { AuthNavbar } from '@/components/auth-navbar';
import { WebSocketManager } from '@/lib/websocket/websocket-client';
import { useHeartRateAlerts } from '@/lib/websocket/use-heart-rate-alerts';
import { getTeacherClassrooms, getClassroomStudents } from '@/services/api-classroom';
import { cn } from '@/lib/utils';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  CardFooter
} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import {
  Heart,
  Search,
  AlertTriangle,
  UserRoundX,
  School,
  Clock,
  Users,
  Filter,
  Wifi,
  WifiOff,
  Loader2,
} from 'lucide-react';
import { useToast } from '@/components/ui/use-toast';

// Define the interface for student data
interface Student {
  id: string;
  firstName: string;
  lastName: string;
  studentNumber: string;
  heartRate?: number;
  isActive: boolean;
  lastUpdate: string;
  lastUpdateTimestamp?: Date; // Track actual timestamp for offline detection
  status: "excellent" | "good" | "warning" | "inactive";
  profilePictureUrl?: string;
}

// WebSocket message types - matching mobile app's VitalsWebSocketDTO
interface VitalsWebSocketData {
  physicalId: string;
  studentId: string;
  classroomId: string;
  taskId: string;
  heartRate: number;
  oxygenSaturation: number;
  timestamp: string;
  isPreActivity: boolean;
  isPostActivity: boolean;
}

// Constants for status and thresholds
const OFFLINE_TIMEOUT_MS = 30000; // 30 seconds - mark as offline if no vitals received

// Function to transform API data to our Student model
function mapApiDataToStudent(apiStudent: any): Student {
  return {
    id: apiStudent.physicalId,
    firstName: apiStudent.firstName,
    lastName: apiStudent.lastName,
    studentNumber: apiStudent.email?.split('@')[0] || 'Unknown', // Use part of email as student number
    heartRate: undefined,
    isActive: false, // Initially offline until we receive vitals
    lastUpdate: "No data yet",
    lastUpdateTimestamp: undefined,
    status: "inactive", // Start as inactive
    profilePictureUrl: apiStudent.profilePictureUrl
  };
}

// Format relative time (e.g., "2 min ago")
function formatRelativeTime(timestamp: string): string {
  const date = new Date(timestamp);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSeconds = Math.floor(diffMs / 1000);
  
  if (diffSeconds < 60) return `${diffSeconds} sec ago`;
  
  const diffMinutes = Math.floor(diffSeconds / 60);
  if (diffMinutes < 60) return `${diffMinutes} min ago`;
  
  const diffHours = Math.floor(diffMinutes / 60);
  return `${diffHours} hr ago`;
}

// Function to determine heart rate status (visual indicator only)
// WARNING status is determined by backend alerts based on age-specific thresholds (85% of max HR)
function getHeartRateStatus(
  heartRate: number | undefined
): "excellent" | "good" | "warning" | "inactive" {
  if (!heartRate) return "inactive";
  if (heartRate < 70) return "excellent"; // Resting heart rate range
  return "good"; // Normal active heart rate (warning determined by backend)
}

// Determine status color CSS classes
function getStatusColor(status: string): string {
  switch (status) {
    case "excellent":
      return "border-green-500 bg-green-50 text-green-700";
    case "good":
      return "border-blue-500 bg-blue-50 text-blue-700";
    case "warning":
      return "border-orange-500 bg-orange-50 text-orange-700";
    case "inactive":
      return "border-gray-300 bg-gray-50 text-gray-500 opacity-75";
    default:
      return "border-gray-300 bg-gray-50 text-gray-500";
  }
}

// Determine badge color for status
function getStatusBadgeClass(status: string): string {
  switch (status) {
    case "excellent":
      return "bg-green-100 text-green-800";
    case "good":
      return "bg-blue-100 text-blue-800";
    case "warning":
      return "bg-orange-100 text-orange-800";
    case "inactive":
      return "bg-gray-100 text-gray-800";
    default:
      return "bg-gray-100 text-gray-800";
  }
}

// Custom hook for WebSocket connection with throttling
function useWebSocketMonitor(classroomId: string | null) {
  const [students, setStudents] = useState<Student[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  
  // Buffer for incoming WebSocket vitals data
  const vitalsBuffer = useRef<Record<string, VitalsWebSocketData>>({});
  const updateTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const lastUpdateTimeRef = useRef<Date>(new Date());
  const { toast } = useToast();
  
  // Subscribe to heart rate alerts from backend (85% of max HR based on age)
  const { alerts } = useHeartRateAlerts(classroomId || '');

  // Throttled function to process buffered updates
  const processBufferedUpdates = useCallback(() => {
    // Clear any existing timeout
    if (updateTimeoutRef.current) {
      clearTimeout(updateTimeoutRef.current);
    }
    
    // Schedule update with 500ms throttle
    updateTimeoutRef.current = setTimeout(() => {
      // Only process if we have data
      if (Object.keys(vitalsBuffer.current).length === 0) {
        return;
      }
      
      setStudents(prevStudents => {
        // Use Map for faster lookups by student ID
        const studentMap = new Map(prevStudents.map(student => [student.id, student]));
        let hasChanges = false;
        
        // Process vitals updates
        Object.values(vitalsBuffer.current).forEach(data => {
          const student = studentMap.get(data.studentId);
          if (student) {
            let status = getHeartRateStatus(data.heartRate);
            const now = new Date();
            
            // Check if student is in backend alerts (exceeding age-based threshold)
            const hasAlert = alerts.some(alert => alert.studentId === data.studentId);
            if (hasAlert) {
              status = 'warning'; // Override status if backend sent an alert
            }
            
            // ALWAYS update timestamp and online status when receiving vitals
            // Even if heart rate hasn't changed, we need to keep the student marked as online
            studentMap.set(data.studentId, {
              ...student,
              heartRate: data.heartRate,
              lastUpdate: formatRelativeTime(data.timestamp),
              lastUpdateTimestamp: now, // CRITICAL: Always update timestamp to prevent false offline status
              status,
              isActive: true // Mark as active when receiving vitals
            });
            hasChanges = true;
            
            console.log(`Updated vitals for ${data.studentId}: HR=${data.heartRate}, O2=${data.oxygenSaturation}%, Status=${status}${hasAlert ? ' (ALERT)' : ''}`);
          }
        });
        
        // Clear buffer
        vitalsBuffer.current = {};
        
        // Update the timestamp
        if (hasChanges) {
          lastUpdateTimeRef.current = new Date();
        }
        
        // Only create new array if data changed
        return hasChanges ? Array.from(studentMap.values()) : prevStudents;
      });
    }, 500);
  }, [alerts]); // Re-run when alerts change to update warning status

  // Handle WebSocket vitals updates (from mobile app)
  const handleVitalsUpdate = useCallback((data: any) => {
    console.log('Received vitals data:', data);
    
    if (!data || !data.studentId || typeof data.heartRate !== 'number') {
      console.warn('Invalid vitals data received:', data);
      return;
    }
    
    // Store in buffer instead of updating state directly
    vitalsBuffer.current[data.studentId] = data as VitalsWebSocketData;
    processBufferedUpdates();
  }, [processBufferedUpdates]);

  // Update student status when backend alerts are received
  useEffect(() => {
    if (alerts.length === 0) return;
    
    setStudents(prevStudents => {
      let hasChanges = false;
      const alertStudentIds = new Set(alerts.map(alert => alert.studentId));
      
      const updatedStudents = prevStudents.map(student => {
        const shouldBeWarning = alertStudentIds.has(student.id);
        
        // Set to warning if in alerts, otherwise keep current status unless it was warning
        if (shouldBeWarning && student.status !== 'warning') {
          hasChanges = true;
          return { ...student, status: 'warning' as const };
        } else if (!shouldBeWarning && student.status === 'warning') {
          // Alert cleared, recalculate status from heart rate
          const newStatus = getHeartRateStatus(student.heartRate);
          hasChanges = true;
          return { ...student, status: newStatus };
        }
        
        return student;
      });
      
      return hasChanges ? updatedStudents : prevStudents;
    });
  }, [alerts]);

  // Check for offline students periodically
  useEffect(() => {
    const checkOfflineStudents = () => {
      setStudents(prevStudents => {
        const now = new Date();
        let hasChanges = false;
        
        const updatedStudents = prevStudents.map(student => {
          // If student has a lastUpdateTimestamp, check if they've gone offline
          if (student.lastUpdateTimestamp && student.isActive) {
            const timeSinceUpdate = now.getTime() - student.lastUpdateTimestamp.getTime();
            
            if (timeSinceUpdate > OFFLINE_TIMEOUT_MS) {
              hasChanges = true;
              return {
                ...student,
                isActive: false,
                status: 'inactive' as const,
                lastUpdate: formatRelativeTime(student.lastUpdateTimestamp.toISOString())
              };
            }
          }
          
          // Update relative time for all students
          if (student.lastUpdateTimestamp) {
            return {
              ...student,
              lastUpdate: formatRelativeTime(student.lastUpdateTimestamp.toISOString())
            };
          }
          
          return student;
        });
        
        return hasChanges ? updatedStudents : prevStudents;
      });
    };
    
    // Check every 5 seconds
    const intervalId = setInterval(checkOfflineStudents, 5000);
    
    return () => clearInterval(intervalId);
  }, []);

  // Set up WebSocket connection
  useEffect(() => {
    if (!classroomId) {
      setIsLoading(false);
      return;
    }
    
    setIsLoading(true);
    setError(null);
    
    const wsManager = WebSocketManager.getInstance();
    const subscriptions: (() => void)[] = [];
    
    // Subscribe to WebSocket events
    subscriptions.push(wsManager.subscribe('$SYSTEM/connected', () => {
      setIsConnected(true);
      toast({
        title: "Connected",
        description: "Live monitoring data is now active",
      });
    }));
    
    subscriptions.push(wsManager.subscribe('$SYSTEM/disconnected', () => {
      setIsConnected(false);
      toast({
        title: "Disconnected",
        description: "Connection to monitoring server lost",
        variant: "destructive",
      });
    }));
    
    // Subscribe to vitals updates (heart rate + oxygen saturation from mobile)
    const vitalsTopic = `/topic/classroom/${classroomId}/vitals`;
    console.log(`Subscribing to vitals topic: ${vitalsTopic}`);
    subscriptions.push(wsManager.subscribe(vitalsTopic, handleVitalsUpdate));
    
    // Fetch initial student list and start WebSocket if needed
    const fetchStudents = async () => {
      try {
        const response = await getClassroomStudents(classroomId);
        const initialStudents = response.students.map(mapApiDataToStudent);
        setStudents(initialStudents);
        
        // Start WebSocket connection if not already connected
        if (!wsManager.isConnected()) {
          // Get token from localStorage if available
          const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;
          wsManager.connect(token || undefined);
        } else {
          setIsConnected(true);
        }
      } catch (err) {
        console.error('Error fetching students:', err);
        setError('Failed to load student data. Please try again.');
        setStudents([]);
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchStudents();
    
    // Cleanup function
    return () => {
      subscriptions.forEach(unsub => unsub());
      if (updateTimeoutRef.current) {
        clearTimeout(updateTimeoutRef.current);
      }
    };
  }, [classroomId, handleVitalsUpdate, toast]);
  
  // Return the state and helper functions
  return {
    students,
    isLoading,
    error,
    isConnected,
    lastUpdateTime: lastUpdateTimeRef.current
  };
}

// Memoized student card component
const StudentCard = memo(function StudentCardBase({ student }: { student: Student }) {
  const isInactive = !student.isActive;
  const heartRateStatus = useMemo(
    () => getHeartRateStatus(student.heartRate),
    [student.heartRate]
  );
  
  return (
    <Card className={cn(
      "relative overflow-hidden transition-all hover:shadow-lg",
      "border-l-4 border-t-4 border-r-2 border-b-2",
      getStatusColor(student.status),
      isInactive && "grayscale"
    )}>
      {/* Status Indicator */}
      <div className={cn(
        "absolute top-0 left-0 right-0 h-1",
        student.status === "excellent" && "bg-green-500",
        student.status === "good" && "bg-blue-500",
        student.status === "warning" && "bg-orange-500",
        student.status === "inactive" && "bg-gray-500"
      )} />
      
      <CardHeader className="pb-2">
        <div className="flex justify-between items-start">
          <div>
            <CardTitle className="text-lg font-bold tracking-tight">
              {student.lastName}, {student.firstName}
            </CardTitle>
            <CardDescription className="font-mono text-xs">
              {student.studentNumber}
            </CardDescription>
          </div>
          <Badge variant="outline" className={cn("text-xs", getStatusBadgeClass(student.status))}>
            {student.status === "excellent" && "Excellent"}
            {student.status === "good" && "Normal"}
            {student.status === "warning" && "Warning"}
            {student.status === "inactive" && "Inactive"}
          </Badge>
        </div>
      </CardHeader>
      
      <CardContent className="pb-2">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className={cn(
              "p-2 rounded-full",
              heartRateStatus === "excellent" && "bg-green-100",
              heartRateStatus === "good" && "bg-blue-100",
              heartRateStatus === "warning" && "bg-orange-100",
              heartRateStatus === "inactive" && "bg-gray-100"
            )}>
              <Heart className={cn(
                "h-5 w-5",
                heartRateStatus === "excellent" && "text-green-600",
                heartRateStatus === "good" && "text-blue-600",
                heartRateStatus === "warning" && "animate-pulse text-orange-600",
                heartRateStatus === "inactive" && "text-gray-600"
              )} />
            </div>
            <div>
              <p className="font-bold text-lg">
                {student.heartRate ? `${student.heartRate} BPM` : "No Data"}
              </p>
            </div>
          </div>
        </div>
      </CardContent>
      
      <CardFooter className="pt-0 pb-3 text-xs text-muted-foreground border-t">
        <div className="flex w-full justify-between items-center">
          <div className="flex items-center">
            <Clock className="mr-1 h-3 w-3" />
            <span>Updated: {student.lastUpdate}</span>
          </div>
          <div className="flex items-center">
            <div className={cn(
              "h-2 w-2 rounded-full mr-1",
              student.isActive ? "bg-green-500" : "bg-gray-400"
            )} />
            <span>{student.isActive ? "Online" : "Offline"}</span>
          </div>
        </div>
      </CardFooter>
    </Card>
  );
}, (prevProps, nextProps) => {
  // Custom equality check to prevent unnecessary re-renders
  const prevStudent = prevProps.student;
  const nextStudent = nextProps.student;
  
  return (
    prevStudent.id === nextStudent.id &&
    prevStudent.heartRate === nextStudent.heartRate &&
    prevStudent.isActive === nextStudent.isActive &&
    prevStudent.lastUpdate === nextStudent.lastUpdate &&
    prevStudent.status === nextStudent.status
  );
});

export default function MonitoringPage() {
  const [selectedClassroom, setSelectedClassroom] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const { toast } = useToast();
  
  // Fetch classrooms
  const { data: classroomsData, isLoading: isLoadingClassrooms } = useQuery({
    queryKey: ['teacher-classrooms'],
    queryFn: async () => {
      try {
        return await getTeacherClassrooms();
      } catch (error) {
        console.error('Error fetching classrooms:', error);
        toast({
          title: "Error",
          description: "Failed to load classrooms. Please try again.",
          variant: "destructive",
        });
        return [];
      }
    }
  });
  
  // Use custom WebSocket hook for real-time student data
  const {
    students,
    isLoading,
    error,
    isConnected,
    lastUpdateTime
  } = useWebSocketMonitor(selectedClassroom);
  
  // Memoized filtering and sorting logic for students
  const filteredStudents = useMemo(() => {
    return students
      .filter(student => {
        // Filter by search query
        const matchesSearch = !searchQuery || 
          `${student.firstName} ${student.lastName}`.toLowerCase().includes(searchQuery.toLowerCase()) ||
          student.studentNumber.toLowerCase().includes(searchQuery.toLowerCase());
        
        // Filter by status
        const matchesStatus = statusFilter === "all" || student.status === statusFilter;
        
        return matchesSearch && matchesStatus;
      })
      .sort((a, b) => a.lastName.localeCompare(b.lastName));
  }, [students, searchQuery, statusFilter]);
  
  // Memoized stats calculation
  const stats = useMemo(() => {
    if (students.length === 0) {
      return { active: 0, inactive: 0, warning: 0, total: 0 };
    }
    
    // Single pass through students array for efficiency
    const result = students.reduce((acc, student) => {
      if (student.isActive) acc.active++;
      else acc.inactive++;
      if (student.status === "warning") acc.warning++;
      return acc;
    }, { active: 0, inactive: 0, warning: 0 });
    
    return { ...result, total: students.length };
  }, [students]);
  
  // Handle classroom selection
  const handleClassroomChange = useCallback((classroomId: string) => {
    setSelectedClassroom(classroomId);
  }, []);
  
  // Handle search input
  const handleSearchChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
  }, []);
  
  // Handle status filter
  const toggleStatusFilter = useCallback(() => {
    setStatusFilter(prev => {
      if (prev === "all") return "warning";
      return "all";
    });
  }, []);
  
  return (
    <div className="flex min-h-screen bg-background">
      <Sidebar className="hidden lg:block" />
      
      <div className="flex-1 lg:ml-64">
        <AuthNavbar />
        
        <div className="flex-1 space-y-6 p-4 pt-6 md:p-8">
          {/* Header */}
          <div className="flex flex-col space-y-4 md:flex-row md:items-center md:justify-between md:space-y-0">
            <div>
              <h1 className="text-3xl font-bold tracking-tight">Student Health Monitoring</h1>
              <p className="text-muted-foreground">
                Real-time vitals tracking • {classroomsData?.find(c => c.physicalId === selectedClassroom)?.name || 'Class'} • {lastUpdateTime.toLocaleTimeString()}
              </p>
            </div>
            
            <div className="flex flex-col md:flex-row items-end md:items-center space-y-2 md:space-y-0 md:space-x-2">
              <div className="flex items-center space-x-2">
                {/* Classroom selector */}
                <Select value={selectedClassroom || ''} onValueChange={handleClassroomChange}>
                  <SelectTrigger className="w-[180px]">
                    <SelectValue placeholder="Select classroom" />
                  </SelectTrigger>
                  <SelectContent>
                    {isLoadingClassrooms ? (
                      <SelectItem value="loading" disabled>Loading...</SelectItem>
                    ) : classroomsData?.length ? (
                      classroomsData.map((classroom) => (
                        <SelectItem key={classroom.physicalId} value={classroom.physicalId}>
                          {classroom.name}
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="no-classrooms" disabled>No classrooms</SelectItem>
                    )}
                  </SelectContent>
                </Select>
                
                {/* WebSocket connection status */}
                <Button 
                  variant="ghost" 
                  size="icon" 
                  className={cn("rounded-full", isConnected ? "text-green-500" : "text-muted-foreground")}>
                  {isConnected ? (
                    <Wifi className="h-4 w-4 animate-pulse" />
                  ) : (
                    <WifiOff className="h-4 w-4" />
                  )}
                </Button>
              </div>
              
              {/* Search */}
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search students..."
                  value={searchQuery}
                  onChange={handleSearchChange}
                  className="pl-9 md:w-[250px] lg:w-[300px]"
                />
              </div>
              
              {/* Filter button */}
              <Button 
                variant="outline" 
                size="icon" 
                onClick={toggleStatusFilter}
                className={statusFilter !== "all" ? "bg-orange-100" : ""}
              >
                <Filter className="h-4 w-4" />
              </Button>
            </div>
          </div>
          
          {/* Stats cards */}
          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <Card className="border-l-4 border-l-green-500">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Active Students</CardTitle>
                <Users className="h-4 w-4 text-green-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-green-600">{stats.active}</div>
                <p className="text-xs text-muted-foreground">
                  {Math.round((stats.active / stats.total) * 100) || 0}% of total
                </p>
              </CardContent>
            </Card>
            
            <Card className="border-l-4 border-l-orange-500">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Needs Attention</CardTitle>
                <AlertTriangle className="h-4 w-4 text-orange-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-orange-600">{stats.warning}</div>
                <p className="text-xs text-muted-foreground">High heart rate detected</p>
              </CardContent>
            </Card>
            
            <Card className="border-l-4 border-l-gray-500">
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Inactive</CardTitle>
                <UserRoundX className="h-4 w-4 text-gray-500" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-gray-600">{stats.inactive}</div>
                <p className="text-xs text-muted-foreground">No data received</p>
              </CardContent>
            </Card>
          </div>
          
          {/* Main content */}
          {isLoading ? (
            <div className="flex items-center justify-center h-60">
              <div className="flex flex-col items-center space-y-4">
                <Loader2 className="h-12 w-12 animate-spin text-primary" />
                <p className="text-lg text-muted-foreground">Loading student data...</p>
              </div>
            </div>
          ) : error ? (
            <Card className="flex flex-col items-center justify-center p-12 text-center">
              <AlertTriangle className="h-20 w-20 mb-6 text-orange-500" />
              <CardTitle className="text-2xl mb-2">Error Loading Data</CardTitle>
              <CardDescription className="text-lg">{error}</CardDescription>
              <Button
                onClick={() => setSelectedClassroom(selectedClassroom)}
                className="mt-6"
                variant="outline"
              >
                Try Again
              </Button>
            </Card>
          ) : filteredStudents.length > 0 ? (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5">
              {filteredStudents.map((student) => (
                <StudentCard key={student.id} student={student} />
              ))}
            </div>
          ) : (
            <Card className="flex flex-col items-center justify-center p-12 text-center">
              <School className="h-20 w-20 mb-6 text-muted-foreground/40" />
              <CardTitle className="text-2xl mb-2">No Students Found</CardTitle>
              <CardDescription className="text-lg">
                {searchQuery ? "Try adjusting your search criteria" : selectedClassroom ? "No students enrolled in this class" : "Please select a classroom"}
              </CardDescription>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}
