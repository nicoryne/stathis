package edu.cit.stathis.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cit.stathis.task.repository.QuizTemplateRepository;
import edu.cit.stathis.task.entity.QuizTemplate;
import edu.cit.stathis.task.dto.QuizTemplateBodyDTO;
import edu.cit.stathis.task.dto.QuizTemplateResponseDTO;
import java.util.List;

@Service
public class QuizTemplateService {
    @Autowired
    private QuizTemplateRepository quizTemplateRepository;

    public QuizTemplate createQuizTemplate(QuizTemplateBodyDTO quizTemplateBodyDTO) {
        QuizTemplate quizTemplate = new QuizTemplate();
        quizTemplate.setTitle(quizTemplateBodyDTO.getTitle());
        quizTemplate.setInstruction(quizTemplateBodyDTO.getInstruction());
        quizTemplate.setMaxScore(quizTemplateBodyDTO.getMaxScore());
        quizTemplate.setContent(quizTemplateBodyDTO.getContent());
        return quizTemplateRepository.save(quizTemplate);
    }

    public QuizTemplate getQuizTemplate(String physicalId) {
        return quizTemplateRepository.findByPhysicalId(physicalId).orElse(null);
    }

    public List<QuizTemplate> getAllQuizTemplates() {
        return quizTemplateRepository.findAll();
    }

    public QuizTemplateResponseDTO getQuizTemplateResponseDTO(String physicalId) {
        QuizTemplate quizTemplate = getQuizTemplate(physicalId);
        return QuizTemplateResponseDTO.builder()
            .physicalId(quizTemplate.getPhysicalId())
            .title(quizTemplate.getTitle())
            .instruction(quizTemplate.getInstruction())
            .maxScore(quizTemplate.getMaxScore())
            .content(quizTemplate.getContent())
            .build();
    }
}
