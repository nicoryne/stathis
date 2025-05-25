package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;

import edu.cit.stathis.task.service.LessonTemplateService;
import edu.cit.stathis.task.dto.LessonTemplateBodyDTO;
import edu.cit.stathis.task.dto.LessonTemplateResponseDTO;
import edu.cit.stathis.task.entity.LessonTemplate;

@RestController
@RequestMapping("/api/lesson-templates")
public class LessonTemplateController {
    @Autowired
    private LessonTemplateService lessonTemplateService;

    @PostMapping
    @Operation(summary = "Create a new lesson template", description = "Create a new lesson template")
    public ResponseEntity<LessonTemplateResponseDTO> createLessonTemplate(@RequestBody LessonTemplateBodyDTO lessonTemplateBodyDTO) {
        LessonTemplate lessonTemplate = lessonTemplateService.createLessonTemplate(lessonTemplateBodyDTO);
        return ResponseEntity.ok(lessonTemplateService.getLessonTemplateResponseDTO(lessonTemplate.getPhysicalId()));
    }

    @GetMapping("/{physicalId}")
    @Operation(summary = "Get a lesson template by its physical ID", description = "Get a lesson template by its physical ID")
    public ResponseEntity<LessonTemplateResponseDTO> getLessonTemplate(@PathVariable String physicalId) {
        LessonTemplate lessonTemplate = lessonTemplateService.getLessonTemplate(physicalId);
        return ResponseEntity.ok(lessonTemplateService.getLessonTemplateResponseDTO(lessonTemplate.getPhysicalId()));
    }
}
