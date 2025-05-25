package edu.cit.stathis.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cit.stathis.task.repository.LessonTemplateRepository;
import edu.cit.stathis.task.entity.LessonTemplate;
import edu.cit.stathis.task.dto.LessonTemplateBodyDTO;
import edu.cit.stathis.task.dto.LessonTemplateResponseDTO;
import java.util.List;
import java.time.OffsetDateTime;
import java.util.Random;

@Service
public class LessonTemplateService {
    @Autowired
    private LessonTemplateRepository lessonTemplateRepository;

    public LessonTemplate createLessonTemplate(LessonTemplateBodyDTO lessonTemplateBodyDTO) {
        LessonTemplate lessonTemplate = new LessonTemplate();
        lessonTemplate.setPhysicalId(generatePhysicalId());
        lessonTemplate.setTitle(lessonTemplateBodyDTO.getTitle());
        lessonTemplate.setDescription(lessonTemplateBodyDTO.getDescription());
        lessonTemplate.setContent(lessonTemplateBodyDTO.getContent());
        return lessonTemplateRepository.save(lessonTemplate);
    }

    private String generatePhysicalId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%04d", random.nextInt(10000));
        String thirdPart = String.format("%03d", random.nextInt(1000));
        return String.format("LESSON-%s-%s-%s", year, secondPart, thirdPart);
    }

    public LessonTemplate getLessonTemplate(String physicalId) {
        return lessonTemplateRepository.findByPhysicalId(physicalId).orElse(null);
    }

    public List<LessonTemplate> getAllLessonTemplates() {
        return lessonTemplateRepository.findAll();
    }

    public LessonTemplateResponseDTO getLessonTemplateResponseDTO(String physicalId) {
        LessonTemplate lessonTemplate = getLessonTemplate(physicalId);
        if (lessonTemplate == null) {
            return null;
        }
        return LessonTemplateResponseDTO.builder()
            .physicalId(lessonTemplate.getPhysicalId())
            .title(lessonTemplate.getTitle())
            .description(lessonTemplate.getDescription())
            .content(lessonTemplate.getContent())
            .build();
    }
}
