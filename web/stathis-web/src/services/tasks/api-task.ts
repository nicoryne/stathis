'use client';

import { serverApiClient } from '@/lib/api/server-client';

/**
 * Server-side functions for task APIs
 */

// Types
export interface TaskBodyDTO {
  title: string;
  description: string;
  classroomPhysicalId: string;
  dueDate: string; // ISO date string
  templatePhysicalId: string;
  templateType: 'LESSON' | 'QUIZ' | 'EXERCISE';
  points: number;
}

export interface TaskResponseDTO {
  physicalId: string;
  title: string;
  description: string;
  classroomPhysicalId: string;
  dueDate: string; // ISO date string
  templatePhysicalId: string;
  templateType: 'LESSON' | 'QUIZ' | 'EXERCISE';
  createdAt: string;
  updatedAt: string;
  points: number;
  status: 'ACTIVE' | 'INACTIVE';
}

/**
 * Create a new task for a classroom
 */
export async function createTask(task: TaskBodyDTO) {
  try {
    console.log('Creating task:', task);
    
    const { data, error, status } = await serverApiClient.post('/tasks', task);
    
    if (error) {
      console.error('[Task Create Error]', { error, status, requestBody: task });
      throw new Error(error);
    }
    
    return data as TaskResponseDTO;
  } catch (error) {
    console.error('Error creating task:', error);
    throw error;
  }
}

/**
 * Get all tasks for a classroom
 */
export async function getClassroomTasks(classroomPhysicalId: string) {
  try {
    const { data, error, status } = await serverApiClient.get(`/tasks/classroom/${classroomPhysicalId}`);
    
    if (error) {
      console.error('[Tasks Get Error]', { error, status });
      
      // If we get a 403 error, return mock data for development
      if (status === 403) {
        console.warn('Using mock data for tasks due to 403 error');
        return getMockTasks(classroomPhysicalId);
      }
      
      throw new Error(error);
    }
    
    return data as TaskResponseDTO[];
  } catch (error) {
    console.error('Error getting classroom tasks:', error);
    
    // Return mock data for any error during development
    if (process.env.NODE_ENV !== 'production') {
      console.warn('Using mock data for tasks due to error');
      return getMockTasks(classroomPhysicalId);
    }
    
    throw error;
  }
}

/**
 * Generate mock tasks for development
 */
function getMockTasks(classroomPhysicalId: string): TaskResponseDTO[] {
  const now = new Date();
  const tomorrow = new Date(now);
  tomorrow.setDate(tomorrow.getDate() + 1);
  
  const nextWeek = new Date(now);
  nextWeek.setDate(nextWeek.getDate() + 7);
  
  return [
    {
      physicalId: 'TASK-MOCK-1',
      title: 'Mock Reading Assignment',
      description: 'Read chapters 1-3 of the textbook',
      classroomPhysicalId,
      dueDate: tomorrow.toISOString(),
      templatePhysicalId: 'LESSON-MOCK-1',
      templateType: 'LESSON',
      createdAt: now.toISOString(),
      updatedAt: now.toISOString(),
      points: 10,
      status: 'ACTIVE'
    },
    {
      physicalId: 'TASK-MOCK-2',
      title: 'Mock Weekly Quiz',
      description: 'Complete the quiz on recent material',
      classroomPhysicalId,
      dueDate: nextWeek.toISOString(),
      templatePhysicalId: 'QUIZ-MOCK-1',
      templateType: 'QUIZ',
      createdAt: now.toISOString(),
      updatedAt: now.toISOString(),
      points: 25,
      status: 'ACTIVE'
    }
  ];
}

/**
 * Get a task by ID
 */
export async function getTask(physicalId: string) {
  try {
    const { data, error, status } = await serverApiClient.get(`/tasks/${physicalId}`);
    
    if (error) {
      console.error('[Task Get Error]', { error, status });
      throw new Error(error);
    }
    
    return data as TaskResponseDTO;
  } catch (error) {
    console.error('Error getting task:', error);
    throw error;
  }
}

/**
 * Update a task's status
 */
export async function updateTaskStatus(physicalId: string, status: 'ACTIVE' | 'INACTIVE') {
  try {
    const { data, error, status: statusCode } = await serverApiClient.put(`/tasks/${physicalId}/status`, { status });
    
    if (error) {
      console.error('[Task Status Update Error]', { error, statusCode });
      throw new Error(error);
    }
    
    return data as TaskResponseDTO;
  } catch (error) {
    console.error('Error updating task status:', error);
    throw error;
  }
}

/**
 * Delete a task
 */
export async function deleteTask(physicalId: string) {
  try {
    const { data, error, status } = await serverApiClient.delete(`/tasks/${physicalId}`);
    
    if (error) {
      console.error('[Task Delete Error]', { error, status });
      throw new Error(error);
    }
    
    return true;
  } catch (error) {
    console.error('Error deleting task:', error);
    throw error;
  }
}
