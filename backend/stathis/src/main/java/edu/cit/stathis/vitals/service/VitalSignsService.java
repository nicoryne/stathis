package edu.cit.stathis.vitals.service;

import edu.cit.stathis.vitals.dto.VitalSignsDTO;
import edu.cit.stathis.vitals.entity.VitalSigns;
import edu.cit.stathis.vitals.repository.VitalSignsRepository;
import edu.cit.stathis.task.entity.Task;
import edu.cit.stathis.task.repository.TaskRepository;
import edu.cit.stathis.auth.service.PhysicalIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VitalSignsService {

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private HeartRateMonitorService heartRateMonitorService;

    @Autowired
    private PhysicalIdService physicalIdService;

    @Transactional
    public void processVitalSigns(VitalSignsDTO vitalSignsDTO) {
        // Check if task is started
        Optional<Task> taskOptional = taskRepository.findByPhysicalId(vitalSignsDTO.getTaskId());
        Task task = taskOptional.orElse(null);
        if (task == null || !task.isStarted()) {
            return; // Skip processing if task is not started
        }

        // Get current student's physical ID
        String studentId = physicalIdService.getCurrentUserPhysicalId();

        // Convert DTO to entity
        VitalSigns vitalSigns = new VitalSigns();
        vitalSigns.setStudentId(studentId);
        vitalSigns.setClassroomId(vitalSignsDTO.getClassroomId());
        vitalSigns.setTaskId(vitalSignsDTO.getTaskId());
        vitalSigns.setHeartRate(vitalSignsDTO.getHeartRate());
        vitalSigns.setOxygenSaturation(vitalSignsDTO.getOxygenSaturation());
        vitalSigns.setTimestamp(vitalSignsDTO.getTimestamp());
        vitalSigns.setIsPreActivity(vitalSignsDTO.getIsPreActivity());
        vitalSigns.setIsPostActivity(vitalSignsDTO.getIsPostActivity());

        // Save to database
        vitalSigns = vitalSignsRepository.save(vitalSigns);
        vitalSignsDTO.setPhysicalId(vitalSigns.getPhysicalId());
        vitalSignsDTO.setStudentId(studentId);

        // Check heart rate and send alerts if necessary
        heartRateMonitorService.checkHeartRate(vitalSignsDTO);

        // Broadcast to WebSocket subscribers
        String destination = "/topic/classroom/" + vitalSignsDTO.getClassroomId() + "/vitals";
        messagingTemplate.convertAndSend(destination, vitalSignsDTO);
    }

    public List<VitalSigns> getVitalSignsByClassroomAndTask(String classroomId, String taskId) {
        return vitalSignsRepository.findByClassroomIdAndTaskId(classroomId, taskId);
    }

    public List<VitalSigns> getVitalSignsByStudentAndTask(String studentId, String taskId) {
        return vitalSignsRepository.findByStudentIdAndTaskId(studentId, taskId);
    }

    public List<VitalSigns> getPreActivityVitalSigns(String studentId, String taskId) {
        return vitalSignsRepository.findByStudentIdAndTaskIdAndIsPreActivity(studentId, taskId, true);
    }

    public List<VitalSigns> getPostActivityVitalSigns(String studentId, String taskId) {
        return vitalSignsRepository.findByStudentIdAndTaskIdAndIsPostActivity(studentId, taskId, true);
    }
} 