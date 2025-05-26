/**
 * Client-side wrapper for Score API operations
 */
import {
  getScoreById,
  updateScore,
  updateManualScore,
  createScore,
  getScoresByTaskId,
  getScoresByStudentId,
  getScoresByStudentAndTaskId,
  getQuizScore,
  getAverageQuizScore,
  getExerciseScore,
  getAverageExerciseScore,
  ScoreResponseDTO,
  ScoreBodyDTO,
  ManualGradeDTO
} from './api-score';

// Re-export types for client usage
export type {
  ScoreResponseDTO,
  ScoreBodyDTO,
  ManualGradeDTO
};

/**
 * Get a score by its ID
 */
export async function getScore(physicalId: string): Promise<ScoreResponseDTO> {
  try {
    return await getScoreById(physicalId);
  } catch (error) {
    console.error('Error fetching score:', error);
    throw error;
  }
}

/**
 * Update an existing score
 */
export async function updateScoreData(physicalId: string, scoreData: Partial<ScoreBodyDTO>): Promise<ScoreResponseDTO> {
  try {
    return await updateScore(physicalId, scoreData);
  } catch (error) {
    console.error('Error updating score:', error);
    throw error;
  }
}

/**
 * Update a score with manual grading (teacher grading)
 */
export async function gradeManually(physicalId: string, gradeData: ManualGradeDTO): Promise<ScoreResponseDTO> {
  try {
    return await updateManualScore(physicalId, gradeData);
  } catch (error) {
    console.error('Error updating manual grade:', error);
    throw error;
  }
}

/**
 * Create a new score
 */
export async function addNewScore(scoreData: ScoreBodyDTO): Promise<ScoreResponseDTO> {
  try {
    return await createScore(scoreData);
  } catch (error) {
    console.error('Error creating score:', error);
    throw error;
  }
}

/**
 * Get all scores for a specific task
 */
export async function getTaskScores(taskId: string): Promise<ScoreResponseDTO[]> {
  try {
    return await getScoresByTaskId(taskId);
  } catch (error) {
    console.error('Error fetching task scores:', error);
    throw error;
  }
}

/**
 * Get all scores for a specific student
 */
export async function getStudentScores(studentId: string): Promise<ScoreResponseDTO[]> {
  try {
    return await getScoresByStudentId(studentId);
  } catch (error) {
    console.error('Error fetching student scores:', error);
    throw error;
  }
}

/**
 * Get scores for a specific student on a specific task
 */
export async function getStudentTaskScores(studentId: string, taskId: string): Promise<ScoreResponseDTO[]> {
  try {
    return await getScoresByStudentAndTaskId(studentId, taskId);
  } catch (error) {
    console.error('Error fetching student task scores:', error);
    throw error;
  }
}

/**
 * Get quiz score for a specific student, task, and quiz
 */
export async function getStudentQuizScore(
  studentId: string, 
  taskId: string, 
  quizTemplateId: string
): Promise<ScoreResponseDTO> {
  try {
    return await getQuizScore(studentId, taskId, quizTemplateId);
  } catch (error) {
    console.error('Error fetching student quiz score:', error);
    throw error;
  }
}

/**
 * Get the average score for a quiz across all students
 */
export async function getQuizAverageScore(taskId: string, quizTemplateId: string): Promise<number> {
  try {
    return await getAverageQuizScore(taskId, quizTemplateId);
  } catch (error) {
    console.error('Error fetching average quiz score:', error);
    throw error;
  }
}

/**
 * Get exercise score for a specific student, task, and exercise
 */
export async function getStudentExerciseScore(
  studentId: string, 
  taskId: string, 
  exerciseTemplateId: string
): Promise<ScoreResponseDTO> {
  try {
    return await getExerciseScore(studentId, taskId, exerciseTemplateId);
  } catch (error) {
    console.error('Error fetching student exercise score:', error);
    throw error;
  }
}

/**
 * Get the average score for an exercise across all students
 */
export async function getExerciseAverageScore(taskId: string, exerciseTemplateId: string): Promise<number> {
  try {
    return await getAverageExerciseScore(taskId, exerciseTemplateId);
  } catch (error) {
    console.error('Error fetching average exercise score:', error);
    throw error;
  }
}
