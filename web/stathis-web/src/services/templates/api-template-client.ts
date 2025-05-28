'use client';

import {
  LessonTemplateBodyDTO,
  LessonTemplateResponseDTO,
  QuizTemplateBodyDTO, 
  QuizTemplateResponseDTO,
  ExerciseTemplateBodyDTO,
  ExerciseTemplateResponseDTO,
  createLessonTemplate as serverCreateLessonTemplate,
  getLessonTemplate as serverGetLessonTemplate,
  getAllLessonTemplates as serverGetAllLessonTemplates,
  createQuizTemplate as serverCreateQuizTemplate,
  getQuizTemplate as serverGetQuizTemplate,
  getAllQuizTemplates as serverGetAllQuizTemplates,
  createExerciseTemplate as serverCreateExerciseTemplate,
  getExerciseTemplate as serverGetExerciseTemplate,
  getAllExerciseTemplates as serverGetAllExerciseTemplates
} from './api-template';

/**
 * Client-side wrapper for template APIs
 */

// Lesson Template functions
export async function createLessonTemplate(template: LessonTemplateBodyDTO) {
  return serverCreateLessonTemplate(template);
}

export async function getLessonTemplate(physicalId: string) {
  return serverGetLessonTemplate(physicalId);
}

export async function getAllLessonTemplates() {
  return serverGetAllLessonTemplates();
}

// Quiz Template functions
export async function createQuizTemplate(template: QuizTemplateBodyDTO) {
  return serverCreateQuizTemplate(template);
}

export async function getQuizTemplate(physicalId: string) {
  return serverGetQuizTemplate(physicalId);
}

export async function getAllQuizTemplates() {
  return serverGetAllQuizTemplates();
}

// Exercise Template functions
export async function createExerciseTemplate(template: ExerciseTemplateBodyDTO) {
  return serverCreateExerciseTemplate(template);
}

export async function getExerciseTemplate(physicalId: string) {
  return serverGetExerciseTemplate(physicalId);
}

export async function getAllExerciseTemplates() {
  return serverGetAllExerciseTemplates();
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
