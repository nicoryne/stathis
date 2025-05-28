import {
  LessonTemplateBodyDTO,
  LessonTemplateResponseDTO,
  QuizTemplateBodyDTO, 
  QuizTemplateResponseDTO,
  ExerciseTemplateBodyDTO,
  ExerciseTemplateResponseDTO,
  createLessonTemplate as serverCreateLessonTemplate,
  getLessonTemplate as serverGetLessonTemplate,
  getTeacherLessonTemplates as serverGetTeacherLessonTemplates,
  updateLessonTemplate as serverUpdateLessonTemplate,
  deleteLessonTemplate as serverDeleteLessonTemplate,
  createQuizTemplate as serverCreateQuizTemplate,
  getQuizTemplate as serverGetQuizTemplate,
  getTeacherQuizTemplates as serverGetTeacherQuizTemplates,
  updateQuizTemplate as serverUpdateQuizTemplate,
  deleteQuizTemplate as serverDeleteQuizTemplate,
  createExerciseTemplate as serverCreateExerciseTemplate,
  getExerciseTemplate as serverGetExerciseTemplate,
  getTeacherExerciseTemplates as serverGetTeacherExerciseTemplates,
  updateExerciseTemplate as serverUpdateExerciseTemplate,
  deleteExerciseTemplate as serverDeleteExerciseTemplate
} from './api-template';

/**
 * Create a new lesson template
 * Requires teacher role
 */
export async function createLessonTemplate(template: LessonTemplateBodyDTO) {
  return serverCreateLessonTemplate(template);
}

/**
 * Get a specific lesson template by ID
 * @param physicalId The physical ID of the lesson template
 */
export async function getLessonTemplate(physicalId: string) {
  return serverGetLessonTemplate(physicalId);
}

/**
 * Get all lesson templates created by the current teacher
 * Uses the security context to determine the current teacher
 */
export async function getTeacherLessonTemplates() {
  return serverGetTeacherLessonTemplates();
}

/**
 * Update a lesson template
 * Only the teacher who created the template can update it
 */
export async function updateLessonTemplate(physicalId: string, template: LessonTemplateBodyDTO) {
  return serverUpdateLessonTemplate(physicalId, template);
}

/**
 * Delete a lesson template
 * Only the teacher who created the template can delete it
 */
export async function deleteLessonTemplate(physicalId: string) {
  return serverDeleteLessonTemplate(physicalId);
}

/**
 * Create a new quiz template
 * Requires teacher role
 */
export async function createQuizTemplate(template: QuizTemplateBodyDTO) {
  return serverCreateQuizTemplate(template);
}

/**
 * Get a specific quiz template by ID
 * @param physicalId The physical ID of the quiz template
 */
export async function getQuizTemplate(physicalId: string) {
  return serverGetQuizTemplate(physicalId);
}

/**
 * Get all quiz templates created by the current teacher
 * Uses the security context to determine the current teacher
 */
export async function getTeacherQuizTemplates() {
  return serverGetTeacherQuizTemplates();
}

/**
 * Update a quiz template
 * Only the teacher who created the template can update it
 */
export async function updateQuizTemplate(physicalId: string, template: QuizTemplateBodyDTO) {
  return serverUpdateQuizTemplate(physicalId, template);
}

/**
 * Delete a quiz template
 * Only the teacher who created the template can delete it
 */
export async function deleteQuizTemplate(physicalId: string) {
  return serverDeleteQuizTemplate(physicalId);
}

/**
 * Create a new exercise template
 * Requires teacher role
 */
export async function createExerciseTemplate(template: ExerciseTemplateBodyDTO) {
  return serverCreateExerciseTemplate(template);
}

/**
 * Get a specific exercise template by ID
 * @param physicalId The physical ID of the exercise template
 */
export async function getExerciseTemplate(physicalId: string) {
  return serverGetExerciseTemplate(physicalId);
}

/**
 * Get all exercise templates created by the current teacher
 * Uses the security context to determine the current teacher
 */
export async function getTeacherExerciseTemplates() {
  return serverGetTeacherExerciseTemplates();
}

/**
 * Update an exercise template
 * Only the teacher who created the template can update it
 */
export async function updateExerciseTemplate(physicalId: string, template: ExerciseTemplateBodyDTO) {
  return serverUpdateExerciseTemplate(physicalId, template);
}

/**
 * Delete an exercise template
 * Only the teacher who created the template can delete it
 */
export async function deleteExerciseTemplate(physicalId: string) {
  return serverDeleteExerciseTemplate(physicalId);
}

// Export types
export type {
  LessonTemplateBodyDTO,
  LessonTemplateResponseDTO,
  QuizTemplateBodyDTO,
  QuizTemplateResponseDTO,
  ExerciseTemplateBodyDTO,
  ExerciseTemplateResponseDTO
};
