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

// Student data returned from API
export interface StudentDTO {
  physicalId: string;
  firstName: string;
  lastName: string;
  email: string;
  profilePictureUrl?: string;
  verified: boolean; // Note: Backend returns 'verified' not 'isVerified'
  joinedAt?: string;
}

// Response structure for classroom students endpoint
export interface StudentListResponseDTO {
  students: StudentDTO[];
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
  
  // Get the token directly to ensure we're using the most up-to-date version
  let authToken = '';
  if (typeof window !== 'undefined') {
    authToken = localStorage.getItem('auth_token') || '';
  }

  // Use the /teacher endpoint which is designed to use the security context
  const { data, error, status } = await serverApiClient.get('/classrooms/teacher', {
    // Add explicit headers including Authorization to ensure it's sent correctly
    headers: {
      'Accept': 'application/json',
      'Authorization': `Bearer ${authToken.trim()}` // Trim to remove any potential whitespace
    }
  });
  
  // Additional debug logging
  console.log('[Teacher Classrooms] Auth header sent:', `Bearer ${authToken.substring(0, 10)}...`);
  
  // Log the full error details
  if (error) {
    console.error('[Teacher Classrooms Get Error]', { error, status });
    
    // Enhanced error reporting for auth issues
    if (status === 401 || status === 403) {
      console.error('[Auth Error] Possible authentication or authorization issue:');
      console.error('- Token valid:', !!authToken);
      console.error('- Token length:', authToken?.length || 0);
      console.error('- User role:', getCurrentUserRole());
      
      // Attempt to decode the token
      try {
        const tokenParts = authToken.split('.');
        if (tokenParts.length === 3) {
          const payload = JSON.parse(atob(tokenParts[1].replace(/-/g, '+').replace(/_/g, '/')));
          console.error('- Token payload:', payload);
          console.error('- Token expiration:', new Date(payload.exp * 1000).toISOString());
          console.error('- Token expired:', payload.exp * 1000 < Date.now());
        }
      } catch (e) {
        console.error('- Could not decode token:', e);
      }
    }
    
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
export async function getClassroomStudents(classroomPhysicalId: string): Promise<StudentListResponseDTO> {
  console.log(`[DEBUG] Fetching students for classroom: ${classroomPhysicalId}`);
  
  try {
    // Make the API call
    const response = await serverApiClient.get(`/classrooms/${classroomPhysicalId}/students`);
    const { data, error, status } = response;
    
    if (error) {
      console.error('[Classroom Students Get Error]', { error, status });
      throw new Error(error);
    }
    
    // Debug: Log the response to see what we're getting
    console.log('[DEBUG] Student API response:', JSON.stringify(data, null, 2));
    
    // Handle different response formats
    if (!data) {
      console.warn('[DEBUG] API returned null/undefined data');
      return { students: [] };
    }
    
    // Direct API call to endpoint:
    // The OpenAPI spec says the response is an array of StudentListResponseDTO objects
    // But based on your schema, we need to explicitly handle different response formats
    
    // Case 1: API returns array of students directly
    if (Array.isArray(data)) {
      console.log('[DEBUG] API returned students array directly');
      return { 
        students: data.map(student => ({
          ...student,
          // Ensure the 'verified' property exists (convert from isVerified if needed)
          verified: 'verified' in student ? student.verified : 
                   'isVerified' in student ? (student as any).isVerified : false
        })) 
      };
    }
    
    // Case 2: API returns { students: [...] } object (matches our DTO)
    if (typeof data === 'object' && data !== null && 'students' in data && Array.isArray(data.students)) {
      console.log('[DEBUG] API returned properly structured StudentListResponseDTO');
      // Ensure all students have the verified property
      const responseDTO = data as StudentListResponseDTO;
      responseDTO.students = responseDTO.students.map(student => ({
        ...student,
        // Ensure the 'verified' property exists (convert from isVerified if needed)
        verified: 'verified' in student ? student.verified : 
                 'isVerified' in student ? (student as any).isVerified : false
      }));
      return responseDTO;
    }
    
    // Case 3: Unknown format, log and return empty array
    console.warn('[DEBUG] API returned unexpected format:', typeof data);
    return { students: [] };
  } catch (error) {
    console.error('[Classroom Students Fetch Error]', error);
    // Return empty array instead of throwing to prevent UI crashes
    return { students: [] };
  }
}

/**
 * Verify a student in a classroom
 * 
 * Now the backend is correctly expecting a student physical ID like "25-1179-089"
 */
export async function verifyClassroomStudent(classroomPhysicalId: string, studentPhysicalId: string) {
  console.log(`Verifying student ${studentPhysicalId} in classroom ${classroomPhysicalId}`);
  
  // Use the serverApiClient for consistency with other API calls
  const { data, error, status } = await serverApiClient.post(
    `/classrooms/${classroomPhysicalId}/students/${studentPhysicalId}/verify`
  );
  
  if (error) {
    console.error('[Student Verify Error]', { 
      error, 
      status,
      classroomPhysicalId,
      studentPhysicalId
    });
    throw new Error(error);
  }
  
  return data || { success: true };
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
