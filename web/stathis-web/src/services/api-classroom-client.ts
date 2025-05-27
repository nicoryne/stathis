'use client';

import {
  ClassroomBodyDTO,
  ClassroomResponseDTO,
  StudentListResponseDTO,
  createClassroom as serverCreateClassroom,
  getClassroomById as serverGetClassroomById,
  getTeacherClassrooms as serverGetTeacherClassrooms,
  updateClassroom as serverUpdateClassroom,
  deleteClassroom as serverDeleteClassroom,
  activateClassroom as serverActivateClassroom,
  deactivateClassroom as serverDeactivateClassroom,
  getClassroomStudents as serverGetClassroomStudents,
  verifyClassroomStudent as serverVerifyClassroomStudent,
  enrollInClassroom as serverEnrollInClassroom
} from './api-classroom';

/**
 * Client-side wrapper for classroom API
 */

export async function createClassroom(classroom: ClassroomBodyDTO) {
  return serverCreateClassroom(classroom);
}

export async function getClassroomById(physicalId: string) {
  return serverGetClassroomById(physicalId);
}

export async function getTeacherClassrooms() {
  return serverGetTeacherClassrooms();
}

export async function updateClassroom(physicalId: string, updates: Partial<ClassroomBodyDTO>) {
  return serverUpdateClassroom(physicalId, updates);
}

export async function deleteClassroom(physicalId: string) {
  return serverDeleteClassroom(physicalId);
}

export async function activateClassroom(physicalId: string) {
  return serverActivateClassroom(physicalId);
}

export async function deactivateClassroom(physicalId: string) {
  return serverDeactivateClassroom(physicalId);
}

export async function getClassroomStudents(classroomPhysicalId: string) {
  return serverGetClassroomStudents(classroomPhysicalId);
}

export async function verifyClassroomStudent(classroomId: string, studentId: string) {
  return serverVerifyClassroomStudent(classroomId, studentId);
}

export async function enrollInClassroom(classroomPhysicalId: string) {
  return serverEnrollInClassroom(classroomPhysicalId);
}

// Type exports for use in components
export type { ClassroomBodyDTO, ClassroomResponseDTO, StudentListResponseDTO };
