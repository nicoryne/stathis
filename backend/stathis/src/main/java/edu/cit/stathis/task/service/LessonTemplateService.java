package edu.cit.stathis.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.cit.stathis.task.repository.LessonTemplateRepository;
import edu.cit.stathis.task.entity.LessonTemplate;
import edu.cit.stathis.task.dto.LessonTemplateBodyDTO;
import edu.cit.stathis.task.dto.LessonTemplateResponseDTO;
import java.util.List;

@Service
public class LessonTemplateService {
    @Autowired
    private LessonTemplateRepository lessonTemplateRepository;

    public LessonTemplate createLessonTemplate(LessonTemplateBodyDTO lessonTemplateBodyDTO) {
        LessonTemplate lessonTemplate = new LessonTemplate();
        lessonTemplate.setTitle(lessonTemplateBodyDTO.getTitle());
        lessonTemplate.setDescription(lessonTemplateBodyDTO.getDescription());
        lessonTemplate.setContent(lessonTemplateBodyDTO.getContent());
        return lessonTemplateRepository.save(lessonTemplate);
    }

    public LessonTemplate getLessonTemplate(String physicalId) {
        return lessonTemplateRepository.findByPhysicalId(physicalId);
    }

    public List<LessonTemplate> getAllLessonTemplates() {
        return lessonTemplateRepository.findAll();
    }

    public LessonTemplateResponseDTO getLessonTemplateResponseDTO(String physicalId) {
        LessonTemplate lessonTemplate = getLessonTemplate(physicalId);
        return LessonTemplateResponseDTO.builder()
            .physicalId(lessonTemplate.getPhysicalId())
            .title(lessonTemplate.getTitle())
            .description(lessonTemplate.getDescription())
            .content(lessonTemplate.getContent())
            .build();
    }
}
