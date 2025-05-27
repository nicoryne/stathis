'use client';

import { serverApiClient } from '@/lib/api/server-client';

export interface Task {
  physicalId: string;
  name: string;
  description: string;
  submissionDate: string;
  closingDate: string;
  imageUrl?: string;
  classroomPhysicalId: string;
  exerciseTemplateId?: string;
  lessonTemplateId?: string;
  quizTemplateId?: string;
  maxAttempts?: number;
  started: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Get tasks for a specific classroom
 */
export async function getTasksByClassroom(classroomId: string): Promise<Task[]> {
  const { data, error, status } = await serverApiClient.get(`/tasks/classroom/${classroomId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch classroom tasks: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as Task[];
}

/**
 * Get a task by its physical ID
 */
export async function getTask(physicalId: string): Promise<Task> {
  const { data, error, status } = await serverApiClient.get(`/tasks/${physicalId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch task: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as Task;
}

/**
 * Get active tasks for a specific classroom
 */
export async function getActiveTasksByClassroom(classroomId: string): Promise<Task[]> {
  const { data, error, status } = await serverApiClient.get(`/tasks/classroom/${classroomId}/active`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch active classroom tasks: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as Task[];
}
