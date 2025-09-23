'use client';

import { serverApiClient } from '@/lib/api/server-client';

/**
 * Score response data transfer object
 */
export interface ScoreResponseDTO {
  physicalId: string;
  studentId: string;
  taskId: string;
  taskName?: string;
  taskType: string;
  scoreValue: number;
  isCompleted: boolean;
  manualScore?: number;
  feedback?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * Types for the Student Progress API
 */
export interface StudentDTO {
  physicalId: string;
  firstName: string;
  lastName: string;
  email: string;
  profilePictureUrl?: string;
  isVerified: boolean;
  verified?: boolean; // Some API responses use verified instead of isVerified
  joinedAt?: string; // When the student joined the classroom
  createdAt: string;
  updatedAt: string;
}

export interface StudentListResponseDTO {
  students: StudentDTO[];
  totalCount: number;
}

export interface BadgeDTO {
  id: string;
  name: string;
  description: string;
  imageUrl: string;
  acquiredDate: string;
}

export interface LeaderboardEntryDTO {
  rank: number;
  studentId: string;
  studentName: string;
  score: number;
  change?: number; // position change since last period
  lastUpdated: string;
}

/**
 * Get a single student by ID
 */
export async function getStudentById(studentId: string): Promise<StudentDTO | null> {
  try {
    // Directly access the API endpoint that we know is working (based on debug output)
    const { data, error, status } = await serverApiClient.get(`/api/students`);
    
    // Log for debugging
    console.log('[DEBUG] Student API response:', data);
    
    if (error || status >= 400) {
      console.error(`Failed to fetch students list:`, error);
      return null;
    }
    
    // The endpoint returns an array of students, find the one with matching ID
    if (Array.isArray(data)) {
      const student = data.find((s: StudentDTO) => s.physicalId === studentId);
      if (student) {
        return student;
      }
    }
    
    // If we didn't find the student in the array
    console.error(`Student with ID ${studentId} not found in students list`);
    return null;
  } catch (error) {
    console.error(`Failed to fetch student with ID ${studentId}:`, error);
    return null;
  }
}

/**
 * Get students for a specific classroom
 */
export async function getClassroomStudents(classroomPhysicalId: string): Promise<StudentListResponseDTO> {
  try {
    // Try the original endpoint first
    console.log(`Attempting to fetch classroom students with endpoint: /classrooms/${classroomPhysicalId}/students`);
    const { data, error, status } = await serverApiClient.get(`/classrooms/${classroomPhysicalId}/students`);
    
    if (error || status >= 400) {
      console.warn(`Original endpoint failed with status ${status}, trying alternative endpoint`);
      
      // Try alternative endpoint format
      console.log(`Attempting alternative endpoint: /classroom-students/${classroomPhysicalId}`);
      const altResponse = await serverApiClient.get(`/classroom-students/${classroomPhysicalId}`);
      
      if (altResponse.error || altResponse.status >= 400) {
        console.error(`Alternative endpoint also failed with status ${altResponse.status}`);
        throw new Error(`Failed to fetch classroom students: ${status}`);
      }
      
      console.log('Alternative endpoint succeeded, formatting response');
      // Format the response to match expected StudentListResponseDTO structure
      return {
        students: Array.isArray(altResponse.data) ? altResponse.data : [],
        totalCount: Array.isArray(altResponse.data) ? altResponse.data.length : 0
      };
    }
    
    return data as StudentListResponseDTO;
  } catch (error) {
    console.error('Error in getClassroomStudents:', error);
    // Return mock data for development to prevent UI errors
    return {
      students: [
        {
          physicalId: 'STUDENT-1',
          firstName: 'Alex',
          lastName: 'Johnson',
          email: 'alex.j@example.com',
          isVerified: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        },
        {
          physicalId: 'STUDENT-2',
          firstName: 'Emma',
          lastName: 'Wilson',
          email: 'emma.w@example.com',
          isVerified: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        },
        {
          physicalId: 'STUDENT-3',
          firstName: 'Michael',
          lastName: 'Brown',
          email: 'michael.b@example.com',
          isVerified: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        }
      ],
      totalCount: 3
    };
  }
}

/**
 * Get scores for a specific task
 */
export async function getTaskScores(taskId: string): Promise<ScoreResponseDTO[]> {
  const { data, error, status } = await serverApiClient.get(`/scores/task/${taskId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch task scores: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO[];
}

/**
 * Get all scores for a specific student
 */
export async function getStudentScores(studentId: string): Promise<ScoreResponseDTO[]> {
  // Updated to use the correct API endpoint with /v1/ in the path
  const { data, error, status } = await serverApiClient.get(`/v1/scores/student/${studentId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch student scores: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO[];
}

/**
 * Get score for a specific student and task
 */
export async function getStudentTaskScore(studentId: string, taskId: string): Promise<ScoreResponseDTO[]> {
  // Updated to use the correct API endpoint with /v1/ in the path
  const { data, error, status } = await serverApiClient.get(`/v1/scores/student/${studentId}/task/${taskId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch student task score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO[];
}

/**
 * Get badges for a specific student
 */
export async function getStudentBadges(studentId: string): Promise<BadgeDTO[]> {
  const url = `/achievements/badges?studentId=${encodeURIComponent(studentId)}`;
  const { data, error, status } = await serverApiClient.get(url);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch student badges: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as BadgeDTO[];
}

/**
 * Get leaderboard data for a specific student
 */
export async function getStudentLeaderboardPosition(studentId: string): Promise<LeaderboardEntryDTO[]> {
  const url = `/achievements/leaderboard?studentId=${encodeURIComponent(studentId)}`;
  const { data, error, status } = await serverApiClient.get(url);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch student leaderboard position: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as LeaderboardEntryDTO[];
}
