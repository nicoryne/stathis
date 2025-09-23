'use client';

import { serverApiClient } from '@/lib/api/server-client';
import { getCurrentUserPhysicalId } from '@/lib/utils/jwt';

/**
 * Server-side functions for template APIs
 */

// Types
export interface LessonTemplateBodyDTO {
  title: string;
  description: string;
  content: Record<string, any>;
}

export interface LessonTemplateResponseDTO {
  physicalId: string;
  title: string;
  description: string;
  content: Record<string, any>;
}

export interface QuizTemplateBodyDTO {
  title: string;
  instruction: string;
  maxScore: number;
  content: Record<string, any>;
}

export interface QuizTemplateResponseDTO {
  physicalId: string;
  title: string;
  instruction: string;
  maxScore: number;
  content: Record<string, any>;
}

export interface ExerciseTemplateBodyDTO {
  title: string; // required, minLength: 3, maxLength: 100
  description: string; // required, minLength: 0, maxLength: 500
  exerciseType: 'PUSH_UP' | 'SQUATS'; // required - updated to match backend enum
  exerciseDifficulty: 'BEGINNER' | 'EXPERT'; // required - updated to match backend enum
  goalReps: string; // required, pattern: ^[0-9]+$
  goalAccuracy: string; // required, pattern: ^[0-9]+$
  goalTime: string; // required, pattern: ^[0-9]+$
}

export interface ExerciseTemplateResponseDTO {
  physicalId: string;
  title: string;
  description: string;
  exerciseType: 'PUSH_UP' | 'SQUATS'; // Note: API lists PUSH_UP and SQUATS as enum values
  exerciseDifficulty: 'BEGINNER' | 'EXPERT'; // Note: API lists BEGINNER and EXPERT as enum values
  goalReps: number; // integer
  goalAccuracy: number; // integer
  goalTime: number; // integer
}

/**
 * Create a new lesson template
 */
export async function createLessonTemplate(template: LessonTemplateBodyDTO) {
  try {
    console.log('Creating lesson template:', template);
    
    const { data, error, status } = await serverApiClient.post('/templates/lessons', template);
    
    if (error) {
      console.error('[Lesson Template Create Error]', { error, status, requestBody: template });
      throw new Error(error);
    }
    
    return data as LessonTemplateResponseDTO;
  } catch (error) {
    console.error('Error creating lesson template:', error);
    throw error;
  }
}

/**
 * Get a lesson template by ID
 */
export async function getLessonTemplate(physicalId: string) {
  try {
    const { data, error, status } = await serverApiClient.get(`/templates/lessons/${physicalId}`);
    
    if (error) {
      console.error('[Lesson Template Get Error]', { error, status });
      throw new Error(error);
    }
    
    return data as LessonTemplateResponseDTO;
  } catch (error) {
    console.error('Error getting lesson template:', error);
    throw error;
  }
}

/**
 * Create a new quiz template
 */
export async function createQuizTemplate(template: QuizTemplateBodyDTO) {
  try {
    console.log('Creating quiz template:', template);
    
    const { data, error, status } = await serverApiClient.post('/templates/quizzes', template);
    
    if (error) {
      console.error('[Quiz Template Create Error]', { error, status, requestBody: template });
      throw new Error(error);
    }
    
    return data as QuizTemplateResponseDTO;
  } catch (error) {
    console.error('Error creating quiz template:', error);
    throw error;
  }
}

/**
 * Get a quiz template by ID
 */
export async function getQuizTemplate(physicalId: string) {
  try {
    const { data, error, status } = await serverApiClient.get(`/templates/quizzes/${physicalId}`);
    
    if (error) {
      console.error('[Quiz Template Get Error]', { error, status });
      throw new Error(error);
    }
    
    return data as QuizTemplateResponseDTO;
  } catch (error) {
    console.error('Error getting quiz template:', error);
    throw error;
  }
}

/**
 * Get all lesson templates
 */
