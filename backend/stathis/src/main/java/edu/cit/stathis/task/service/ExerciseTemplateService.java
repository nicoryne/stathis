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

@Service
public class ExerciseTemplateService {
    @Autowired
    private ExerciseTemplateRepository exerciseTemplateRepository;

    public ExerciseTemplate createExerciseTemplate(ExerciseTemplateBodyDTO exerciseTemplateBodyDTO) {
        ExerciseTemplate exerciseTemplate = new ExerciseTemplate();
        exerciseTemplate.setTitle(exerciseTemplateBodyDTO.getTitle());
        exerciseTemplate.setDescription(exerciseTemplateBodyDTO.getDescription());
        exerciseTemplate.setExerciseType(ExerciseType.valueOf(exerciseTemplateBodyDTO.getExerciseType()));
        exerciseTemplate.setExerciseDifficulty(ExerciseDifficulty.valueOf(exerciseTemplateBodyDTO.getExerciseDifficulty()));
        exerciseTemplate.setGoalReps(Integer.parseInt(exerciseTemplateBodyDTO.getGoalReps()));
        exerciseTemplate.setGoalAccuracy(Integer.parseInt(exerciseTemplateBodyDTO.getGoalAccuracy()));
        exerciseTemplate.setGoalTime(Integer.parseInt(exerciseTemplateBodyDTO.getGoalTime()));
        return exerciseTemplateRepository.save(exerciseTemplate);
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
