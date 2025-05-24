package edu.cit.stathis.vitals.service;

import edu.cit.stathis.vitals.dto.VitalSignsDTO;
import edu.cit.stathis.vitals.entity.VitalSigns;
import edu.cit.stathis.vitals.repository.VitalSignsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VitalSignsService {

    @Autowired
    private VitalSignsRepository vitalSignsRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private HeartRateMonitorService heartRateMonitorService;

    @Transactional
    public void processVitalSigns(VitalSignsDTO vitalSignsDTO) {
        // Convert DTO to entity
        VitalSigns vitalSigns = new VitalSigns();
        vitalSigns.setStudentId(vitalSignsDTO.getStudentId());
        vitalSigns.setClassroomId(vitalSignsDTO.getClassroomId());
        vitalSigns.setTaskId(vitalSignsDTO.getTaskId());
        vitalSigns.setHeartRate(vitalSignsDTO.getHeartRate());
        vitalSigns.setOxygenSaturation(vitalSignsDTO.getOxygenSaturation());
        vitalSigns.setTimestamp(vitalSignsDTO.getTimestamp());
        vitalSigns.setIsPreActivity(vitalSignsDTO.getIsPreActivity());
        vitalSigns.setIsPostActivity(vitalSignsDTO.getIsPostActivity());

        // Save to database
        vitalSignsRepository.save(vitalSigns);

        // Check heart rate and send alerts if necessary
        heartRateMonitorService.checkHeartRate(vitalSignsDTO);

        // Broadcast to WebSocket subscribers
        String destination = "/topic/classroom/" + vitalSignsDTO.getClassroomId() + "/vitals";
        messagingTemplate.convertAndSend(destination, vitalSignsDTO);
    }

    public List<VitalSigns> getVitalSignsByClassroomAndTask(Long classroomId, Long taskId) {
        return vitalSignsRepository.findByClassroomIdAndTaskId(classroomId, taskId);
    }

    public List<VitalSigns> getVitalSignsByStudentAndTask(Long studentId, Long taskId) {
        return vitalSignsRepository.findByStudentIdAndTaskId(studentId, taskId);
    }

    public List<VitalSigns> getPreActivityVitalSigns(Long studentId, Long taskId) {
        return vitalSignsRepository.findByStudentIdAndTaskIdAndIsPreActivity(studentId, taskId, true);
    }

    public List<VitalSigns> getPostActivityVitalSigns(Long studentId, Long taskId) {
        return vitalSignsRepository.findByStudentIdAndTaskIdAndIsPostActivity(studentId, taskId, true);
    }
} 