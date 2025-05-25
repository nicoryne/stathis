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

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    @Autowired
    private LessonTemplateService lessonTemplateService;

    @Autowired
    private ExerciseTemplateService exerciseTemplateService;

    @Autowired
    private QuizTemplateService quizTemplateService;

    // Lesson Template Endpoints
    @PostMapping("/lessons")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new lesson template")
    public ResponseEntity<LessonTemplateResponseDTO> createLessonTemplate(@RequestBody LessonTemplateBodyDTO lessonTemplateBodyDTO) {
        return ResponseEntity.ok(lessonTemplateService.getLessonTemplateResponseDTO(
            lessonTemplateService.createLessonTemplate(lessonTemplateBodyDTO).getPhysicalId()));
    }

    @GetMapping("/lessons")
    @Operation(summary = "Get all lesson templates")
    public ResponseEntity<List<LessonTemplateResponseDTO>> getAllLessonTemplates() {
        return ResponseEntity.ok(lessonTemplateService.getAllLessonTemplates().stream()
            .map(lesson -> lessonTemplateService.getLessonTemplateResponseDTO(lesson.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }

    // Exercise Template Endpoints
    @PostMapping("/exercises")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new exercise template")
    public ResponseEntity<ExerciseTemplateResponseDTO> createExerciseTemplate(@RequestBody ExerciseTemplateBodyDTO exerciseTemplateBodyDTO) {
        return ResponseEntity.ok(exerciseTemplateService.getExerciseTemplateResponseDTO(
            exerciseTemplateService.createExerciseTemplate(exerciseTemplateBodyDTO).getPhysicalId()));
    }

    @GetMapping("/exercises")
    @Operation(summary = "Get all exercise templates")
    public ResponseEntity<List<ExerciseTemplateResponseDTO>> getAllExerciseTemplates() {
        return ResponseEntity.ok(exerciseTemplateService.getAllExerciseTemplates().stream()
            .map(exercise -> exerciseTemplateService.getExerciseTemplateResponseDTO(exercise.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }

    // Quiz Template Endpoints
    @PostMapping("/quizzes")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new quiz template")
    public ResponseEntity<QuizTemplateResponseDTO> createQuizTemplate(@RequestBody QuizTemplateBodyDTO quizTemplateBodyDTO) {
        return ResponseEntity.ok(quizTemplateService.getQuizTemplateResponseDTO(
            quizTemplateService.createQuizTemplate(quizTemplateBodyDTO).getPhysicalId()));
    }

    @GetMapping("/quizzes")
    @Operation(summary = "Get all quiz templates")
    public ResponseEntity<List<QuizTemplateResponseDTO>> getAllQuizTemplates() {
        return ResponseEntity.ok(quizTemplateService.getAllQuizTemplates().stream()
            .map(quiz -> quizTemplateService.getQuizTemplateResponseDTO(quiz.getPhysicalId()))
            .collect(java.util.stream.Collectors.toList()));
    }
} 