export async function getAllLessonTemplates() {
  try {
    const { data, error, status } = await serverApiClient.get('/templates/lessons');
    
    if (error) {
      console.error('[Lesson Templates Get Error]', { error, status });
      
      // If we get a 403 error, return mock data for development
      if (status === 403) {
        console.warn('Using mock data for lesson templates due to 403 error');
        return getMockLessonTemplates();
      }
      
      throw new Error(error);
    }
    
    return data as LessonTemplateResponseDTO[];
  } catch (error) {
    console.error('Error getting all lesson templates:', error);
    
    // Return mock data for any error during development
    if (process.env.NODE_ENV !== 'production') {
      console.warn('Using mock data for lesson templates due to error');
      return getMockLessonTemplates();
    }
    
    throw error;
  }
}

/**
 * Get all lesson templates created by the current teacher
 * Uses the security context to identify the teacher
 */
export async function getTeacherLessonTemplates() {
  try {
    console.log('Getting lesson templates for current teacher');
    const { data, error, status } = await serverApiClient.get('/templates/lessons/teacher');
    
    if (error) {
      console.error('[Teacher Lesson Templates Get Error]', { error, status });
      // If we get an error, return mock data for development
      if (process.env.NODE_ENV !== 'production') {
        console.warn('Using mock data for teacher lesson templates due to error');
        return getMockLessonTemplates();
      }
      throw new Error(error);
    }
    
    return data as LessonTemplateResponseDTO[];
  } catch (error) {
    console.error('Error getting teacher lesson templates:', error);
    // Return mock data for any error during development
    if (process.env.NODE_ENV !== 'production') {
      console.warn('Using mock data for teacher lesson templates due to error');
      return getMockLessonTemplates();
    }
    throw error;
  }
}

/**
 * Update a lesson template
 * Only the teacher who created the template can update it
 */
export async function updateLessonTemplate(physicalId: string, template: LessonTemplateBodyDTO) {
  try {
    console.log('Updating lesson template:', physicalId);
    
    const { data, error, status } = await serverApiClient.put(`/templates/lessons/${physicalId}`, template);
    
    if (error) {
      console.error('[Lesson Template Update Error]', { error, status, physicalId });
      throw new Error(error);
    }
    
    return data as LessonTemplateResponseDTO;
  } catch (error) {
    console.error('Error updating lesson template:', error);
    throw error;
  }
}

/**
 * Delete a lesson template
 * Only the teacher who created the template can delete it
 */
export async function deleteLessonTemplate(physicalId: string) {
  try {
    console.log('Deleting lesson template:', physicalId);
    
    // First, verify this is the teacher's template by checking against the teacher templates list
    const teacherTemplates = await getTeacherLessonTemplates();
    const isTeacherTemplate = teacherTemplates.some(template => template.physicalId === physicalId);
    
    if (!isTeacherTemplate) {
      throw new Error(`Template with ID ${physicalId} not found in your templates. You can only delete your own templates.`);
    }
    
    // Now perform the delete operation with authorization headers
    const { error } = await serverApiClient.delete(`/templates/lessons/${physicalId}`, {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include'
    });
    
    if (error) {
      console.error('[Lesson Template Delete Error]', error);
      throw new Error(`Failed to delete template: ${error}`);
    }
    
    return true;
  } catch (error) {
    console.error('Error deleting lesson template:', error);
    throw error;
  }
}

/**
 * Generate mock lesson templates for development
 */
function getMockLessonTemplates(): LessonTemplateResponseDTO[] {
  return [
    {
      physicalId: 'LESSON-MOCK-1',
      title: 'Introduction to Mathematics',
      description: 'Basic overview of mathematical concepts',
      content: {
        sections: [
          {
            title: 'Numbers and Operations',
            content: 'Introduction to basic arithmetic operations.'
          },
          {
            title: 'Algebra Basics',
            content: 'Introduction to variables and equations.'
          }
        ]
      }
    },
    {
      physicalId: 'LESSON-MOCK-2',
      title: 'History of Science',
      description: 'Overview of major scientific discoveries',
      content: {
        sections: [
          {
            title: 'Ancient Discoveries',
            content: 'Scientific achievements in ancient civilizations.'
          },
          {
            title: 'Modern Science',
            content: 'The scientific revolution and beyond.'
          }
        ]
      }
    }
  ];
}

