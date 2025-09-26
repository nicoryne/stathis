package edu.cit.stathis.vitals.controller;

import edu.cit.stathis.vitals.dto.VitalSignsDTO;
import edu.cit.stathis.vitals.service.VitalSignsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import io.swagger.v3.oas.annotations.Operation;

@Controller
public class VitalSignsController {

    @Autowired
    private VitalSignsService vitalSignsService;

    @Operation(summary = "Send vital signs", description = "Send vital signs for classroom broadcasting only")
    @MessageMapping("/vitals/send")
    public void handleVitalSigns(VitalSignsDTO vitalSignsDTO) {
        // The service will publish to /topic/classroom/{classroomId}/vitals explicitly
        vitalSignsService.processVitalSigns(vitalSignsDTO);
    }
} 