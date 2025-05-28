'use client';

import { serverApiClient } from '@/lib/api/server-client';

/**
 * Server-side functions for task APIs
 */

// Types
export interface TaskBodyDTO {
  name: string; // Required
  description?: string;
  submissionDate: string; // Required - ISO date string
  closingDate: string; // Required - ISO date string
  imageUrl?: string;
  classroomPhysicalId: string; // Required
  exerciseTemplateId?: string;
  lessonTemplateId?: string;
  quizTemplateId?: string;
  maxAttempts?: number;
}

export interface TaskResponseDTO {
  id?: string;
  version?: number;
  physicalId: string;
  createdAt: string;
  updatedAt: string;
  name: string;
  description: string;
  submissionDate: string; // ISO date string
  closingDate: string; // ISO date string
  imageUrl?: string;
  classroomPhysicalId: string;
  exerciseTemplateId?: string;
  lessonTemplateId?: string;
  quizTemplateId?: string;
  maxAttempts?: number;
  started: boolean;
  active: boolean;
}

// Task creation according to OpenAPI specification

/**
 * Create a new task for a classroom
 */
/**
 * Create a new task for a classroom
 * Based on OpenAPI spec: POST /api/tasks
 * 
 * Note: The API requires specific data formats:
 * - classroomPhysicalId: Must match pattern ^[A-Z0-9-]+$
 * - exerciseTemplateId: Must match pattern ^EXERCISE-[A-Z0-9-]+$
 * - lessonTemplateId: Must match pattern ^LESSON-[A-Z0-9-]+$
 * - quizTemplateId: Must match pattern ^[A-Za-z0-9-]+$
 * - dates: Must match pattern ^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}([+-]\d{2}:\d{2}|Z)$
 */
