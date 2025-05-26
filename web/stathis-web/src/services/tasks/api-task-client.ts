'use client';

import {
  TaskBodyDTO,
  TaskResponseDTO,
  createTask as serverCreateTask,
  getClassroomTasks as serverGetClassroomTasks,
  getTask as serverGetTask,
  updateTaskStatus as serverUpdateTaskStatus,
  deleteTask as serverDeleteTask,
  startTask as serverStartTask,
  deactivateTask as serverDeactivateTask,
  updateTask as serverUpdateTask
} from './api-task';

/**
 * Client-side wrapper for task APIs
 */

// Task functions
export async function createTask(task: TaskBodyDTO) {
  return serverCreateTask(task);
}

export async function getClassroomTasks(classroomPhysicalId: string) {
  return serverGetClassroomTasks(classroomPhysicalId);
}

export async function getTask(physicalId: string) {
  return serverGetTask(physicalId);
}

export async function updateTaskStatus(physicalId: string, status: 'ACTIVE' | 'INACTIVE') {
  return serverUpdateTaskStatus(physicalId, status);
}

/**
 * Start a task
 * @param physicalId - Task physical ID
 * @returns Updated task data
 */
export async function startTask(physicalId: string): Promise<TaskResponseDTO> {
  return serverStartTask(physicalId);
}

/**
 * Deactivate a task
 * @param physicalId - Task physical ID
 * @returns Updated task data
 */
export async function deactivateTask(physicalId: string): Promise<TaskResponseDTO> {
  return serverDeactivateTask(physicalId);
}

/**
 * Update a task's details
 * @param physicalId - Task physical ID
 * @param taskData - Updated task data
 * @returns Updated task data
 */
export async function updateTask(physicalId: string, taskData: Partial<TaskResponseDTO>): Promise<TaskResponseDTO> {
  // Convert from TaskResponseDTO to TaskBodyDTO format
  const taskBodyDTO: TaskBodyDTO = {
    name: taskData.name || '',
    description: taskData.description || '',
    submissionDate: taskData.submissionDate || '',
    closingDate: taskData.closingDate || '',
    classroomPhysicalId: taskData.classroomPhysicalId || '',
    // Optional fields
    ...(taskData.exerciseTemplateId && { exerciseTemplateId: taskData.exerciseTemplateId }),
    ...(taskData.lessonTemplateId && { lessonTemplateId: taskData.lessonTemplateId }),
    ...(taskData.quizTemplateId && { quizTemplateId: taskData.quizTemplateId }),
    ...(taskData.maxAttempts !== undefined && { maxAttempts: taskData.maxAttempts }),
    ...(taskData.imageUrl && { imageUrl: taskData.imageUrl })
  };
  
  return serverUpdateTask(physicalId, taskBodyDTO);
}

/**
 * Delete a task
 * @param physicalId - Task physical ID
 * @returns True if deletion was successful
 */
export async function deleteTask(physicalId: string) {
  return serverDeleteTask(physicalId);
}

// Export types
export type {
  TaskBodyDTO,
  TaskResponseDTO
};
