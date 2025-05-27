package edu.cit.stathis.vitals.service;

import edu.cit.stathis.auth.entity.UserProfile;
import edu.cit.stathis.auth.repository.UserProfileRepository;
import edu.cit.stathis.vitals.dto.HeartRateAlertDTO;
import edu.cit.stathis.vitals.dto.VitalSignsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class HeartRateMonitorService {

    private static final double MAX_HEART_RATE_THRESHOLD = 0.85; // 85% of max heart rate

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void checkHeartRate(VitalSignsDTO vitalSignsDTO) {
        UserProfile userProfile = userProfileRepository.findByUser_PhysicalId(vitalSignsDTO.getStudentId())
                .orElse(null);

        if (userProfile == null || userProfile.getAge() == null) {
            return;
        }

        // Calculate max heart rate using Karvonen formula
        int maxHeartRate = 220 - userProfile.getAge();
        int thresholdHeartRate = (int) (maxHeartRate * MAX_HEART_RATE_THRESHOLD);

        // Check if current heart rate exceeds threshold
        if (vitalSignsDTO.getHeartRate() > thresholdHeartRate) {
            // Create alert message
            String alertMessage = String.format(
                "ALERT: Student %s %s's heart rate (%d bpm) exceeds safety threshold (%d bpm)",
                userProfile.getFirstName(),
                userProfile.getLastName(),
                vitalSignsDTO.getHeartRate(),
                thresholdHeartRate
            );

            // Send alert to teacher's dashboard
            String destination = "/topic/classroom/" + vitalSignsDTO.getClassroomId() + "/alerts";
            messagingTemplate.convertAndSend(destination, new HeartRateAlertDTO(
                vitalSignsDTO.getStudentId(),
                userProfile.getFirstName() + " " + userProfile.getLastName(),
                vitalSignsDTO.getHeartRate(),
                thresholdHeartRate,
                alertMessage,
                vitalSignsDTO.getTimestamp()
            ));
        }
    }
} 