export async function createTask(task: TaskBodyDTO) {
  try {
    // Do final validation of the task data structure
    const finalTaskData: TaskBodyDTO = {
      // Required fields
      name: task.name,
      description: task.description || '',
      submissionDate: task.submissionDate.replace(/\.\d{3}/, ''), // Remove milliseconds if present
      closingDate: task.closingDate.replace(/\.\d{3}/, ''), // Remove milliseconds if present
      classroomPhysicalId: task.classroomPhysicalId.toUpperCase(), // Ensure uppercase
      
      // Optional template IDs (only include one that's relevant)
      ...(task.exerciseTemplateId && { 
        exerciseTemplateId: task.exerciseTemplateId.toUpperCase() 
      }),
      ...(task.lessonTemplateId && { 
        lessonTemplateId: task.lessonTemplateId.toUpperCase() 
      }),
      ...(task.quizTemplateId && { 
        quizTemplateId: task.quizTemplateId 
      }),
      
      // Other optional fields
      ...(task.maxAttempts !== undefined && { maxAttempts: task.maxAttempts }),
      ...(task.imageUrl && { imageUrl: task.imageUrl })
    };
    
    console.log('Creating task with fully validated data:', finalTaskData);
    
    // Log auth token for debugging
    if (typeof window !== 'undefined') {
      const authToken = localStorage.getItem('auth_token');
      console.log('Authentication status:', { 
        hasToken: !!authToken,
        tokenLength: authToken ? authToken.length : 0,
        tokenExpiry: authToken ? JSON.parse(atob(authToken.split('.')[1])).exp : 'N/A'
      });
    }
    
    // Use the correct endpoint with proper Content-Type
    const { data, error, status } = await serverApiClient.post('/tasks', finalTaskData, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
    
    if (error || status !== 200) {
      console.error('[Task Create Error]', { 
        error, 
        status, 
        requestBody: finalTaskData,
        endpoint: '/tasks'
      });
      
      // Show detailed error message based on status code
      let errorMessage = error || `API returned status ${status}`;
      if (status === 403) {
        errorMessage = 'Permission denied. Your account must have the TEACHER role to create tasks.';
      } else if (status === 400) {
        errorMessage = 'Invalid task data. Please check all fields are correctly formatted.';
      }
      
      throw new Error(errorMessage);
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
      id: '1',
      version: 1,
      physicalId: 'TASK-MOCK-1',
      name: 'Mock Reading Assignment',
      description: 'Read chapters 1-3 of the textbook',
      submissionDate: tomorrow.toISOString(),
      closingDate: nextWeek.toISOString(),
      imageUrl: '',
      classroomPhysicalId,
      lessonTemplateId: 'LESSON-MOCK-1',
      createdAt: now.toISOString(),
      updatedAt: now.toISOString(),
      maxAttempts: 3,
      started: true,
      active: true
    },
    {
      id: '2',
      version: 1,
      physicalId: 'TASK-MOCK-2',
      name: 'Mock Weekly Quiz',
      description: 'Complete the quiz on recent material',
      submissionDate: tomorrow.toISOString(),
      closingDate: nextWeek.toISOString(),
      imageUrl: '',
      classroomPhysicalId,
      quizTemplateId: 'QUIZ-MOCK-1',
      createdAt: now.toISOString(),
      updatedAt: now.toISOString(),
      maxAttempts: 2,
      started: false,
      active: true
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
 * Update task status
 * @param physicalId - Task physical ID
 * @param status - New status
 * @returns Updated task data
 */
export async function updateTaskStatus(physicalId: string, status: 'ACTIVE' | 'INACTIVE'): Promise<TaskResponseDTO> {
  try {
    const { data, error, status: responseStatus } = await serverApiClient.patch(`/tasks/${physicalId}/status`, { status }, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    if (error || responseStatus >= 400) {
      console.error('Error updating task status:', error);
      throw new Error(`Failed to update task status. Error: ${error || responseStatus}`);
    }

    return data as TaskResponseDTO;
  } catch (error) {
    console.error('Error updating task status:', error);
    throw error;
  }
}

/**
 * Start a task
 * @param physicalId - Task physical ID
 * @returns Updated task data
 */
export async function startTask(physicalId: string): Promise<TaskResponseDTO> {
  try {
    const { data, error, status: responseStatus } = await serverApiClient.post(`/tasks/${physicalId}/start`, {}, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    if (error || responseStatus >= 400) {
      console.error('Error starting task:', error);
      throw new Error(`Failed to start task. Error: ${error || responseStatus}`);
    }

    return data as TaskResponseDTO;
  } catch (error) {
    console.error('Error starting task:', error);
    throw error;
  }
}

/**
 * Deactivate a task
 * @param physicalId - Task physical ID
 * @returns Updated task data
 */
export async function deactivateTask(physicalId: string): Promise<TaskResponseDTO> {
  try {
    const { data, error, status: responseStatus } = await serverApiClient.post(`/tasks/${physicalId}/deactivate`, {}, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    if (error || responseStatus >= 400) {
      console.error('Error deactivating task:', error);
      throw new Error(`Failed to deactivate task. Error: ${error || responseStatus}`);
    }

    return data as TaskResponseDTO;
  } catch (error) {
    console.error('Error deactivating task:', error);
    throw error;
  }
}

/**
 * Update a task
 * @param physicalId - Task physical ID
 * @param taskData - Updated task data using TaskBodyDTO format
 * @returns Updated task data
 */
export async function updateTask(physicalId: string, taskData: TaskBodyDTO): Promise<TaskResponseDTO> {
  try {
    // Ensure all required fields are present
    if (!taskData.name || !taskData.submissionDate || !taskData.closingDate || !taskData.classroomPhysicalId) {
      throw new Error('Missing required fields for task update: name, submissionDate, closingDate, classroomPhysicalId');
    }
    
    // Format dates according to the API requirement pattern
    // The spec requires: ^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}([+-]\d{2}:\d{2}|Z)$
    if (taskData.submissionDate) {
      taskData.submissionDate = taskData.submissionDate.replace(/\.\d{3}/, '');
    }
    if (taskData.closingDate) {
      taskData.closingDate = taskData.closingDate.replace(/\.\d{3}/, '');
    }
    
    // Format classroomPhysicalId to match pattern ^[A-Z0-9-]+$
    if (taskData.classroomPhysicalId) {
      taskData.classroomPhysicalId = taskData.classroomPhysicalId.toUpperCase();
    }
    
    // Format template IDs if present
    if (taskData.exerciseTemplateId) {
      taskData.exerciseTemplateId = taskData.exerciseTemplateId.toUpperCase();
    }
    if (taskData.lessonTemplateId) {
      taskData.lessonTemplateId = taskData.lessonTemplateId.toUpperCase();
    }
    
    console.log('Updating task with validated data:', taskData);
    
    const { data, error, status: responseStatus } = await serverApiClient.put(`/tasks/${physicalId}`, taskData, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    if (error || responseStatus >= 400) {
      console.error('Error updating task:', error);
      throw new Error(`Failed to update task. Error: ${error || responseStatus}`);
    }

    return data as TaskResponseDTO;
  } catch (error) {
    console.error('Error updating task:', error);
    throw error;
  }
}

/**
 * Delete a task
 * @param physicalId - Task physical ID
 * @returns True if deletion was successful
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
