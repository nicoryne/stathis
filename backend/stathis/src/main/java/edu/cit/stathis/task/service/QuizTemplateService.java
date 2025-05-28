package edu.cit.stathis.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.stathis.auth.service.PhysicalIdService;
import edu.cit.stathis.task.repository.QuizTemplateRepository;
import edu.cit.stathis.task.entity.QuizTemplate;
import edu.cit.stathis.task.dto.QuizTemplateBodyDTO;
import edu.cit.stathis.task.dto.QuizTemplateResponseDTO;
import java.util.List;
import java.time.OffsetDateTime;
import java.util.Random;

@Service
public class QuizTemplateService {
    @Autowired
    private QuizTemplateRepository quizTemplateRepository;

    @Autowired
    private PhysicalIdService physicalIdService;

    public QuizTemplate createQuizTemplate(QuizTemplateBodyDTO quizTemplateBodyDTO) {
        QuizTemplate quizTemplate = new QuizTemplate();
        quizTemplate.setPhysicalId(generatePhysicalId());
        quizTemplate.setTeacherPhysicalId(physicalIdService.getCurrentUserPhysicalId());
        quizTemplate.setTitle(quizTemplateBodyDTO.getTitle());
        quizTemplate.setInstruction(quizTemplateBodyDTO.getInstruction());
        quizTemplate.setMaxScore(quizTemplateBodyDTO.getMaxScore());
        quizTemplate.setContent(quizTemplateBodyDTO.getContent());
        return quizTemplateRepository.save(quizTemplate);
    }

    private String generatePhysicalId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%04d", random.nextInt(10000));
        String thirdPart = String.format("%03d", random.nextInt(1000));
        return String.format("QUIZ-%s-%s-%s", year, secondPart, thirdPart);
    }

    public QuizTemplate getQuizTemplate(String physicalId) {
        return quizTemplateRepository.findByPhysicalId(physicalId).orElse(null);
    }

    public List<QuizTemplate> getAllQuizTemplates() {
        return quizTemplateRepository.findAll();
    }

    public List<QuizTemplate> getAllQuizTemplatesByTeacherPhysicalId(String teacherPhysicalId) {
        return quizTemplateRepository.findByTeacherPhysicalId(teacherPhysicalId);
    }

    public QuizTemplate updateQuizTemplate(String physicalId, QuizTemplateBodyDTO quizTemplateBodyDTO) {
        QuizTemplate quizTemplate = getQuizTemplate(physicalId);
        if (quizTemplate == null) {
            throw new RuntimeException("Quiz template not found");
        }

        if (!quizTemplate.getTeacherPhysicalId().equals(physicalIdService.getCurrentUserPhysicalId())) {
            throw new RuntimeException("You are not the teacher of this quiz template");
        }
        quizTemplate.setTitle(quizTemplateBodyDTO.getTitle());
        quizTemplate.setInstruction(quizTemplateBodyDTO.getInstruction());
        quizTemplate.setMaxScore(quizTemplateBodyDTO.getMaxScore());
        quizTemplate.setContent(quizTemplateBodyDTO.getContent());
        return quizTemplateRepository.save(quizTemplate);
    }

    @Transactional
    public void deleteQuizTemplate(String physicalId) {
        QuizTemplate quizTemplate = getQuizTemplate(physicalId);
        if (quizTemplate == null) {
            throw new RuntimeException("Quiz template not found");
        }

        if (!quizTemplate.getTeacherPhysicalId().equals(physicalIdService.getCurrentUserPhysicalId())) {
            throw new RuntimeException("You are not the teacher of this quiz template");
        }
        quizTemplateRepository.deleteByPhysicalId(physicalId);
    }

    public QuizTemplateResponseDTO getQuizTemplateResponseDTO(String physicalId) {
        QuizTemplate quizTemplate = getQuizTemplate(physicalId);
        if (quizTemplate == null) {
            return null;
        }
        return QuizTemplateResponseDTO.builder()
            .physicalId(quizTemplate.getPhysicalId())
            .title(quizTemplate.getTitle())
            .instruction(quizTemplate.getInstruction())
            .maxScore(quizTemplate.getMaxScore())
            .content(quizTemplate.getContent())
            .build();
    }
}
