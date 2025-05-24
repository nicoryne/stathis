package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import edu.cit.stathis.task.service.ExerciseTemplateService;
import edu.cit.stathis.task.dto.ExerciseTemplateBodyDTO;
import edu.cit.stathis.task.dto.ExerciseTemplateResponseDTO;
import edu.cit.stathis.task.entity.ExerciseTemplate;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/exercise-templates")
public class ExerciseTemplateController {
    @Autowired
    private ExerciseTemplateService exerciseTemplateService;

    @PostMapping
    @Operation(summary = "Create a new exercise template", description = "Create a new exercise template")
    public ResponseEntity<ExerciseTemplateResponseDTO> createExerciseTemplate(@RequestBody ExerciseTemplateBodyDTO exerciseTemplateBodyDTO) {
        ExerciseTemplate exerciseTemplate = exerciseTemplateService.createExerciseTemplate(exerciseTemplateBodyDTO);
        return ResponseEntity.ok(exerciseTemplateService.getExerciseTemplateResponseDTO(exerciseTemplate.getPhysicalId()));
    }

    @GetMapping("/{physicalId}")
    @Operation(summary = "Get an exercise template by its physical ID", description = "Get an exercise template by its physical ID")
    public ResponseEntity<ExerciseTemplateResponseDTO> getExerciseTemplate(@PathVariable String physicalId) {
        ExerciseTemplate exerciseTemplate = exerciseTemplateService.getExerciseTemplate(physicalId);
        return ResponseEntity.ok(exerciseTemplateService.getExerciseTemplateResponseDTO(exerciseTemplate.getPhysicalId()));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all exercise templates", description = "Get all exercise templates")
    public ResponseEntity<List<ExerciseTemplateResponseDTO>> getAllExerciseTemplates() {
        List<ExerciseTemplate> exerciseTemplates = exerciseTemplateService.getAllExerciseTemplates();
        return ResponseEntity.ok(exerciseTemplates.stream()
            .map(exerciseTemplate -> exerciseTemplateService.getExerciseTemplateResponseDTO(exerciseTemplate.getPhysicalId()))
            .collect(Collectors.toList()));
    }
}
