'use client';

/**
 * API functions for Score operations
 */
import { serverApiClient } from '@/lib/api/server-client';

export interface ScoreResponseDTO {
  physicalId: string;
  studentId: string;
  taskId: string;
  score: number;
  maxScore: number;
  quizTemplateId?: string;
  exerciseTemplateId?: string;
  lessonTemplateId?: string;
  attempts: number;
  remainingAttempts: number;
  submissionDate: string;
  manuallyGraded: boolean;
  status: 'PENDING' | 'COMPLETED' | 'GRADED';
  feedback?: string;
  // Exercise-specific fields
  reps?: number;              // Repetitions completed
  goalReps?: number;          // Target repetitions
  accuracy?: number;          // Accuracy percentage (0-100)
  goalAccuracy?: number;      // Target accuracy percentage
}

export interface ScoreBodyDTO {
  studentId: string;
  taskId: string;
  score: number;
  maxScore: number;
  quizTemplateId?: string;
  exerciseTemplateId?: string;
  lessonTemplateId?: string;
  attempts?: number;
  remainingAttempts?: number;
  submissionDate?: string;
  manuallyGraded?: boolean;
  status?: 'PENDING' | 'COMPLETED' | 'GRADED';
  feedback?: string;
  // Exercise-specific fields
  reps?: number;
  goalReps?: number;
  accuracy?: number;
  goalAccuracy?: number;
}

export interface ManualGradeDTO {
  score: number;
  feedback?: string;
}

/**
 * Get a score by its physical ID
 */
export async function getScoreById(physicalId: string): Promise<ScoreResponseDTO> {
  const { data, error, status } = await serverApiClient.get(`/scores/${physicalId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO;
}

/**
 * Update a score
 */
export async function updateScore(physicalId: string, scoreData: Partial<ScoreBodyDTO>): Promise<ScoreResponseDTO> {
  const { data, error, status } = await serverApiClient.put(`/scores/${physicalId}`, scoreData);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to update score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO;
}

/**
 * Update a manual score (for teacher grading)
 */
export async function updateManualScore(physicalId: string, gradeData: ManualGradeDTO): Promise<ScoreResponseDTO> {
  const { data, error, status } = await serverApiClient.put(`/scores/${physicalId}/manual-grade`, gradeData);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to update manual score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO;
}

/**
 * Create a new score
 */
export async function createScore(scoreData: ScoreBodyDTO): Promise<ScoreResponseDTO> {
  const { data, error, status } = await serverApiClient.post('/scores', scoreData);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to create score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO;
}

/**
 * Get scores by task ID
 */
export async function getScoresByTaskId(taskId: string): Promise<ScoreResponseDTO[]> {
  const { data, error, status } = await serverApiClient.get(`/scores/task/${taskId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch scores by task: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO[];
}

/**
 * Get scores by student ID
 */
export async function getScoresByStudentId(studentId: string): Promise<ScoreResponseDTO[]> {
  const { data, error, status } = await serverApiClient.get(`/scores/student/${studentId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch scores by student: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO[];
}

/**
 * Get scores by student ID and task ID
 */
export async function getScoresByStudentAndTaskId(studentId: string, taskId: string): Promise<ScoreResponseDTO[]> {
  const { data, error, status } = await serverApiClient.get(`/scores/student/${studentId}/task/${taskId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch scores by student and task: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO[];
}

/**
 * Get a quiz score by student ID, task ID, and quiz template ID
 */
export async function getQuizScore(
  studentId: string, 
  taskId: string, 
  quizTemplateId: string
): Promise<ScoreResponseDTO> {
  // Construct URL with query parameters
  const url = `/scores/quiz?studentId=${encodeURIComponent(studentId)}&taskId=${encodeURIComponent(taskId)}&quizTemplateId=${encodeURIComponent(quizTemplateId)}`;
  const { data, error, status } = await serverApiClient.get(url);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch quiz score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO;
}

/**
 * Get the average quiz score by task ID and quiz template ID
 */
export async function getAverageQuizScore(taskId: string, quizTemplateId: string): Promise<number> {
  // Construct URL with query parameters
  const url = `/scores/quiz/average?taskId=${encodeURIComponent(taskId)}&quizTemplateId=${encodeURIComponent(quizTemplateId)}`;
  const { data, error, status } = await serverApiClient.get(url);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch average quiz score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as number;
}

/**
 * Get an exercise score by student ID, task ID, and exercise template ID
 */
export async function getExerciseScore(
  studentId: string, 
  taskId: string, 
  exerciseTemplateId: string
): Promise<ScoreResponseDTO> {
  // Construct URL with query parameters
  const url = `/scores/exercise?studentId=${encodeURIComponent(studentId)}&taskId=${encodeURIComponent(taskId)}&exerciseTemplateId=${encodeURIComponent(exerciseTemplateId)}`;
  const { data, error, status } = await serverApiClient.get(url);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch exercise score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as ScoreResponseDTO;
}

/**
 * Get the average exercise score by task ID and exercise template ID
 */
export async function getAverageExerciseScore(taskId: string, exerciseTemplateId: string): Promise<number> {
  // Construct URL with query parameters
  const url = `/scores/exercise/average?taskId=${encodeURIComponent(taskId)}&exerciseTemplateId=${encodeURIComponent(exerciseTemplateId)}`;
  const { data, error, status } = await serverApiClient.get(url);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch average exercise score: ${status}`;
    throw new Error(errorMessage);
  }
  
  return data as number;
}
