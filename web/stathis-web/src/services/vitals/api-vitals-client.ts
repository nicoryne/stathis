'use client';

import { serverApiClient } from '@/lib/api/server-client';

/**
 * Types for the Vital Signs API
 */

export interface VitalSignsDTO {
  physicalId?: string;
  studentId?: string;
  classroomId?: string;
  taskId?: string;
  heartRate: number;           // Beats per minute
  oxygenSaturation: number;    // Oxygen saturation percentage (0-100)
  timestamp?: string;          // ISO format
  isPreActivity?: boolean;
  isPostActivity?: boolean;
}

export interface PrePostVitalSignsDTO {
  pre: VitalSignsDTO;
  post: VitalSignsDTO;
  difference: {
    heartRate: number;
    respirationRate: number;
    bloodOxygen: number;
    bloodPressure: {
      systolic: number;
      diastolic: number;
    };
    temperature: number;
  };
  taskId: string;
  studentId: string;
  activityType: string;
}

/**
 * HeartRateAlertDTO - Matches the backend DTO for heart rate alerts
 */
export interface HeartRateAlertDTO {
  studentId: string;
  studentName: string;
  currentHeartRate: number;
  thresholdHeartRate: number;
  alertMessage: string;
  timestamp: string; // ISO date string
}

/**
 * Get student vital signs for a specific task
 */
export async function getStudentVitalSigns(taskId: string, studentId: string): Promise<VitalSignsDTO> {
  console.log(`Fetching vital signs for student: ${studentId}, task: ${taskId}`);
  
  try {
    const { data, error, status } = await serverApiClient.get(`/vital-signs/${taskId}/${studentId}`);
    
    if (error || status >= 400) {
      const errorMessage = typeof error === 'object' && error !== null && 'message' in error
        ? (error as { message: string }).message
        : `Failed to fetch vital signs: ${status}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }
    
    // Validate the data structure
    if (!data || typeof data !== 'object') {
      console.error('Invalid vital signs data structure received:', data);
      throw new Error('Invalid vital signs data structure');
    }
    
    // Cast data to a more specific type to work with it
    const responseData = data as Partial<VitalSignsDTO>;
    
    // Ensure the data has the required fields
    if (typeof responseData.heartRate !== 'number' || typeof responseData.oxygenSaturation !== 'number') {
      console.warn('Vital signs data missing required fields:', data);
      // Fill in defaults for missing fields to prevent UI errors
      const fallbackData: VitalSignsDTO = {
        heartRate: typeof responseData.heartRate === 'number' ? responseData.heartRate : 0,
        oxygenSaturation: typeof responseData.oxygenSaturation === 'number' ? responseData.oxygenSaturation : 0,
        // Preserve any valid fields from the response
        physicalId: responseData.physicalId,
        studentId: responseData.studentId || studentId,
        taskId: responseData.taskId || taskId,
        classroomId: responseData.classroomId,
        timestamp: responseData.timestamp || new Date().toISOString(),
        isPreActivity: responseData.isPreActivity,
        isPostActivity: responseData.isPostActivity
      };
      return fallbackData;
    }
    
    // Ensure studentId and taskId are properly set
    const validatedData: VitalSignsDTO = {
      ...responseData as VitalSignsDTO,
      studentId: responseData.studentId || studentId,
      taskId: responseData.taskId || taskId
    };
    
    console.log('Successfully fetched vital signs data:', validatedData);
    return validatedData;
  } catch (err) {
    console.error('Error in getStudentVitalSigns:', err);
    throw err;
  }
}

/**
 * Get pre and post activity vital signs comparison
 */
export async function getPrePostVitalSigns(taskId: string, studentId: string): Promise<PrePostVitalSignsDTO> {
  console.log(`Fetching pre/post vital signs for student: ${studentId}, task: ${taskId}`);
  
  try {
    const { data, error, status } = await serverApiClient.get(`/vital-signs/pre-post/${taskId}/${studentId}`);
    
    if (error || status >= 400) {
      const errorMessage = typeof error === 'object' && error !== null && 'message' in error
        ? (error as { message: string }).message
        : `Failed to fetch pre/post vital signs: ${status}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }
    
    // Validate the data structure
    if (!data || typeof data !== 'object' || !('pre' in data) || !('post' in data)) {
      console.error('Invalid pre/post vital signs data structure received:', data);
      throw new Error('Invalid pre/post vital signs data structure');
    }
    
    console.log('Successfully fetched pre/post vital signs data');
    return data as PrePostVitalSignsDTO;
  } catch (err) {
    console.error('Error in getPrePostVitalSigns:', err);
    throw err;
  }
}

/**
 * Get heart rate alerts for a classroom
 */
export async function getHeartRateAlerts(classroomId: string): Promise<HeartRateAlertDTO[]> {
  console.log(`Fetching heart rate alerts for classroom: ${classroomId}`);
  
  try {
    const { data, error, status } = await serverApiClient.get(`/vital-signs/alerts/${classroomId}`);
    
    if (error || status >= 400) {
      const errorMessage = typeof error === 'object' && error !== null && 'message' in error
        ? (error as { message: string }).message
        : `Failed to fetch heart rate alerts: ${status}`;
      console.error(errorMessage);
      throw new Error(errorMessage);
    }
    
    // Validate the data structure
    if (!data || !Array.isArray(data)) {
      console.error('Invalid heart rate alerts data structure received:', data);
      return [];
    }
    
    // Process and return the alerts
    return data.map((alert: any) => ({
      studentId: alert.studentId || '',
      studentName: alert.studentName || 'Unknown Student',
      currentHeartRate: typeof alert.currentHeartRate === 'number' ? alert.currentHeartRate : 0,
      thresholdHeartRate: typeof alert.thresholdHeartRate === 'number' ? alert.thresholdHeartRate : 0,
      alertMessage: alert.alertMessage || 'Heart rate alert',
      timestamp: alert.timestamp || new Date().toISOString()
    }));
  } catch (err) {
    console.error('Error in getHeartRateAlerts:', err);
    return [];
  }
}

/**
 * Get real-time vital signs for a student (simulated)
 * This would typically connect to a WebSocket in a real implementation
 */
export function simulateRealTimeVitalSigns(baseVitals?: Partial<VitalSignsDTO>): VitalSignsDTO {
  const base = baseVitals || {
    heartRate: 75,
    oxygenSaturation: 98
  };
  
  // Add small random variations to simulate real-time changes
  return {
    physicalId: `VITAL-${Math.random().toString(36).substring(2, 15)}`,
    studentId: baseVitals?.studentId || 'default-student',
    classroomId: baseVitals?.classroomId || 'default-classroom',
    taskId: baseVitals?.taskId || 'default-task',
    heartRate: Math.round(base.heartRate! + (Math.random() * 6 - 3)),
    oxygenSaturation: Math.round(Math.min(100, Math.max(94, base.oxygenSaturation! + (Math.random() * 1 - 0.5)))),
    timestamp: new Date().toISOString(),
    isPreActivity: baseVitals?.isPreActivity || false,
    isPostActivity: baseVitals?.isPostActivity || false
  };
}
