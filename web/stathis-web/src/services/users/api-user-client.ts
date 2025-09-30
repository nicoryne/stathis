'use client';

import { serverApiClient } from '@/lib/api/server-client';

/**
 * Types for user registration
 */
export type UserRoleEnum = 'GUEST_USER' | 'STUDENT' | 'TEACHER';

export interface CreateUserDTO {
  email: string;
  password: string;
  first_name: string;
  last_name: string;
  userRole: UserRoleEnum;
}

export interface UserResponseDTO {
  userId: string;
  email: string;
  first_name: string;
  last_name: string;
  role: UserRoleEnum;
  active: boolean;
  verified: boolean;
  created_at: string;
}

/**
 * Register a new user
 */
export async function registerUser(userData: CreateUserDTO): Promise<UserResponseDTO> {
  const { data, error, status } = await serverApiClient.post('/auth/register', userData);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to register user: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as UserResponseDTO;
}

/**
 * Register a new student
 */
export async function registerStudent(email: string, password: string, firstName: string, lastName: string): Promise<UserResponseDTO> {
  return registerUser({
    email,
    password,
    first_name: firstName,
    last_name: lastName,
    userRole: 'STUDENT'
  });
}

/**
 * Types for user profile management
 */
export interface UserProfileDTO {
  physicalId: string;
  email: string;
  firstName: string;
  lastName: string;
  birthdate?: string;
  profilePictureUrl?: string;
  role: 'GUEST_USER' | 'STUDENT' | 'TEACHER';
  
  // General fields
  school?: string;
  
  // Teacher-specific fields
  department?: string;
  positionTitle?: string;
  
  // Student-specific fields
  course?: string;
  yearLevel?: number;
}

// Matching the exact schema from the OpenAPI specification
export interface UpdateUserProfileDTO {
  firstName: string;
  lastName: string;
  birthdate?: string; // Optional, format should be YYYY-MM-DD
  profilePictureUrl?: string; // Optional
}

export interface UpdateTeacherProfileDTO {
  school: string;
  department?: string;
  positionTitle?: string;
}

export interface UpdateStudentProfileDTO {
  school?: string;
  course?: string;
  yearLevel?: number;
}

/**
 * Get the current user's profile (teacher)
 */
export async function getTeacherProfile(): Promise<UserProfileDTO> {
  const { data, error, status } = await serverApiClient.get('/users/profile/teacher');
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch teacher profile: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as UserProfileDTO;
}

/**
 * Validates if a string is in UUID format
 */
function isValidUUID(id: string): boolean {
  return /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id);
}

/**
 * Update user's general profile information
 */
export async function updateUserProfile(
  profileData: UpdateUserProfileDTO
): Promise<UserProfileDTO> {
  console.log('Updating user profile');
  
  // Ensure payload is exactly as expected
  const exactPayload: UpdateUserProfileDTO = {
    firstName: profileData.firstName,
    lastName: profileData.lastName
  };
  
  // Only include optional fields if they have values
  if (profileData.birthdate) {
    exactPayload.birthdate = profileData.birthdate;
  }
  
  if (profileData.profilePictureUrl) {
    exactPayload.profilePictureUrl = profileData.profilePictureUrl;
    console.log(`Profile picture URL length: ${profileData.profilePictureUrl.length} characters`);
  }
  
  // Log the exact payload being sent
  console.log('Sending payload to API:', exactPayload);
  
  // Using correct API endpoint path with custom content-type headers
  const { data, error, status } = await serverApiClient.put(
    `/users/profile`,
    exactPayload,
    {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    }
  );
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to update user profile: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as UserProfileDTO;
}

/**
 * Update teacher-specific profile information
 */
export async function updateTeacherProfile(
  profileData: UpdateTeacherProfileDTO
): Promise<UserProfileDTO> {
  console.log('Updating teacher profile');
  
  // Using correct API endpoint path
  const { data, error, status } = await serverApiClient.put(
    `/users/profile/teacher`,
    profileData
  );
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to update teacher profile: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as UserProfileDTO;
}

/**
 * Update student-specific profile information
 */
export async function updateStudentProfile(
  profileData: UpdateStudentProfileDTO
): Promise<UserProfileDTO> {
  console.log('Updating student profile');
  
  // Using correct API endpoint path
  const { data, error, status } = await serverApiClient.put(
    `/users/profile/student`,
    profileData
  );
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to update student profile: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as UserProfileDTO;
}
