package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import edu.cit.stathis.task.service.LessonTemplateService;
import edu.cit.stathis.task.service.ExerciseTemplateService;
import edu.cit.stathis.task.service.QuizTemplateService;
import edu.cit.stathis.task.dto.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import edu.cit.stathis.auth.service.PhysicalIdService;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    @Autowired
    private LessonTemplateService lessonTemplateService;

    @Autowired
    private ExerciseTemplateService exerciseTemplateService;

    @Autowired
    private QuizTemplateService quizTemplateService;

    @Autowired
    private PhysicalIdService physicalIdService;

    // Lesson Template Endpoints
    @PostMapping("/lessons")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new lesson template")
    public ResponseEntity<LessonTemplateResponseDTO> createLessonTemplate(@RequestBody LessonTemplateBodyDTO lessonTemplateBodyDTO) {
        return ResponseEntity.ok(lessonTemplateService.getLessonTemplateResponseDTO(
            lessonTemplateService.createLessonTemplate(lessonTemplateBodyDTO).getPhysicalId()));
    }

    @GetMapping("lessons/{physicalId}")
    @Operation(summary = "Get a lesson template by its physical ID", description = "Get a lesson template by its physical ID")
    public ResponseEntity<LessonTemplateResponseDTO> getLessonTemplate(@PathVariable String physicalId) {
        return ResponseEntity.ok(lessonTemplateService.getLessonTemplateResponseDTO(physicalId));
    }

    @GetMapping("/lessons")
    @Operation(summary = "Get all lesson templates")
    public ResponseEntity<List<LessonTemplateResponseDTO>> getAllLessonTemplates() {
        return ResponseEntity.ok(lessonTemplateService.getAllLessonTemplates().stream()
            .map(lesson -> lessonTemplateService.getLessonTemplateResponseDTO(lesson.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/lessons/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get all lesson templates by teacher physical id")
    public ResponseEntity<List<LessonTemplateResponseDTO>> getAllLessonTemplatesByTeacherPhysicalId() {
        return ResponseEntity.ok(lessonTemplateService.getAllLessonTemplatesByTeacherPhysicalId(physicalIdService.getCurrentUserPhysicalId()).stream()
            .map(lesson -> lessonTemplateService.getLessonTemplateResponseDTO(lesson.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }

    @PutMapping("/lessons/{physicalId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update a lesson template by its physical ID")
    public ResponseEntity<LessonTemplateResponseDTO> updateLessonTemplate(@PathVariable String physicalId, @RequestBody LessonTemplateBodyDTO lessonTemplateBodyDTO) {
        return ResponseEntity.ok(lessonTemplateService.getLessonTemplateResponseDTO(lessonTemplateService.updateLessonTemplate(physicalId, lessonTemplateBodyDTO).getPhysicalId()));
    }

    @DeleteMapping("/lessons/{physicalId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete a lesson template by its physical ID")
    public ResponseEntity<Void> deleteLessonTemplate(@PathVariable String physicalId) {
        lessonTemplateService.deleteLessonTemplate(physicalId);
        return ResponseEntity.ok().build();
    }

    // Exercise Template Endpoints
    @PostMapping("/exercises")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new exercise template")
    public ResponseEntity<ExerciseTemplateResponseDTO> createExerciseTemplate(@RequestBody ExerciseTemplateBodyDTO exerciseTemplateBodyDTO) {
        return ResponseEntity.ok(exerciseTemplateService.getExerciseTemplateResponseDTO(
            exerciseTemplateService.createExerciseTemplate(exerciseTemplateBodyDTO).getPhysicalId()));
    }

    @GetMapping("exercises/{physicalId}")
    @Operation(summary = "Get an exercise template by its physical ID", description = "Get an exercise template by its physical ID")
    public ResponseEntity<ExerciseTemplateResponseDTO> getExerciseTemplate(@PathVariable String physicalId) {
        return ResponseEntity.ok(exerciseTemplateService.getExerciseTemplateResponseDTO(physicalId));
    }

    @GetMapping("/exercises")
    @Operation(summary = "Get all exercise templates")
    public ResponseEntity<List<ExerciseTemplateResponseDTO>> getAllExerciseTemplates() {
        return ResponseEntity.ok(exerciseTemplateService.getAllExerciseTemplates().stream()
            .map(exercise -> exerciseTemplateService.getExerciseTemplateResponseDTO(exercise.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/exercises/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get all exercise templates by teacher physical id")
    public ResponseEntity<List<ExerciseTemplateResponseDTO>> getAllExerciseTemplatesByTeacherPhysicalId() {
        return ResponseEntity.ok(exerciseTemplateService.getAllExerciseTemplatesByTeacherPhysicalId(physicalIdService.getCurrentUserPhysicalId()).stream()
            .map(exercise -> exerciseTemplateService.getExerciseTemplateResponseDTO(exercise.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }

    @PutMapping("/exercises/{physicalId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update an exercise template by its physical ID")
    public ResponseEntity<ExerciseTemplateResponseDTO> updateExerciseTemplate(@PathVariable String physicalId, @RequestBody ExerciseTemplateBodyDTO exerciseTemplateBodyDTO) {
        return ResponseEntity.ok(exerciseTemplateService.getExerciseTemplateResponseDTO(exerciseTemplateService.updateExerciseTemplate(physicalId, exerciseTemplateBodyDTO).getPhysicalId()));
    }

    @DeleteMapping("/exercises/{physicalId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete an exercise template by its physical ID")
    public ResponseEntity<Void> deleteExerciseTemplate(@PathVariable String physicalId) {
        exerciseTemplateService.deleteExerciseTemplate(physicalId);
        return ResponseEntity.ok().build();
    }

    // Quiz Template Endpoints
    @PostMapping("/quizzes")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new quiz template")
    public ResponseEntity<QuizTemplateResponseDTO> createQuizTemplate(@RequestBody QuizTemplateBodyDTO quizTemplateBodyDTO) {
        return ResponseEntity.ok(quizTemplateService.getQuizTemplateResponseDTO(
            quizTemplateService.createQuizTemplate(quizTemplateBodyDTO).getPhysicalId()));
    }

    @GetMapping("quizzes/{physicalId}")
    @Operation(summary = "Get a quiz template by its physical ID", description = "Get a quiz template by its physical ID")
    public ResponseEntity<QuizTemplateResponseDTO> getQuizTemplate(@PathVariable String physicalId) {
        return ResponseEntity.ok(quizTemplateService.getQuizTemplateResponseDTO(physicalId));
    }

    @GetMapping("/quizzes")
    @Operation(summary = "Get all quiz templates")
    public ResponseEntity<List<QuizTemplateResponseDTO>> getAllQuizTemplates() {
        return ResponseEntity.ok(quizTemplateService.getAllQuizTemplates().stream()
            .map(quiz -> quizTemplateService.getQuizTemplateResponseDTO(quiz.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/quizzes/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get all quiz templates by teacher physical id")
    public ResponseEntity<List<QuizTemplateResponseDTO>> getAllQuizTemplatesByTeacherPhysicalId() {
        return ResponseEntity.ok(quizTemplateService.getAllQuizTemplatesByTeacherPhysicalId(physicalIdService.getCurrentUserPhysicalId()).stream()
            .map(quiz -> quizTemplateService.getQuizTemplateResponseDTO(quiz.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }

    @PutMapping("/quizzes/{physicalId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update a quiz template by its physical ID")
    public ResponseEntity<QuizTemplateResponseDTO> updateQuizTemplate(@PathVariable String physicalId, @RequestBody QuizTemplateBodyDTO quizTemplateBodyDTO) {
        return ResponseEntity.ok(quizTemplateService.getQuizTemplateResponseDTO(quizTemplateService.updateQuizTemplate(physicalId, quizTemplateBodyDTO).getPhysicalId()));
    }

    @DeleteMapping("/quizzes/{physicalId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete a quiz template by its physical ID")
    public ResponseEntity<Void> deleteQuizTemplate(@PathVariable String physicalId) {
        quizTemplateService.deleteQuizTemplate(physicalId);
        return ResponseEntity.ok().build();
    }
} 