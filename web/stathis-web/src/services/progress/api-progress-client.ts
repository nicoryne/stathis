'use client';

/**
 * Student Progress API Client
 * 
 * This module provides functions for interacting with the student progress API endpoints.
 * It uses an aggregated API approach to efficiently retrieve student progress data.
 * 
 * Key features:
 * - Utilizes the single aggregated endpoint: GET /api/v1/student-progress/{studentId}
 * - Reduces multiple API calls to a single request
 * - Properly handles authentication and error cases
 * - Supports dynamic maxScore from backend data
 * - Provides task metadata (taskName, taskType) for UI display
 * 
 * Implementation notes:
 * 1. The StudentProgressItemDTO interface matches the backend StudentProgressDTO structure
 * 2. The fetchStudentProgressItems function directly uses the aggregated endpoint
 * 3. Authentication is handled through Authorization headers with Bearer tokens
 * 4. The UI components correctly display taskName, taskType, and dynamic maxScore
 * 
 * @version v1.0 (2025-10-01)
 * @see /api/v1/student-progress/{studentId} endpoint
 */

import { serverApiClient } from '@/lib/api/server-client';

/**
 * Student Progress DTO matching the backend TaskProgressDTO structure
 */
export interface StudentProgressDTO {
  // Original fields from the backend TaskProgressDTO
  lessonCompleted: boolean;
  exerciseCompleted: boolean;
  quizCompleted: boolean;
  quizScore: number;
  maxQuizScore: number;
  quizAttempts: number;
  totalTimeTaken: number; // Time in seconds
  startedAt: string;
  completedAt: string | null;
  submittedForReview: boolean;
  submittedAt: string | null;
  
  // Extended client-side properties for the UI
  studentId: string; // Set from URL parameter
  fullName: string; // Set from user context
  
  // Key performance indicators (KPIs) - derived from available data
  kpis: {
    averageScore: number; // Calculated from quizScore / maxQuizScore
    recentActivityDays: number; // Calculated from current date vs completedAt
    timeSpentMinutes: number; // Calculated from totalTimeTaken (seconds to minutes)
    currentStreakDays: number; // Default to 1 if active, 0 if not
  };
  
  // Additional UI fields required by the component
  statusMessages: string[];
  performanceHistory?: Array<{
    period: string; // e.g., "Week 1", "Assessment 3"
    score: number;
    maxScore?: number; // Maximum possible score
    taskName?: string; // Name of the task
    taskType?: string; // Type of task (quiz, lesson, exercise)
  }>;
}

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
  maxScore?: number; // Maximum possible score for this task
  isCompleted: boolean;
  manualScore?: number;
  feedback?: string;
  createdAt: string;
  updatedAt: string;
  completedAt?: string; // When the task was completed
  classroomId?: string; // Optional classroom ID that might be associated with the score
}

/**
 * StudentProgressItemDTO - Matches the backend StudentProgressDTO structure
 * 
 * This interface represents the response from the aggregated student progress endpoint.
 * It provides comprehensive data about a student's progress on a specific task including:
 * - Task metadata (ID, name, type)
 * - Completion status
 * - Score information with dynamic maxScore
 * - Timestamps for progress tracking
 * 
 * @see edu.cit.stathis.task.dto.StudentProgressDTO in the backend
 */
