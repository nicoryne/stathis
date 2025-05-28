import { useQuery } from '@tanstack/react-query';
import { 
  getTaskScores, 
  getTaskLeaderboard,
  getVitalSignsStatistics,
  Score,
  LeaderboardResponseDTO,
  VitalSignsStatisticsDTO
} from '@/services/analytics/api-analytics-client';
import { Task, getTasksByClassroom } from '@/services/api-task-client';

/**
 * Hook to fetch task scores
 */
export function useTaskScores(taskId?: string) {
  return useQuery<Score[]>({
    queryKey: ['task-scores', taskId],
    queryFn: () => getTaskScores(taskId || ''),
    enabled: !!taskId,
  });
}

/**
 * Hook to fetch task leaderboard
 */
export function useTaskLeaderboard(taskId?: string) {
  return useQuery<LeaderboardResponseDTO[]>({
    queryKey: ['task-leaderboard', taskId],
    queryFn: () => getTaskLeaderboard(taskId || ''),
    enabled: !!taskId,
  });
}

/**
 * Hook to fetch active tasks
 */
export function useActiveTasks(classroomId?: string) {
  return useQuery<Task[]>({
    queryKey: ['active-tasks', classroomId],
    queryFn: () => getTasksByClassroom(classroomId || ''),
    enabled: !!classroomId
  });
}

/**
 * Hook to fetch vital signs statistics
 */
export function useVitalSignsStatistics() {
  return useQuery<VitalSignsStatisticsDTO>({
    queryKey: ['vital-signs-statistics'],
    queryFn: getVitalSignsStatistics,
    refetchInterval: 30000, // Refetch every 30 seconds
  });
}