/**
 * Get all quiz templates
 */
export async function getAllQuizTemplates() {
  try {
    const { data, error, status } = await serverApiClient.get('/templates/quizzes');
    
    if (error) {
      console.error('[Quiz Templates Get All Error]', { error, status });
      throw new Error(error);
    }
    
    return data as QuizTemplateResponseDTO[];
  } catch (error) {
    console.error('Error getting all quiz templates:', error);
    throw error;
  }
}

/**
 * Get all quiz templates created by the current teacher
 * Uses the security context to identify the teacher
 */
export async function getTeacherQuizTemplates() {
  try {
    console.log('Getting quiz templates for current teacher');
    const { data, error, status } = await serverApiClient.get('/templates/quizzes/teacher');
    
    if (error) {
      console.error('[Teacher Quiz Templates Get Error]', { error, status });
      throw new Error(error);
    }
    
    return data as QuizTemplateResponseDTO[];
  } catch (error) {
    console.error('Error getting teacher quiz templates:', error);
    throw error;
  }
}

/**
 * Update a quiz template
 * Only the teacher who created the template can update it
 */
export async function updateQuizTemplate(physicalId: string, template: QuizTemplateBodyDTO) {
  try {
    console.log('Updating quiz template:', physicalId);
    
    const { data, error, status } = await serverApiClient.put(`/templates/quizzes/${physicalId}`, template);
    
    if (error) {
      console.error('[Quiz Template Update Error]', { error, status, physicalId });
      throw new Error(error);
    }
    
    return data as QuizTemplateResponseDTO;
  } catch (error) {
    console.error('Error updating quiz template:', error);
    throw error;
  }
}

/**
 * Delete a quiz template
 * Only the teacher who created the template can delete it
 */
export async function deleteQuizTemplate(physicalId: string) {
  try {
    console.log('Deleting quiz template:', physicalId);
    
    // First, verify this is the teacher's template by checking against the teacher templates list
    const teacherTemplates = await getTeacherQuizTemplates();
    const isTeacherTemplate = teacherTemplates.some(template => template.physicalId === physicalId);
    
    if (!isTeacherTemplate) {
      throw new Error(`Template with ID ${physicalId} not found in your templates. You can only delete your own templates.`);
    }
    
    // Now perform the delete operation with authorization headers
    const { error } = await serverApiClient.delete(`/templates/quizzes/${physicalId}`, {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include'
    });
    
    if (error) {
      console.error('[Quiz Template Delete Error]', error);
      throw new Error(`Failed to delete template: ${error}`);
    }
    
    return true;
  } catch (error) {
    console.error('Error deleting quiz template:', error);
    throw error;
  }
}

/**
 * Create a new exercise template
 */
export async function createExerciseTemplate(template: ExerciseTemplateBodyDTO) {
  try {
    console.log('Creating exercise template:', template);
    
    const { data, error, status } = await serverApiClient.post('/templates/exercises', template);
    
    if (error) {
      console.error('[Exercise Template Create Error]', { error, status, requestBody: template });
      throw new Error(error);
    }
    
    return data as ExerciseTemplateResponseDTO;
  } catch (error) {
    console.error('Error creating exercise template:', error);
    throw error;
  }
}

/**
 * Get an exercise template by ID
 */
export async function getExerciseTemplate(physicalId: string) {
  try {
    const { data, error, status } = await serverApiClient.get(`/templates/exercises/${physicalId}`);
    
    if (error) {
      console.error('[Exercise Template Get Error]', { error, status });
      throw new Error(error);
    }
    
    return data as ExerciseTemplateResponseDTO;
  } catch (error) {
    console.error('Error getting exercise template:', error);
    throw error;
  }
}

/**
 * Get all exercise templates
 */