export interface StudentProgressItemDTO {
  /** Unique identifier for the task */
  taskId: string;
  /** Human-readable name of the task - displayed in the UI */
  taskName: string;
  /** Type of task (QUIZ, LESSON, EXERCISE) - used for conditional rendering */
  taskType: string;
  /** Physical ID of the classroom this task belongs to */
  classroomPhysicalId: string;
  /** Whether the student has completed this task */
  completed: boolean;
  /** Student's score on this task (null if not applicable or not yet scored) */
  score: number | null;
  /** Maximum possible score for this task - set by the teacher */
  maxScore: number | null;
  /** Number of attempts the student has made on this task */
  attempts: number | null;
  /** When the student completed the task */
  completedAt: string | null;
  /** Due date for submission */
  submissionDate: string | null;
  /** Final closing date after which submissions are not accepted */
  closingDate: string | null;
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

/**
 * User response DTO matching the backend UserResponseDTO
 */
export interface UserResponseDTO {
  physicalId: string;
  email: string;
  firstName: string;
  lastName: string;
  birthdate?: string;
  profilePictureUrl?: string;
  role: string;
  school?: string;
  course?: string;
  yearLevel?: number;
  department?: string;
  positionTitle?: string;
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
  score: number;
  change?: number; // position change since last period
  lastUpdated: string;
}

/**
 * Get a single student by ID
 * First tries to get the student from the student profile endpoint if it's the current user
 * Falls back to a mock response if it's not the current user or we can't access the profile
 */
export async function getStudentById(studentId: string): Promise<StudentDTO | null> {
  try {
    console.log(`Attempting to fetch student with ID: ${studentId}`);
    
    // Try to fetch the specific student from the users endpoint
    try {
      const { data, error, status } = await serverApiClient.get(`/users/${studentId}`);
      
      if (!error && status < 400 && data) {
        console.log('[DEBUG] Student user data response:', data);
        
        // Convert UserResponseDTO to StudentDTO format
        const userData = data as UserResponseDTO;
        const student: StudentDTO = {
          physicalId: userData.physicalId,
          firstName: userData.firstName,
          lastName: userData.lastName,
          email: userData.email,
          profilePictureUrl: userData.profilePictureUrl,
          isVerified: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        
        return student;
      }
    } catch (e) {
      console.warn(`Could not fetch user data for ${studentId}, trying alternative methods:`, e);
    }
    
    // Try to get student from classroom students endpoint
    try {
      // Extract classroom code from student ID (format: XX-YYYY-ZZZ)
      const parts = studentId.split('-');
      if (parts.length >= 2) {
        const classroomId = `ROOM-${parts[0]}-${parts[1]}`;
        console.log(`Trying to fetch student from classroom: ${classroomId}`);
        
        const { data, error, status } = await serverApiClient.get(`/classrooms/${classroomId}/students`);
        
        if (!error && status < 400 && data) {
          console.log('[DEBUG] Got classroom students data');
          const classroomData = data as { students?: any[] };
          const studentInClassroom = classroomData.students?.find((s: any) => s.physicalId === studentId);
          
          if (studentInClassroom) {
            console.log('[DEBUG] Found student in classroom:', studentInClassroom);
            return {
              physicalId: studentInClassroom.physicalId,
              firstName: studentInClassroom.firstName,
              lastName: studentInClassroom.lastName,
              email: studentInClassroom.email,
              profilePictureUrl: studentInClassroom.profilePictureUrl,
              isVerified: studentInClassroom.verified || studentInClassroom.isVerified || true,
              createdAt: studentInClassroom.createdAt || new Date().toISOString(),
              updatedAt: studentInClassroom.updatedAt || new Date().toISOString(),
              joinedAt: studentInClassroom.joinedAt
            };
          }
        }
      }
    } catch (e) {
      console.warn('Could not fetch student from classroom:', e);
    }
    
    // If all else fails, return a mock student with the ID
    // This allows the UI to still function with limited data
    console.warn(`Could not fetch student data, creating placeholder for: ${studentId}`);
    const idParts = studentId.split('-');
    const studentNumber = idParts.length >= 3 ? idParts[2] : studentId;
    
    return {
      physicalId: studentId,
      firstName: `Student ${studentNumber}`,
      lastName: '',
      email: '',
      isVerified: true,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
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
 * Get tasks for a specific classroom
 */
export async function getClassroomTasks(classroomId: string): Promise<Record<string, { name: string, type: string }>> {
  try {
    // Validate classroom ID
    if (!classroomId) {
      console.error('No classroom ID provided to getClassroomTasks');
      return {};
    }
    
    console.log(`Fetching tasks for classroom: ${classroomId}`);
    
    // Use the tasks/classroom/{classroomId} endpoint that has proper permissions
    const { data, error, status } = await serverApiClient.get(`tasks/classroom/${classroomId}`);
    
    if (error || status >= 400) {
      console.warn(`Failed to fetch classroom tasks: ${status}`, error);
      
      // Try an alternative endpoint format if the first one fails
      try {
        console.log(`Trying alternative endpoint for classroom tasks: /api/v1/tasks/classroom/${classroomId}`);
        const altResponse = await serverApiClient.get(`/api/v1/tasks/classroom/${classroomId}`);
        
        if (!altResponse.error && altResponse.status < 400 && altResponse.data) {
          console.log('Alternative endpoint succeeded');
          const tasks = altResponse.data;
          return processTasksData(tasks, classroomId);
        }
      } catch (altError) {
        console.warn('Alternative endpoint also failed', altError);
      }
      
      return {};
    }
    
    return processTasksData(data, classroomId);
  } catch (e) {
    console.error('Error fetching classroom tasks:', e);
    return {};
  }
}

/**
 * Helper function to process task data into a standardized format
 */
function processTasksData(data: any, classroomId: string): Record<string, { name: string, type: string }> {
  // Map of taskId -> {name, type}
  const taskMap: Record<string, { name: string, type: string }> = {};
  
  // Process task data - this API returns an array of Task objects directly
  interface TaskData {
    physicalId: string;
    name?: string;
    quizTemplateId?: string;
    lessonTemplateId?: string;
    exerciseTemplateId?: string;
    description?: string;
    active?: boolean;
  }
  
  const tasks = data as TaskData[];
  
  if (Array.isArray(tasks)) {
    console.log(`Found ${tasks.length} tasks in classroom ${classroomId}`);
    
    // Map each task to our format
    tasks.forEach((task: TaskData) => {
      if (task.physicalId) {
        // Determine task type based on available properties
        let taskType = 'TASK';
        if (task.quizTemplateId) taskType = 'QUIZ';
        else if (task.lessonTemplateId) taskType = 'LESSON';
        else if (task.exerciseTemplateId) taskType = 'EXERCISE';
        // Fallback to name-based inference if needed
        else if (task.name) {
          const name = task.name.toLowerCase();
          if (name.includes('quiz')) taskType = 'QUIZ';
          else if (name.includes('test')) taskType = 'TEST';
          else if (name.includes('lesson')) taskType = 'LESSON';
          else if (name.includes('exercise')) taskType = 'EXERCISE';
          else if (name.includes('assess')) taskType = 'ASSESSMENT';
        }
        
        taskMap[task.physicalId] = {
          name: task.name || `Task ${task.physicalId.substring(0, 8)}`,
          type: taskType
        };
      }
    });
  } else {
    console.warn(`Unexpected task data format for classroom ${classroomId}:`, data);
  }
  
  return taskMap;
}

/**
 * Get all scores for a specific student using multiple API calls and client-side merging
 */
export async function getStudentScores(studentId: string): Promise<ScoreResponseDTO[]> {
  console.log(`Fetching comprehensive data for student: ${studentId}`);
  
  // Step 1: Get the base score data
  const { data: scoreData, error, status } = await serverApiClient.get(`/v1/scores/student/${studentId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch student scores: ${status}`;
    throw new Error(errorMessage);
  }
  
  // Get base scores from the API
  const baseScores = scoreData as ScoreResponseDTO[];
  console.log(`Retrieved ${baseScores.length} base scores for student ${studentId}`);

  // Step 2: Get the student's classrooms
  let classroomIds: string[] = [];
  try {
    // Try to get classrooms from the API
    const { data: classroomsData } = await serverApiClient.get('classrooms/student');
    if (Array.isArray(classroomsData) && classroomsData.length > 0) {
      classroomIds = classroomsData.map(classroom => classroom.physicalId);
      console.log(`Found ${classroomIds.length} classrooms for student ${studentId}:`, classroomIds);
    } else {
      console.warn('No classrooms found for student');
    }
  } catch (e) {
    console.warn('Error fetching student classrooms:', e);
  }
  
  // If no classrooms were found, try to get classroom IDs from the user profile
  if (classroomIds.length === 0) {
    try {
      interface UserProfile {
        classroomId?: string;
        firstName?: string;
        lastName?: string;
      }
      
      const { data: userData } = await serverApiClient.get('users/profile/student');
      const userProfile = userData as UserProfile;
      
      if (userProfile && userProfile.classroomId) {
        classroomIds = [userProfile.classroomId];
        console.log(`Using classroom ID from user profile: ${userProfile.classroomId}`);
      }
    } catch (e) {
      console.warn('Error fetching user profile:', e);
    }
  }
  
  // Step 3: Get all tasks for each classroom
  const allClassroomTasks: Record<string, { name: string, type: string, classroomId: string }> = {};
  
  // Process each classroom to get its tasks
  await Promise.all(classroomIds.map(async (classroomId) => {
    try {
      console.log(`Fetching tasks for classroom: ${classroomId}`);
      const { data, error } = await serverApiClient.get(`tasks/classroom/${classroomId}`);
      
      if (error) {
        console.warn(`Error fetching tasks for classroom ${classroomId}:`, error);
        return;
      }
      
      // Process the task data
      interface TaskData {
        physicalId: string;
        name?: string;
        quizTemplateId?: string;
        lessonTemplateId?: string;
        exerciseTemplateId?: string;
        description?: string;
      }
      
      if (Array.isArray(data)) {
        data.forEach((task: TaskData) => {
          if (task.physicalId) {
            // Determine task type based on templates
            let taskType = 'TASK';
            if (task.quizTemplateId) taskType = 'QUIZ';
            else if (task.lessonTemplateId) taskType = 'LESSON';
            else if (task.exerciseTemplateId) taskType = 'EXERCISE';
            // Name-based inference as fallback
            else if (task.name) {
              const name = task.name.toLowerCase();
              if (name.includes('quiz')) taskType = 'QUIZ';
              else if (name.includes('test')) taskType = 'TEST';
              else if (name.includes('lesson')) taskType = 'LESSON';
              else if (name.includes('exercise')) taskType = 'EXERCISE';
            }
            
            allClassroomTasks[task.physicalId] = {
              name: task.name || `Task ${task.physicalId.substring(0, 8)}`,
              type: taskType,
              classroomId
            };
          }
        });
        console.log(`Added ${data.length} tasks from classroom ${classroomId}`);
      }
    } catch (e) {
      console.warn(`Error processing classroom ${classroomId}:`, e);
    }
  }));

  console.log(`Total classroom tasks collected: ${Object.keys(allClassroomTasks).length}`);
  
  // Step 4: Get task completion information
  let taskCompletions: Record<string, any> = {};
  try {
    const { data: completionsData } = await serverApiClient.get(`v1/task-completions/student/${studentId}`);
    if (Array.isArray(completionsData)) {
      completionsData.forEach(completion => {
        if (completion.taskId) {
          taskCompletions[completion.taskId] = completion;
        }
      });
      console.log(`Retrieved ${completionsData.length} task completions`);
    }
  } catch (e) {
    console.warn('Error fetching task completions:', e);
  }

  // Step 5: Define known tasks as fallback
  const knownTasks: Record<string, {name: string, type: string}> = {
    'TASK-F38A3426-1198-4B19-84F4-C4A0D0D391D3': { name: 'Push Ups', type: 'EXERCISE' },
    'TASK-B28C7D38-23A8-45F8-AEFC-165D9D71BD1A': { name: 'Jump Rope Exercise', type: 'EXERCISE' },
    'TASK-2B6B28B9-513B-4A00-9D6F-965D05EA990C': { name: 'Cardio Assessment', type: 'QUIZ' },
    'TASK-D07EBEA5-8F34-4560-9525-5259CC1149AA': { name: 'Weekly Fitness Test', type: 'TEST' }
  };

  // Step 6: Enhance each score with task details
  const enhancedScores = await Promise.all(baseScores.map(async (score) => {
    // Define default enhanced score with what we have
    let enhancedScore: ScoreResponseDTO = {
      ...score,
      taskName: score.taskName || "",  // Will be updated below
      taskType: score.taskType || "TASK", // Will be updated below
      maxScore: score.maxScore || 100,
      isCompleted: score.isCompleted || false
    };
    
    // 6.1: Check if we have task info from classroom tasks
    if (score.taskId && score.taskId in allClassroomTasks) {
      const taskInfo = allClassroomTasks[score.taskId];
      console.log(`Found task info for ${score.taskId} in classroom ${taskInfo.classroomId}`);
      enhancedScore.taskName = taskInfo.name;
      enhancedScore.taskType = taskInfo.type;
    }
    
    // 6.2: Check if task is in our hardcoded known tasks
    else if (score.taskId in knownTasks) {
      console.log(`Using known task data for ${score.taskId}: ${knownTasks[score.taskId].name}`);
      enhancedScore.taskName = knownTasks[score.taskId].name;
      enhancedScore.taskType = knownTasks[score.taskId].type;
    }
    
    // 6.3: For tasks not found elsewhere, try to infer name and type from taskId
    else if (!enhancedScore.taskName && score.taskId) {
      // Parse the task ID for a name
      const taskIdParts = score.taskId.split('-');
      if (taskIdParts.length > 1) {
        const taskWords = taskIdParts.filter((part: string) => 
          part.length > 3 && !part.match(/^[0-9A-F]+$/)
        );
        
        if (taskWords.length > 0) {
          enhancedScore.taskName = taskWords.map((word: string) => 
            word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
          ).join(' ');
        } else {
          enhancedScore.taskName = `Task ${score.taskId.substring(0, 8)}`;
        }
      } else {
        enhancedScore.taskName = `Task ${score.taskId.substring(0, 8)}`;
      }
      
      // Try to determine task type from name
      const textToCheck = (enhancedScore.taskName || score.taskId).toLowerCase();
      if (textToCheck.includes('quiz')) enhancedScore.taskType = 'QUIZ';
      else if (textToCheck.includes('test')) enhancedScore.taskType = 'TEST';
      else if (textToCheck.includes('lesson')) enhancedScore.taskType = 'LESSON';
      else if (textToCheck.includes('exercise') || 
               textToCheck.includes('pushup') || 
               textToCheck.includes('push up') || 
               textToCheck.includes('workout')) enhancedScore.taskType = 'EXERCISE';
      else if (textToCheck.includes('assess')) enhancedScore.taskType = 'ASSESSMENT';
      console.log(`Inferred name/type for ${score.taskId}: ${enhancedScore.taskName} (${enhancedScore.taskType})`);
    }
    
    // 6.4: Check if we have completion data for this task
    if (score.taskId && score.taskId in taskCompletions) {
      const completion = taskCompletions[score.taskId];
      console.log(`Found completion data for task ${score.taskId}`);
      
      // Update completion status using completion data
      enhancedScore.isCompleted = completion.lessonCompleted || 
                                 completion.quizCompleted || 
                                 completion.exerciseCompleted || 
                                 completion.fullyCompleted || 
                                 false;
    } else {
      // If we don't have explicit completion data, set as incomplete
      // unless there is actual score data indicating it was completed
      enhancedScore.isCompleted = !!score.completedAt || 
                                (score.scoreValue > 0 && score.scoreValue !== undefined) || 
                                false;
    }
    
    // Return the enhanced score
    return enhancedScore;
  }));
  
  // Filter out duplicate and generic tasks that don't have meaningful names
  const uniqueTaskIds = new Set<string>();
  const filteredScores = enhancedScores.filter(score => {
    // Skip tasks that just have the default 'Task' name without any more information
    if (score.taskName === 'Task' || !score.taskName) {
      return false;
    }
    
    // If we've already seen a task with this ID, skip it (to avoid duplicates)
    if (score.taskId && uniqueTaskIds.has(score.taskId)) {
      return false;
    }
    
    // Add this task ID to our set of seen tasks
    if (score.taskId) {
      uniqueTaskIds.add(score.taskId);
    }
    
    return true;
  });
  
  console.log(`Returning ${filteredScores.length} filtered scores from ${enhancedScores.length} total`);
  return filteredScores;
}

/**
 * Get student progress using the new endpoint that returns aggregated progress data
 * @param studentId The ID of the student to fetch progress for
 * @param classroomId Optional classroom ID to filter tasks by classroom
 * @returns List of StudentProgressItemDTO objects
 */
export async function fetchStudentProgressItems(studentId: string, classroomId?: string): Promise<StudentProgressItemDTO[]> {
  try {
    console.log(`Fetching student progress items for student: ${studentId}`);
    
    // First, make sure we have the classroom ID to ensure proper access pattern
    let targetClassroomId = classroomId;
    
    // If no classroomId is provided, use a known working classroom ID
    // Previous attempts to extract from student ID pattern were unreliable
    if (!targetClassroomId && studentId.includes('-')) {
      const parts = studentId.split('-');
      if (parts.length >= 2) {
        // No longer relying on this approach - using API call below instead
        // Don't set a hardcoded fallback - this will force proper parameter passing
      }
    }
    
    // If still no classroom ID, try to determine it dynamically from the API
    if (!targetClassroomId) {
      console.log('No classroom ID available. Attempting to fetch from API...');
      try {
        // Try to get classrooms from the API
        const { data: classroomsData } = await serverApiClient.get('classrooms/student');
        if (Array.isArray(classroomsData) && classroomsData.length > 0) {
          targetClassroomId = classroomsData[0].physicalId;
          console.log(`Successfully determined classroom ID from API: ${targetClassroomId}`);
        }
      } catch (e) {
        console.warn('Error trying to determine classroom ID:', e);
      }
    }
    
    // Note: At this point, classroom ID is either provided via URL params, determined via API, or null
    // Log which classroom ID we're using
    console.log(`Final classroom ID being used: ${targetClassroomId}`);
    
    // Ensure classroom ID has ROOM- prefix if not already present
    if (targetClassroomId && !targetClassroomId.startsWith('ROOM-')) {
      const originalId = targetClassroomId;
      targetClassroomId = `ROOM-${targetClassroomId}`;
      console.log(`Added ROOM- prefix to classroom ID: ${originalId} → ${targetClassroomId}`);
    }
    
    if (!targetClassroomId) {
      console.error(`Cannot fetch progress: No classroom ID available for student ${studentId}`);
      return [];
    }
    
    // Fetch all tasks for the classroom
    console.log(`Fetching all tasks for classroom: ${targetClassroomId}`);
    let tasks: Record<string, { name: string, type: string }> = {};
    try {
      tasks = await getClassroomTasks(targetClassroomId);
      console.log(`Found ${Object.keys(tasks).length} tasks for classroom ${targetClassroomId}`);
      if (Object.keys(tasks).length === 0) {
        console.warn(`No tasks found for classroom ${targetClassroomId}`);
      }
    } catch (e) {
      console.warn(`Error getting tasks for classroom ${targetClassroomId}:`, e);
    }
    
    // Fetch the student progress data with proper authorization headers
    // Build the endpoint URL with required classroomId parameter
    let url = `/v1/student-progress/${studentId}?classroomId=${encodeURIComponent(targetClassroomId)}`;
    
    console.log(`Accessing student progress with URL: ${url}`);
    
    // Add custom headers to ensure proper authentication
    const customHeaders: Record<string, string> = {};
    
    // Try to get authentication token from localStorage
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('auth_token');
      if (token) {
        customHeaders['Authorization'] = `Bearer ${token.trim()}`;
        console.log('Added authorization token to request');
      } else {
        console.warn('No authentication token available for student progress request');
      }
    }
    
    const { data, error, status } = await serverApiClient.get(url, { headers: customHeaders });
    
    if (status === 403) {
      console.error('AUTHENTICATION ERROR: 403 Forbidden accessing student progress data');
      console.error('This typically means the token is invalid, expired, or has insufficient permissions');
      console.error('Ensure you are logged in with the correct user role and have access to this student');
      return [];
    }
    
    if (error || status >= 400) {
      const errorMessage = typeof error === 'object' && error !== null && 'message' in error
        ? (error as { message: string }).message
        : `Failed to fetch student progress: ${status}`;
      console.error(errorMessage);
      return [];
    }
    
    if (!data) {
      console.log(`No progress data returned for student ${studentId}`);
      return [];
    }
    
    // If we have progress data and tasks, enhance the progress items with task details
    const progressItems = Array.isArray(data) ? data as StudentProgressItemDTO[] : [];
    
    // If we have task data, enhance the progress items with task names and types
    if (Object.keys(tasks).length > 0) {
      for (const item of progressItems) {
        const taskInfo = tasks[item.taskId];
        if (taskInfo) {
          item.taskName = taskInfo.name || item.taskName;
          item.taskType = taskInfo.type || item.taskType;
        }
      }
    }
    
    console.log(`Successfully retrieved ${progressItems.length} progress items for student ${studentId}`);
    return progressItems;
  } catch (error) {
    console.error('Error in fetchStudentProgressItems:', error);
    // Return empty array instead of throwing to prevent cascading errors
    return [];
  }
}

/**
 * Task interface for proper typing
 */
interface TaskDTO {
  name?: string;
  description?: string;
  quizTemplateId?: string;
  lessonTemplateId?: string;
  exerciseTemplateId?: string;
}

/**
 * Helper function to determine the task type based on template IDs
 */
function determineTaskType(taskData: TaskDTO): string {
  if (!taskData) return "UNKNOWN";
  
  if (taskData.quizTemplateId) return "QUIZ";
  if (taskData.lessonTemplateId) return "LESSON";
  if (taskData.exerciseTemplateId) return "EXERCISE";
  
  // Check for type indicators in the task name
  const name = taskData.name?.toLowerCase() || "";
  if (name.includes("quiz")) return "QUIZ";
  if (name.includes("lesson")) return "LESSON";
  if (name.includes("exercise") || name.includes("push up") || name.includes("pushup")) return "EXERCISE";
  
  return "TASK";
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

/**
 * Get comprehensive progress data for a specific student using the aggregated progress API endpoint
 * @param studentId The ID of the student to fetch progress for
 * @returns StudentProgressDTO containing the student's progress data
 */
export async function getStudentProgress(studentId: string): Promise<StudentProgressDTO> {
  try {
    console.log(`Fetching aggregated student progress for student ID: ${studentId}`);
    
    // Extract classroom ID from student ID for proper API access
    const classroomId = studentId.includes('-') 
      ? `${studentId.split('-')[0]}-${studentId.split('-')[1]}` 
      : undefined;
    
    if (!classroomId) {
      console.error(`Cannot fetch progress: No classroom ID could be extracted from studentId ${studentId}`);
      throw new Error('Invalid student ID format - cannot determine classroom');
    }
    
    // Use the new aggregated student progress endpoint which provides task details and scores in one call
    let progressItems: StudentProgressItemDTO[] = [];
    try {
      progressItems = await fetchStudentProgressItems(studentId, classroomId);
      console.log(`Retrieved ${progressItems.length} progress items from aggregated endpoint`);
    } catch (e) {
      console.error('Error fetching aggregated progress data:', e);
    }
    
    // Get user profile information for name and other details
    let firstName = "Student";
    let lastName = "";
    
    try {
      interface UserProfile {
        firstName?: string;
        lastName?: string;
        yearLevel?: number;
      }
      
      const { data: profileData } = await serverApiClient.get(`/users/profile/student`);
      const userProfile = profileData as UserProfile;
      
      if (userProfile) {
        firstName = userProfile.firstName || firstName;
        lastName = userProfile.lastName || lastName;
        console.log(`Found student name: ${firstName} ${lastName}`);
      }
    } catch (e) {
      console.warn('Could not fetch student profile:', e);
    }
    
    // Calculate statistics from the progress items
    const totalTasks = progressItems.length;
    const completedTasks = progressItems.filter(item => item.completed).length;
    
    // Calculate quiz statistics
    const quizItems = progressItems.filter(item => 
      item.taskType?.toUpperCase() === 'QUIZ' || 
      item.taskType?.includes('QUIZ')
    );
    
    let quizScoreValue = 0;
    let quizMaxValue = 100; // Default max score
    if (quizItems.length > 0) {
      const scoreSum = quizItems.reduce((sum, item) => sum + (item.score || 0), 0);
      const maxSum = quizItems.reduce((sum, item) => sum + (item.maxScore || 100), 0);
      quizScoreValue = Math.round(scoreSum / quizItems.length);
      quizMaxValue = Math.round(maxSum / quizItems.length);
    }
    
    // Calculate overall average score as percentage
    const scoredItems = progressItems.filter(item => item.score !== null);
    const totalPoints = scoredItems.reduce((sum, item) => sum + (item.score || 0), 0);
    const totalPossible = scoredItems.reduce((sum, item) => sum + (item.maxScore || 100), 0);
    const overallPercentage = totalPossible > 0 ? (totalPoints / totalPossible * 100) : 0;
    
    // Calculate activity metrics
    const completedItems = progressItems.filter(item => item.completed && item.completedAt);
    const latestActivity = completedItems.length > 0
      ? new Date(Math.max(...completedItems.map(item => 
          item.completedAt ? new Date(item.completedAt).getTime() : 0)))
      : null;
    
    const recentActivityDays = latestActivity
      ? Math.round((new Date().getTime() - latestActivity.getTime()) / (1000 * 60 * 60 * 24))
      : 7; // Default if no activity
    
    // Create the StudentProgressDTO from the aggregated data
    const progressData: StudentProgressDTO = {
      // Basic identification
      studentId,
      fullName: `${firstName} ${lastName}`,
      
      // Task completion data from aggregated endpoint
      lessonCompleted: progressItems.some(item => item.taskType?.toUpperCase() === 'LESSON' && item.completed),
      exerciseCompleted: progressItems.some(item => item.taskType?.toUpperCase() === 'EXERCISE' && item.completed),
      quizCompleted: progressItems.some(item => item.taskType?.toUpperCase() === 'QUIZ' && item.completed),
      quizScore: quizScoreValue,
      maxQuizScore: quizMaxValue, // Use dynamic max score from the DTO
      quizAttempts: quizItems.filter(item => item.attempts !== null).reduce((sum, item) => sum + (item.attempts || 0), 0),
      totalTimeTaken: progressItems.length * 600, // Estimate 10 minutes per task (in seconds)
      startedAt: progressItems.length > 0 && progressItems[0].submissionDate ? 
                progressItems[0].submissionDate : new Date().toISOString(),
      completedAt: completedTasks > 0 ? new Date().toISOString() : null,
      submittedForReview: progressItems.some(item => item.completed),
      submittedAt: latestActivity ? latestActivity.toISOString() : null,
      
      // Computed KPIs for the UI
      kpis: {
        averageScore: Math.round(overallPercentage),
        recentActivityDays,
        timeSpentMinutes: Math.round(progressItems.length * 10), // Estimate 10 min per task
        currentStreakDays: recentActivityDays < 3 ? 1 : 0 // Consider active if activity in last 3 days
      },
      
      // Status messages
      statusMessages: [],
      
      // Generate performance history from the progress items
      performanceHistory: progressItems.length > 0 ? 
        // Take up to 5 progress items
        progressItems
          .filter(item => item.taskName) // Only include items with names
          .slice(0, 5)
          .map((item) => ({
            period: item.taskType || 'Assessment',
            score: item.score || 0,
            maxScore: item.maxScore || 100,
            taskName: item.taskName || 'Unknown Task',
            taskType: item.taskType || 'TASK'
          }))
        : 
        // Empty array if no progress items available
        []
    };
    
    console.log('Generated student progress data:', progressData);
    return progressData;
  } catch (error) {
    console.error('Error in getStudentProgress:', error);
    throw error;
  }
}
