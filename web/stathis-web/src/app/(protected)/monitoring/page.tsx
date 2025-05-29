'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { DashboardShell } from '@/components/dashboard/dashboard-shell';
import { DashboardHeader } from '@/components/dashboard/dashboard-header';
import { 
  Card, 
  CardContent, 
  CardDescription, 
  CardHeader, 
  CardTitle 
} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from '@/components/ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Activity, 
  Heart, 
  Thermometer, 
  Wind, 
  Droplet, 
  Clock
} from 'lucide-react';
import { Sidebar } from '@/components/dashboard/sidebar';
import { AuthNavbar } from '@/components/auth-navbar';
import { VitalSignsDTO, HeartRateAlertDTO } from '@/services/vitals/api-vitals-client';
import { useVitalSigns } from '@/lib/websocket/use-vital-signs';
import { useHeartRateAlerts } from '@/lib/websocket/use-heart-rate-alerts';
import { getTeacherClassrooms, getClassroomStudents } from '@/services/api-classroom';
import { getTasksByClassroom } from '@/services/api-task-client';

export default function MonitoringPage() {
  const [selectedClassroom, setSelectedClassroom] = useState('');
  const [selectedTask, setSelectedTask] = useState('');
  const [selectedStudent, setSelectedStudent] = useState('');
  // Fetch classrooms
  const { data: classroomsData } = useQuery({
    queryKey: ['teacher-classrooms'],
    queryFn: async () => {
      try {
        return await getTeacherClassrooms();
      } catch (error) {
        console.error('Error fetching classrooms:', error);
        return [];
      }
    }
  });
  
  // Fetch tasks for selected classroom
  const { data: tasksData } = useQuery({
    queryKey: ['classroom-tasks', selectedClassroom],
    queryFn: async () => {
      if (!selectedClassroom) return [];
      try {
        return await getTasksByClassroom(selectedClassroom);
      } catch (error) {
        console.error('Error fetching tasks:', error);
        return [];
      }
    },
    enabled: !!selectedClassroom
  });
  
  // Fetch students for selected classroom
  const { data: studentsData, isLoading: isStudentsLoading } = useQuery({
    queryKey: ['classroom-students', selectedClassroom],
    queryFn: async () => {
      if (!selectedClassroom) return { students: [] };
      try {
        const response = await getClassroomStudents(selectedClassroom);
        return response;
      } catch (error) {
        console.error('Error fetching students:', error);
        return { students: [] };
      }
    },
    enabled: !!selectedClassroom
  });
  
  // Use real-time vital signs from WebSocket
  const { 
    vitalSigns, 
    isConnected, 
    error: wsError, 
    lastUpdated,
    refreshData,
    subscriptionTopics
  } = useVitalSigns(selectedStudent, selectedTask);
  
  // Use heart rate alerts for the selected classroom
  const {
    alerts: heartRateAlerts,
    clearAlert,
    clearAllAlerts,
    error: alertsError
  } = useHeartRateAlerts(selectedClassroom);
  
  // Debug information for development
  useEffect(() => {
    if (isConnected) {
      console.log('WebSocket connected, subscribed to topics:', subscriptionTopics);
    }
    
    if (vitalSigns) {
      console.log('Received vital signs update:', vitalSigns);
    }
  }, [isConnected, vitalSigns, subscriptionTopics]);
  
  // Helper function to determine color based on vital sign value
  const getVitalStatusColor = (value: number, type: string): string => {
    switch (type) {
      case 'heartRate':
        return value < 60 || value > 100 ? 'text-red-500' : 'text-green-500';
      case 'oxygenSaturation':
        return value < 95 ? 'text-red-500' : 'text-green-500';
      default:
        return 'text-green-500';
    }
  };
  
  // Only use the actual vital signs available from the backend
  // The backend provides only heart rate and oxygen saturation
  
  // No need to transform the data - use it as is
  
  return (
    <div className="flex min-h-screen">
      <Sidebar className="w-64 flex-shrink-0" />

      <div className="flex-1">
        <AuthNavbar />
        
        <main className="p-6">
          <div className="mb-6 flex flex-col space-y-2 md:flex-row md:items-center md:justify-between md:space-y-0">
            <div>
              <h1 className="text-2xl font-bold tracking-tight">Vital Signs Monitoring</h1>
              <p className="text-muted-foreground mt-1">Monitor student vital signs during activities</p>
            </div>
            <div className="flex items-center gap-2">
              <Button 
                variant="outline" 
                size="sm" 
                disabled={!selectedStudent || !isConnected}
                onClick={refreshData}
              >
                <Activity className="mr-2 h-4 w-4" />
                Refresh Now
              </Button>
              
              <Button 
                variant="outline" 
                size="sm" 
                disabled={!selectedStudent || heartRateAlerts.length === 0}
                onClick={() => clearAllAlerts()}
              >
                <Clock className="mr-2 h-4 w-4" />
                Clear Alerts
              </Button>
            </div>
          </div>

          <div className="grid gap-6">
            {/* Selection Controls */}
            <Card>
              <CardHeader>
                <CardTitle>Monitoring Controls</CardTitle>
                <CardDescription>Select classroom, task, and student to monitor</CardDescription>
              </CardHeader>
              <CardContent>
            <div className="grid gap-4 sm:grid-cols-3">
              <div>
                <label className="text-sm font-medium mb-2 block">Classroom</label>
                <Select value={selectedClassroom} onValueChange={setSelectedClassroom}>
                  <SelectTrigger>
                    <SelectValue placeholder="Select classroom" />
                  </SelectTrigger>
                  <SelectContent>
                    {classroomsData?.map(classroom => (
                      <SelectItem key={classroom.physicalId} value={classroom.physicalId}>
                        {classroom.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label className="text-sm font-medium mb-2 block">Task</label>
                <Select 
                  value={selectedTask} 
                  onValueChange={setSelectedTask}
                  disabled={!selectedClassroom}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select task" />
                  </SelectTrigger>
                  <SelectContent>
                    {tasksData?.map(task => (
                      <SelectItem key={task.physicalId} value={task.physicalId}>
                        {task.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label className="text-sm font-medium mb-2 block">Student</label>
                <Select 
                  value={selectedStudent} 
                  onValueChange={setSelectedStudent}
                  disabled={!selectedTask || isStudentsLoading || !studentsData?.students?.length}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select student" />
                  </SelectTrigger>
                  <SelectContent>
                    {isStudentsLoading ? (
                      <SelectItem value="loading" disabled>Loading students...</SelectItem>
                    ) : studentsData?.students?.length ? (
                      studentsData.students.map(student => (
                        <SelectItem key={student.physicalId} value={student.physicalId}>
                          {student.firstName} {student.lastName}
                        </SelectItem>
                      ))
                    ) : (
                      <SelectItem value="no-students" disabled>No students found</SelectItem>
                    )}
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Vital Signs Display */}
        {/* Error messages */}
        {(wsError || alertsError) && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative" role="alert">
            <strong className="font-bold">Connection Error: </strong>
            <span className="block sm:inline">{wsError || alertsError}</span>
          </div>
        )}
        
        {/* Heart Rate Alerts Section */}
        {heartRateAlerts.length > 0 && (
          <div className="mb-6">
            <h2 className="text-lg font-semibold mb-3 flex items-center">
              <Heart className="h-5 w-5 text-red-500 mr-2" />
              Heart Rate Alerts
            </h2>
            <div className="space-y-2">
              {heartRateAlerts.map((alert, index) => (
                <div 
                  key={`${alert.studentId}-${alert.timestamp}-${index}`}
                  className="bg-red-50 border border-red-200 rounded-md p-3 flex justify-between items-center"
                >
                  <div>
                    <p className="font-medium">{alert.studentName}</p>
                    <p className="text-sm text-red-700">{alert.alertMessage}</p>
                    <p className="text-xs text-gray-500 mt-1">
                      {new Date(alert.timestamp).toLocaleTimeString()}
                    </p>
                  </div>
                  <Button 
                    variant="ghost" 
                    size="sm"
                    onClick={() => clearAlert(`${alert.studentId}${alert.timestamp}`)}
                  >
                    Dismiss
                  </Button>
                </div>
              ))}
            </div>
          </div>
        )}
        
        {isConnected && (
          <div className="flex items-center gap-2 mb-4">
            <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
            <span className="text-sm text-muted-foreground">
              Connected to real-time monitoring
              {lastUpdated && ` · Last updated: ${lastUpdated.toLocaleTimeString()}`}
            </span>
          </div>
        )}
        
        {selectedStudent && selectedTask && !isConnected && (
          <div className="flex items-center gap-2 mb-4 text-amber-600">
            <div className="w-3 h-3 bg-amber-500 rounded-full"></div>
            <span className="text-sm">Connecting to monitoring system...</span>
          </div>
        )}
        
        {/* Show debugging info in development */}
        {process.env.NODE_ENV === 'development' && selectedStudent && selectedTask && (
          <div className="bg-slate-50 p-4 rounded mb-4 text-xs font-mono overflow-auto max-h-40">
            <details>
              <summary className="cursor-pointer font-semibold mb-2">Connection Debug Info</summary>
              <div>
                <p><strong>Connection Status:</strong> {isConnected ? 'Connected' : 'Disconnected'}</p>
                <p><strong>Student ID:</strong> {selectedStudent}</p>
                <p><strong>Task ID:</strong> {selectedTask}</p>
                <p><strong>Last Updated:</strong> {lastUpdated?.toISOString() || 'Never'}</p>
                <p><strong>Subscribed Topics:</strong></p>
                <ul className="list-disc pl-4">
                  {subscriptionTopics.map((topic, i) => (
                    <li key={i}>{topic}</li>
                  ))}
                </ul>
              </div>
            </details>
          </div>
        )}
        
        {selectedStudent && vitalSigns ? (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {/* Heart Rate */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Heart Rate</CardTitle>
                <Heart className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className={`text-2xl font-bold ${getVitalStatusColor(vitalSigns.heartRate, 'heartRate')}`}>
                  {vitalSigns.heartRate ?? 'N/A'} <span className="text-sm font-normal">BPM</span>
                </div>
                <p className="text-xs text-muted-foreground">
                  Normal range: 60-100 BPM
                </p>
                {vitalSigns.isPreActivity && (
                  <div className="mt-1 inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    Pre-Activity
                  </div>
                )}
                {vitalSigns.isPostActivity && (
                  <div className="mt-1 inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
                    Post-Activity
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Blood Oxygen / Oxygen Saturation */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Blood Oxygen</CardTitle>
                <Droplet className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className={`text-2xl font-bold ${getVitalStatusColor(vitalSigns.oxygenSaturation, 'oxygenSaturation')}`}>
                  {vitalSigns.oxygenSaturation ?? 'N/A'}%
                </div>
                <p className="text-xs text-muted-foreground">
                  Normal range: ≥95%
                </p>
              </CardContent>
            </Card>

            {/* Last Updated */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Last Updated</CardTitle>
                <Clock className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-xl font-medium">
                  {lastUpdated ? lastUpdated.toLocaleTimeString() : 'N/A'}
                </div>
                <p className="text-xs text-muted-foreground">
                  {lastUpdated ? lastUpdated.toLocaleDateString() : 'No data yet'}
                </p>
              </CardContent>
            </Card>
          </div>
        ) : selectedStudent ? (
          <Card>
            <CardContent className="py-10">
              <div className="text-center text-muted-foreground">
                <Activity className="mx-auto h-12 w-12 mb-4 text-muted-foreground/50" />
                <h3 className="text-lg font-medium mb-2">Loading vital signs...</h3>
                <p>Connecting to monitoring system</p>
              </div>
            </CardContent>
          </Card>
        ) : (
          <Card>
            <CardContent className="py-10">
              <div className="text-center text-muted-foreground">
                <Activity className="mx-auto h-12 w-12 mb-4 text-muted-foreground/50" />
                <h3 className="text-lg font-medium mb-2">No student selected</h3>
                <p>Select a classroom, task, and student to view vital signs</p>
              </div>
            </CardContent>
          </Card>
        )}
          </div>
        </main>
      </div>
    </div>
  );
}
