'use client';

import React, { useState, useEffect } from 'react';
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
import { VitalSignsDTO } from '@/services/vitals/api-vitals-client';
import { useVitalSigns } from '@/lib/websocket/use-vital-signs';
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
    refreshData
  } = useVitalSigns(selectedStudent, selectedTask);
  
  // Helper function to determine color based on vital sign value
  const getVitalStatusColor = (value: number, type: string): string => {
    switch (type) {
      case 'heartRate':
        return value < 60 || value > 100 ? 'text-red-500' : 'text-green-500';
      case 'respirationRate':
        return value < 12 || value > 20 ? 'text-red-500' : 'text-green-500';
      case 'bloodOxygen':
        return value < 95 ? 'text-red-500' : 'text-green-500';
      case 'systolic':
        return value < 90 || value > 140 ? 'text-red-500' : 'text-green-500';
      case 'diastolic':
        return value < 60 || value > 90 ? 'text-red-500' : 'text-green-500';
      case 'temperature':
        return value < 36 || value > 37.5 ? 'text-red-500' : 'text-green-500';
      default:
        return 'text-green-500';
    }
  };
  
  return (
    <DashboardShell>
      <DashboardHeader heading="Vital Signs Monitoring" text="Monitor student vital signs during activities">
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
          
          <Button variant="outline" size="sm" disabled={!selectedStudent}>
            <Clock className="mr-2 h-4 w-4" />
            View History
          </Button>
        </div>
      </DashboardHeader>

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
        {wsError && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded relative" role="alert">
            <strong className="font-bold">Connection Error: </strong>
            <span className="block sm:inline">{wsError}</span>
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
                  {vitalSigns.heartRate} <span className="text-sm font-normal">BPM</span>
                </div>
                <p className="text-xs text-muted-foreground">
                  Normal range: 60-100 BPM
                </p>
              </CardContent>
            </Card>

            {/* Respiration Rate */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Respiration Rate</CardTitle>
                <Wind className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className={`text-2xl font-bold ${getVitalStatusColor(vitalSigns.respirationRate, 'respirationRate')}`}>
                  {vitalSigns.respirationRate} <span className="text-sm font-normal">breaths/min</span>
                </div>
                <p className="text-xs text-muted-foreground">
                  Normal range: 12-20 breaths/min
                </p>
              </CardContent>
            </Card>

            {/* Blood Oxygen */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Blood Oxygen</CardTitle>
                <Droplet className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className={`text-2xl font-bold ${getVitalStatusColor(vitalSigns.bloodOxygen, 'bloodOxygen')}`}>
                  {vitalSigns.bloodOxygen}%
                </div>
                <p className="text-xs text-muted-foreground">
                  Normal range: ≥95%
                </p>
              </CardContent>
            </Card>

            {/* Blood Pressure */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Blood Pressure</CardTitle>
                <Activity className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="flex items-baseline gap-1">
                  <span className={`text-2xl font-bold ${getVitalStatusColor(vitalSigns.bloodPressure.systolic, 'systolic')}`}>
                    {vitalSigns.bloodPressure.systolic}
                  </span>
                  <span>/</span>
                  <span className={`text-2xl font-bold ${getVitalStatusColor(vitalSigns.bloodPressure.diastolic, 'diastolic')}`}>
                    {vitalSigns.bloodPressure.diastolic}
                  </span>
                  <span className="text-sm font-normal ml-1">mmHg</span>
                </div>
                <p className="text-xs text-muted-foreground">
                  Normal range: 90-140/60-90 mmHg
                </p>
              </CardContent>
            </Card>

            {/* Temperature */}
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium">Temperature</CardTitle>
                <Thermometer className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className={`text-2xl font-bold ${getVitalStatusColor(vitalSigns.temperature, 'temperature')}`}>
                  {vitalSigns.temperature}°C
                </div>
                <p className="text-xs text-muted-foreground">
                  Normal range: 36.0-37.5°C
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
    </DashboardShell>
  );
}
