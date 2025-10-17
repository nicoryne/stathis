'use client';

import { serverApiClient } from '@/lib/api/server-client';

// Task Score interfaces
export interface Score {
  id: string;
  physicalId: string;
  createdAt: string;
  updatedAt: string;
  studentId: string;
  taskId: string;
  quizTemplateId?: string;
  exerciseTemplateId?: string;
  score: number;
  maxScore: number;
  attempts: number;
  timeTaken: number;
  accuracy: number;
  startedAt: string;
  completedAt: string;
  teacherFeedback?: string;
  manualScore?: number;
  completed: boolean;
}

// Analytics interfaces
export interface TaskScoreAnalytics {
  averageScore: number;
  maxScore: number;
  minScore: number;
  completionRate: number;
  totalStudents: number;
  completedStudents: number;
  taskId: string;
  taskName: string;
}

export interface LeaderboardResponseDTO {
  physicalId: string;
  studentId: string;
  taskId: string;
  score: number;
  timeTaken: number;
  accuracy: number;
  rank: number;
  completedAt: string;
}

export interface TaskCompletionCountDTO {
  count: number;
  taskId: string;
}

export interface VitalSignsStatisticsDTO {
  averageHeartRate: number;
  averageOxygenSaturation: number;
  studentCount: number;
  timestamp: string;
}

/**
 * Get scores for a specific task
 */
export async function getTaskScores(taskId: string): Promise<Score[]> {
  const { data, error, status } = await serverApiClient.get(`/v1/scores/task/${taskId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch task scores: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as Score[];
}

/**
 * Get analytics from task scores
 */
export function analyzeTaskScores(scores: Score[], taskName: string): TaskScoreAnalytics {
  if (!scores || scores.length === 0) {
    return {
      averageScore: 0,
      maxScore: 0,
      minScore: 0,
      completionRate: 0,
      totalStudents: 0,
      completedStudents: 0,
      taskId: '',
      taskName: taskName || 'Unknown Task'
    };
  }

  const taskId = scores[0]?.taskId || '';
  
  // Get unique students
  const uniqueStudents = new Set(scores.map(s => s.studentId));
  const totalStudents = uniqueStudents.size;
  
  // Get completed scores
  const completedScores = scores.filter(s => s.completed);
  const completedStudents = new Set(completedScores.map(s => s.studentId)).size;
  
  // Calculate metrics
  const scoreValues = completedScores.map(s => s.score);
  const averageScore = scoreValues.length > 0 
    ? Math.round(scoreValues.reduce((sum, score) => sum + score, 0) / scoreValues.length) 
    : 0;
  
  // Get the task's maximum possible score (should be consistent across all submissions)
  const taskMaxScore = scores.length > 0 ? scores[0].maxScore : 100;
  
  const maxScore = taskMaxScore; // Use the task's max score, not the highest student score
  const minScore = scoreValues.length > 0 ? Math.min(...scoreValues) : 0;
  const completionRate = totalStudents > 0 ? (completedStudents / totalStudents) * 100 : 0;
  
  return {
    averageScore,
    maxScore,
    minScore,
    completionRate,
    totalStudents,
    completedStudents,
    taskId,
    taskName: taskName || 'Unknown Task'
  };
}

/**
 * Get leaderboard for a specific task
 */
export async function getTaskLeaderboard(taskId: string): Promise<LeaderboardResponseDTO[]> {
  const { data, error, status } = await serverApiClient.get(`/achievements/leaderboard?taskId=${taskId}`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch leaderboard: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as LeaderboardResponseDTO[];
}



/**
 * Get vital signs statistics
 */
export async function getVitalSignsStatistics(): Promise<VitalSignsStatisticsDTO> {
  const { data, error, status } = await serverApiClient.get(`/vital-signs/statistics`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch vital signs statistics: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as VitalSignsStatisticsDTO;
}



/**
 * Get count of completed tasks for a specific task
 */
export async function getCompletedTasksCount(taskId: string): Promise<number> {
  const { data, error, status } = await serverApiClient.get(`/tasks/${taskId}/completed-count`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch completed tasks count: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return (data as { count: number }).count;
}

/**
 * Get count of submitted tasks for a specific task
 */
export async function getSubmittedTasksCount(taskId: string): Promise<number> {
  const { data, error, status } = await serverApiClient.get(`/v1/task-completions/task/${taskId}/submitted/count`);
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch submitted tasks count: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as number;
}

/**
 * Get average quiz score
 */
export async function getAverageQuizScore(taskId: string, quizTemplateId: string): Promise<number> {
  const { data, error, status } = await serverApiClient.get(
    `/v1/scores/quiz/average?taskId=${taskId}&quizTemplateId=${quizTemplateId}`
  );
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch average quiz score: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as number;
}

/**
 * Get average exercise score
 */
export async function getAverageExerciseScore(taskId: string, exerciseTemplateId: string): Promise<number> {
  const { data, error, status } = await serverApiClient.get(
    `/v1/scores/exercise/average?taskId=${taskId}&exerciseTemplateId=${exerciseTemplateId}`
  );
  
  if (error || status >= 400) {
    const errorMessage = typeof error === 'object' && error !== null && 'message' in error
      ? (error as { message: string }).message
      : `Failed to fetch average exercise score: ${status}`;
    console.error(errorMessage);
    throw new Error(errorMessage);
  }
  
  return data as number;
}