export async function getAllExerciseTemplates() {
  try {
    const { data, error, status } = await serverApiClient.get('/templates/exercises');
    
    if (error) {
      console.error('[Exercise Templates Get All Error]', { error, status });
      
      // If we get a 403 error, return mock data for development
      if (status === 403) {
        console.warn('Using mock data for exercise templates due to 403 error');
        return getMockExerciseTemplates();
      }
      
      throw new Error(error);
    }
    
    return data as ExerciseTemplateResponseDTO[];
  } catch (error) {
    console.error('Error getting all exercise templates:', error);
    
    // Return mock data for any error during development
    if (process.env.NODE_ENV !== 'production') {
      console.warn('Using mock data for exercise templates due to error');
      return getMockExerciseTemplates();
    }
    
    throw error;
  }
}

/**
 * Get all exercise templates created by the current teacher
 * Uses the security context to identify the teacher
 */
export async function getTeacherExerciseTemplates() {
  try {
    console.log('Getting exercise templates for current teacher');
    const { data, error, status } = await serverApiClient.get('/templates/exercises/teacher');
    
    if (error) {
      console.error('[Teacher Exercise Templates Get Error]', { error, status });
      // If we get an error, return mock data for development
      if (process.env.NODE_ENV !== 'production') {
        console.warn('Using mock data for teacher exercise templates due to error');
        return getMockExerciseTemplates();
      }
      throw new Error(error);
    }
    
    return data as ExerciseTemplateResponseDTO[];
  } catch (error) {
    console.error('Error getting teacher exercise templates:', error);
    // Return mock data for any error during development
    if (process.env.NODE_ENV !== 'production') {
      console.warn('Using mock data for teacher exercise templates due to error');
      return getMockExerciseTemplates();
    }
    throw error;
  }
}

/**
 * Update an exercise template
 * Only the teacher who created the template can update it
 */
export async function updateExerciseTemplate(physicalId: string, template: ExerciseTemplateBodyDTO) {
  try {
    console.log('Updating exercise template:', physicalId);
    
    const { data, error, status } = await serverApiClient.put(`/templates/exercises/${physicalId}`, template);
    
    if (error) {
      console.error('[Exercise Template Update Error]', { error, status, physicalId });
      throw new Error(error);
    }
    
    return data as ExerciseTemplateResponseDTO;
  } catch (error) {
    console.error('Error updating exercise template:', error);
    throw error;
  }
}

/**
 * Delete an exercise template
 * Only the teacher who created the template can delete it
 */
export async function deleteExerciseTemplate(physicalId: string) {
  try {
    console.log('Deleting exercise template:', physicalId);
    
    // First, verify this is the teacher's template by checking against the teacher templates list
    const teacherTemplates = await getTeacherExerciseTemplates();
    const isTeacherTemplate = teacherTemplates.some(template => template.physicalId === physicalId);
    
    if (!isTeacherTemplate) {
      throw new Error(`Template with ID ${physicalId} not found in your templates. You can only delete your own templates.`);
    }
    
    // Now perform the delete operation with authorization headers
    const { error } = await serverApiClient.delete(`/templates/exercises/${physicalId}`, {
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include'
    });
    
    if (error) {
      console.error('[Exercise Template Delete Error]', error);
      throw new Error(`Failed to delete template: ${error}`);
    }
    
    return true;
  } catch (error) {
    console.error('Error deleting exercise template:', error);
    throw error;
  }
}

/**
 * Generate mock exercise templates for development
 */
function getMockExerciseTemplates(): ExerciseTemplateResponseDTO[] {
  return [
    {
      physicalId: 'EXERCISE-MOCK-1',
      title: 'Basic Push-ups',
      description: 'Standard push-up exercise for beginners',
      exerciseType: 'PUSH_UP',
      exerciseDifficulty: 'BEGINNER',
      goalReps: 10,
      goalAccuracy: 80,
      goalTime: 60
    },
    {
      physicalId: 'EXERCISE-MOCK-2',
      title: 'Advanced Squats',
      description: 'Deep squats for experienced students',
      exerciseType: 'SQUATS',
      exerciseDifficulty: 'EXPERT',
      goalReps: 20,
      goalAccuracy: 90,
      goalTime: 120
    }
  ];
}
