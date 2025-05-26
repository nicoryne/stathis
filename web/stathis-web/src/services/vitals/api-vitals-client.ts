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
 * Get student vital signs for a specific task
 */
export async function getStudentVitalSigns(taskId: string, studentId: string): Promise<VitalSignsDTO> {
  const { data, error, status } = await serverApiClient.get(`/vital-signs/${taskId}/${studentId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch vital signs: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as VitalSignsDTO;
}

/**
 * Get pre and post activity vital signs comparison
 */
export async function getPrePostVitalSigns(taskId: string, studentId: string): Promise<PrePostVitalSignsDTO> {
  const { data, error, status } = await serverApiClient.get(`/vital-signs/pre-post/${taskId}/${studentId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch pre/post vital signs: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as PrePostVitalSignsDTO;
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
