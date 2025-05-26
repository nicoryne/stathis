'use client';

import {
  TaskBodyDTO,
  TaskResponseDTO,
  createTask as serverCreateTask,
  getClassroomTasks as serverGetClassroomTasks,
  getTask as serverGetTask,
  updateTaskStatus as serverUpdateTaskStatus,
  deleteTask as serverDeleteTask
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

export async function deleteTask(physicalId: string) {
  return serverDeleteTask(physicalId);
}

// Export types
export type {
  TaskBodyDTO,
  TaskResponseDTO
};
