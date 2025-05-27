package edu.cit.stathis.task.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import edu.cit.stathis.task.repository.ExerciseTemplateRepository;
import edu.cit.stathis.task.entity.ExerciseTemplate;
import edu.cit.stathis.task.dto.ExerciseTemplateBodyDTO;
import edu.cit.stathis.task.dto.ExerciseTemplateResponseDTO;
import edu.cit.stathis.task.enums.ExerciseType;
import edu.cit.stathis.task.enums.ExerciseDifficulty;
import java.util.List;
import java.time.OffsetDateTime;
import java.util.Random;

@Service
public class ExerciseTemplateService {
    @Autowired
    private ExerciseTemplateRepository exerciseTemplateRepository;

    public ExerciseTemplate createExerciseTemplate(ExerciseTemplateBodyDTO exerciseTemplateBodyDTO) {
        ExerciseTemplate exerciseTemplate = new ExerciseTemplate();
        exerciseTemplate.setPhysicalId(generatePhysicalId());
        exerciseTemplate.setTitle(exerciseTemplateBodyDTO.getTitle());
        exerciseTemplate.setDescription(exerciseTemplateBodyDTO.getDescription());
        exerciseTemplate.setExerciseType(ExerciseType.valueOf(exerciseTemplateBodyDTO.getExerciseType()));
        exerciseTemplate.setExerciseDifficulty(ExerciseDifficulty.valueOf(exerciseTemplateBodyDTO.getExerciseDifficulty()));
        exerciseTemplate.setGoalReps(Integer.parseInt(exerciseTemplateBodyDTO.getGoalReps()));
        exerciseTemplate.setGoalAccuracy(Integer.parseInt(exerciseTemplateBodyDTO.getGoalAccuracy()));
        exerciseTemplate.setGoalTime(Integer.parseInt(exerciseTemplateBodyDTO.getGoalTime()));
        return exerciseTemplateRepository.save(exerciseTemplate);
    }

    private String generatePhysicalId() {
        String year = String.valueOf(OffsetDateTime.now().getYear()).substring(2);
        Random random = new Random();
        String secondPart = String.format("%04d", random.nextInt(10000));
        String thirdPart = String.format("%03d", random.nextInt(1000));
        return String.format("EXERCISE-%s-%s-%s", year, secondPart, thirdPart);
    }

    public ExerciseTemplate getExerciseTemplate(String physicalId) {
        return exerciseTemplateRepository.findByPhysicalId(physicalId)
            .orElseThrow(() -> new RuntimeException("Exercise template not found"));
    }

    public List<ExerciseTemplate> getAllExerciseTemplates() {
        return exerciseTemplateRepository.findAll();
    }

    public ExerciseTemplateResponseDTO getExerciseTemplateResponseDTO(String physicalId) {
        ExerciseTemplate exerciseTemplate = getExerciseTemplate(physicalId);
        if (exerciseTemplate == null) {
            return null;
        }
        return ExerciseTemplateResponseDTO.builder()
            .physicalId(exerciseTemplate.getPhysicalId())
            .title(exerciseTemplate.getTitle())
            .description(exerciseTemplate.getDescription())
            .exerciseType(exerciseTemplate.getExerciseType())
            .exerciseDifficulty(exerciseTemplate.getExerciseDifficulty())
            .goalReps(exerciseTemplate.getGoalReps())
            .goalAccuracy(exerciseTemplate.getGoalAccuracy())
            .goalTime(exerciseTemplate.getGoalTime())
            .build();
    }
}
