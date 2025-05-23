package edu.cit.stathis.task.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import edu.cit.stathis.task.service.QuizTemplateService;
import edu.cit.stathis.task.dto.QuizTemplateBodyDTO;
import edu.cit.stathis.task.dto.QuizTemplateResponseDTO;
import edu.cit.stathis.task.entity.QuizTemplate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quiz-templates")
public class QuizTemplateController {
    @Autowired
    private QuizTemplateService quizTemplateService;

    @PostMapping
    public ResponseEntity<QuizTemplateResponseDTO> createQuizTemplate(@RequestBody QuizTemplateBodyDTO quizTemplateBodyDTO) {
        QuizTemplate quizTemplate = quizTemplateService.createQuizTemplate(quizTemplateBodyDTO);
        return ResponseEntity.ok(quizTemplateService.getQuizTemplateResponseDTO(quizTemplate.getPhysicalId()));
    }

    @GetMapping("/{physicalId}")
    public ResponseEntity<QuizTemplateResponseDTO> getQuizTemplate(@PathVariable String physicalId) {
        QuizTemplate quizTemplate = quizTemplateService.getQuizTemplate(physicalId);
        return ResponseEntity.ok(quizTemplateService.getQuizTemplateResponseDTO(quizTemplate.getPhysicalId()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<QuizTemplateResponseDTO>> getAllQuizTemplates() {
        List<QuizTemplate> quizTemplates = quizTemplateService.getAllQuizTemplates();
        return ResponseEntity.ok(quizTemplates.stream()
            .map(quizTemplate -> quizTemplateService.getQuizTemplateResponseDTO(quizTemplate.getPhysicalId()))
            .collect(Collectors.toList()));
    }
}
