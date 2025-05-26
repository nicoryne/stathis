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
  title: string;
  description: string;
  exerciseType: string;
  exerciseDifficulty: string;
  goalReps: string;
  goalAccuracy: string;
  goalTime: string;
}

export interface ExerciseTemplateResponseDTO {
  physicalId: string;
  title: string;
  description: string;
  exerciseType: 'PUSH_UP' | 'SQUATS';
  exerciseDifficulty: 'BEGINNER' | 'EXPERT';
  goalReps: number;
  goalAccuracy: number;
  goalTime: number;
}

/**
 * Create a new lesson template
 */
export async function createLessonTemplate(template: LessonTemplateBodyDTO) {
  try {
    console.log('Creating lesson template:', template);
    
    const { data, error, status } = await serverApiClient.post('/lesson-templates', template);
    
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
    const { data, error, status } = await serverApiClient.get(`/lesson-templates/${physicalId}`);
    
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
    
    const { data, error, status } = await serverApiClient.post('/quiz-templates', template);
    
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
    const { data, error, status } = await serverApiClient.get(`/quiz-templates/${physicalId}`);
    
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
    const { data, error, status } = await serverApiClient.get('/lesson-templates/all');
    
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
    const { data, error, status } = await serverApiClient.get('/quiz-templates/all');
    
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
 * Create a new exercise template
 */
export async function createExerciseTemplate(template: ExerciseTemplateBodyDTO) {
  try {
    console.log('Creating exercise template:', template);
    
    const { data, error, status } = await serverApiClient.post('/exercise-templates', template);
    
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
    const { data, error, status } = await serverApiClient.get(`/exercise-templates/${physicalId}`);
    
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
    const { data, error, status } = await serverApiClient.get('/exercise-templates/all');
    
    if (error) {
      console.error('[Exercise Templates Get All Error]', { error, status });
      throw new Error(error);
    }
    
    return data as ExerciseTemplateResponseDTO[];
  } catch (error) {
    console.error('Error getting all exercise templates:', error);
    throw error;
  }
}
