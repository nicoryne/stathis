'use client';

import { serverApiClient, API_BASE_URL } from '@/lib/api/server-client';
import { getCurrentUserPhysicalId, getCurrentUserEmail, getCurrentUserRole } from '@/lib/utils/jwt';

/**
 * Server-side functions for classroom API
 */

// Types
export interface ClassroomBodyDTO {
  name: string;
  description: string;
}


export interface ClassroomResponseDTO {
  physicalId: string;
  name: string;
  description: string;
  teacherId: string;
  active: boolean; // Changed from isActive to match backend
  classroomCode: string; // Added field from backend
  createdAt: string;
  updatedAt: string;
  teacherName?: string; // Optional field that might be returned
  studentCount?: number; // Optional field that might be returned
}

export interface StudentListResponseDTO {
  students: {
    physicalId: string;
    firstName: string;
    lastName: string;
    email: string;
    profilePictureUrl?: string;
    isVerified: boolean;
  }[];
}

/**
 * Create a new classroom
 */
export async function createClassroom(classroom: ClassroomBodyDTO) {
  // The backend derives teacherId from the security context
  // We only need to send name and description
  try {
    // Log authentication info for debugging
    console.log('Current user role:', getCurrentUserRole());
    console.log('Current user email:', getCurrentUserEmail());
    console.log('Current user physical ID:', getCurrentUserPhysicalId());
    
    // Using the serverApiClient with detailed logging
    console.log('Request payload:', classroom);
    
    // Make the API request - send only name and description
    // The backend will automatically assign the teacher ID from the security context
    const { data, error, status } = await serverApiClient.post('/classrooms', classroom);
    
    if (error) {
      console.error('[Classroom Create Error]', { error, status, requestBody: classroom });
      throw new Error(error);
    }
    
    return data as ClassroomResponseDTO;
  } catch (error) {
    console.error('Error creating classroom:', error);
    throw error;
  }
}

/**
 * Get a classroom by ID
 */
export async function getClassroomById(physicalId: string) {
  const { data, error, status } = await serverApiClient.get(`/classrooms/${physicalId}`);
  
  if (error) {
    console.error('[Classroom Get Error]', { error, status });
    throw new Error(error);
  }
  
  return data as ClassroomResponseDTO;
}

/**
 * Get all classrooms for the current authenticated teacher
 * Uses the security context in the backend to determine the teacher
 */
export async function getTeacherClassrooms() {
  // Log for debugging purposes
  console.log('Getting classrooms for current teacher');
  
  // Get current user info for debugging
  const userEmail = getCurrentUserEmail();
  const userRole = getCurrentUserRole();
  console.log('Current user info:', { userEmail, userRole });
  
  // Print auth token (partially masked for security)
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('auth_token');
    if (token) {
      const maskedToken = token.substring(0, 15) + '...' + token.substring(token.length - 10);
      console.log('Using auth token (masked):', maskedToken);
    }
  }
  
  // Use the /teacher endpoint which is designed to use the security context
  const { data, error, status } = await serverApiClient.get('/classrooms/teacher', {
    // Add explicit headers for debugging
    headers: {
      'Accept': 'application/json'
    }
  });
  
  // Log the full error details
  if (error) {
    console.error('[Teacher Classrooms Get Error]', { error, status });
    throw new Error(error);
  }
  
  return data as ClassroomResponseDTO[];
}

/**
 * Update a classroom
 */
export async function updateClassroom(physicalId: string, updates: Partial<ClassroomBodyDTO>) {
  const { data, error, status } = await serverApiClient.patch(`/classrooms/${physicalId}`, updates);
  
  if (error) {
    console.error('[Classroom Update Error]', { error, status });
    throw new Error(error);
  }
  
  return data as ClassroomResponseDTO;
}

/**
 * Delete a classroom
 */
export async function deleteClassroom(physicalId: string) {
  const { error, status } = await serverApiClient.delete(`/classrooms/${physicalId}`);
  
  if (error) {
    console.error('[Classroom Delete Error]', { error, status });
    throw new Error(error);
  }
  
  return true;
}

/**
 * Activate a classroom
 */
export async function activateClassroom(physicalId: string) {
  const { data, error, status } = await serverApiClient.post(`/classrooms/${physicalId}/activate`);
  
  if (error) {
    console.error('[Classroom Activate Error]', { error, status });
    throw new Error(error);
  }
  
  return data as ClassroomResponseDTO;
}

/**
 * Deactivate a classroom
 */
export async function deactivateClassroom(physicalId: string) {
  const { data, error, status } = await serverApiClient.post(`/classrooms/${physicalId}/deactivate`);
  
  if (error) {
    console.error('[Classroom Deactivate Error]', { error, status });
    throw new Error(error);
  }
  
  return data as ClassroomResponseDTO;
}

/**
 * Get students in a classroom
 */
export async function getClassroomStudents(classroomPhysicalId: string) {
  const { data, error, status } = await serverApiClient.get(`/classrooms/${classroomPhysicalId}/students`);
  
  if (error) {
    console.error('[Classroom Students Get Error]', { error, status });
    throw new Error(error);
  }
  
  return data as StudentListResponseDTO;
}

/**
 * Verify a student in a classroom
 */
export async function verifyClassroomStudent(classroomPhysicalId: string, studentId: string) {
  const { data, error, status } = await serverApiClient.post(`/classrooms/${classroomPhysicalId}/students/${studentId}/verify`);
  
  if (error) {
    console.error('[Student Verify Error]', { error, status });
    throw new Error(error);
  }
  
  return data;
}

/**
 * Enroll in a classroom (for students)
 */
export async function enrollInClassroom(classroomPhysicalId: string) {
  const { data, error, status } = await serverApiClient.post(`/classrooms/${classroomPhysicalId}/enroll`);
  
  if (error) {
    console.error('[Classroom Enroll Error]', { error, status });
    throw new Error(error);
  }
  
  return data;
}
