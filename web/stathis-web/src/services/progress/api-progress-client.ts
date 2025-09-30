'use client';

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
    const { data, error, status } = await serverApiClient.get(`/v1/students`);
    // Added /v1/ prefix to conform to API versioning convention and removed redundant /api prefix
    
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
 * Get tasks for a specific classroom
 */
export async function getClassroomTasks(classroomId: string): Promise<Record<string, { name: string, type: string }>> {
  try {
    console.log(`Fetching tasks for classroom: ${classroomId}`);
    
    // Use the tasks/classroom/{classroomId} endpoint that has proper permissions
    const { data, error, status } = await serverApiClient.get(`tasks/classroom/${classroomId}`);
    
    if (error || status >= 400) {
      console.warn(`Failed to fetch classroom tasks: ${status}`);
      return {};
    }
    
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
    }
    
    return taskMap;
  } catch (e) {
    console.error('Error fetching classroom tasks:', e);
    return {};
  }
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
 * Get comprehensive progress data for a specific student
 * @param studentId The ID of the student to fetch progress for
 * @returns StudentProgressDTO containing the student's progress data
 */
export async function getStudentProgress(studentId: string): Promise<StudentProgressDTO> {
  try {
    console.log(`Fetching student progress for student ID: ${studentId}`);
    
    // First, get the enhanced scores which now include task names, types, and completion status
    let enhancedScores: ScoreResponseDTO[] = [];
    try {
      enhancedScores = await getStudentScores(studentId);
      console.log(`Retrieved ${enhancedScores.length} enhanced scores with task details`);
    } catch (e) {
      console.error('Error fetching enhanced scores:', e);
    }
    
    // Get user profile information for name and other details
    let firstName = "Student";
    let lastName = "";
    let yearLevel = undefined;
    
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
        yearLevel = userProfile.yearLevel;
        console.log(`Found student name: ${firstName} ${lastName}`);
      }
    } catch (e) {
      console.warn('Could not fetch student profile:', e);
    }
    
    // Calculate statistics from enhanced scores
    // First get filtered scores to remove duplicates/generics
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
    
    // Use the filtered scores for statistics
    const totalScores = filteredScores.length;
    const completedScores = filteredScores.filter(s => s.isCompleted).length;
    
    // Calculate average quiz scores
    const quizScores = filteredScores.filter(s => 
      s.taskType?.toUpperCase() === 'QUIZ' || s.taskType?.includes('QUIZ')
    );
    
    let quizScoreValue = 0;
    let quizMaxValue = 0;
    if (quizScores.length > 0) {
      const scoreSum = quizScores.reduce((sum, s) => sum + (s.scoreValue || 0), 0);
      const maxSum = quizScores.reduce((sum, s) => sum + (s.maxScore || 100), 0);
      quizScoreValue = Math.round(scoreSum / quizScores.length);
      quizMaxValue = Math.round(maxSum / quizScores.length);
    }
    
    // Calculate overall average score as percentage
    const totalPoints = filteredScores.reduce((sum, s) => sum + (s.scoreValue || 0), 0);
    const totalPossible = filteredScores.reduce((sum, s) => sum + (s.maxScore || 100), 0);
    const overallPercentage = totalPossible > 0 ? (totalPoints / totalPossible * 100) : 0;
    
    // Calculate activity metrics based on most recent score
    const sortedScores = [...filteredScores].sort((a, b) => 
      new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime()
    );
    
    const recentActivityDays = sortedScores.length > 0
      ? Math.round((new Date().getTime() - new Date(sortedScores[0].updatedAt).getTime()) / (1000 * 60 * 60 * 24))
      : 7; // Default if no activity
    
    // Create the StudentProgressDTO
    const progressData: StudentProgressDTO = {
      // Basic identification
      studentId,
      fullName: `${firstName} ${lastName}`,
      
      // Task completion data
      lessonCompleted: filteredScores.some(s => s.taskType?.toUpperCase() === 'LESSON' && s.isCompleted),
      exerciseCompleted: filteredScores.some(s => s.taskType?.toUpperCase() === 'EXERCISE' && s.isCompleted),
      quizCompleted: filteredScores.some(s => s.taskType?.toUpperCase() === 'QUIZ' && s.isCompleted),
      quizScore: quizScoreValue,
      maxQuizScore: quizMaxValue || 100, // Use actual max score or default to 100
      quizAttempts: quizScores.length,
      totalTimeTaken: filteredScores.length * 600, // Estimate 10 minutes per task (in seconds)
      startedAt: sortedScores.length > 0 ? sortedScores[sortedScores.length-1].createdAt : new Date().toISOString(),
      completedAt: completedScores > 0 ? new Date().toISOString() : null,
      submittedForReview: false, // We don't have this info in the scores data
      submittedAt: null,
      
      // Computed KPIs for the UI
      kpis: {
        averageScore: Math.round(overallPercentage),
        recentActivityDays,
        timeSpentMinutes: Math.round(filteredScores.length * 10), // Estimate 10 min per task
        currentStreakDays: recentActivityDays < 3 ? 1 : 0 // Consider active if activity in last 3 days
      },
      
      // Status messages
      statusMessages: [],
      
      // Generate performance history from the filtered scores
      performanceHistory: filteredScores.length > 0 ? 
        // Take up to 5 scores
        filteredScores
          .slice(0, 5)
          .map((score) => ({
            period: score.taskType || 'Assessment',
            score: score.scoreValue,
            maxScore: score.maxScore || 100,
            taskName: score.taskName || 'Unknown Task',
            taskType: score.taskType || 'TASK'
          }))
        : 
        // Empty array if no scores available
        []
    };
    
    console.log('Generated student progress data:', progressData);
    return progressData;
  } catch (error) {
    console.error('Error in getStudentProgress:', error);
    throw error;
  }
}
