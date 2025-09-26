package edu.cit.stathis.vitals.controller;

import edu.cit.stathis.vitals.dto.VitalSignsDTO;
import edu.cit.stathis.vitals.entity.VitalSigns;
import edu.cit.stathis.vitals.service.VitalSignsService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vitals")
public class VitalSignsRestController {

    @Autowired
    private VitalSignsService vitalSignsService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Ingest vital signs via REST", description = "Accepts vital signs payload and processes it like WebSocket")
    public ResponseEntity<Void> ingestVitalSigns(@RequestBody VitalSignsDTO vitalSignsDTO) {
        vitalSignsService.processVitalSigns(vitalSignsDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/classroom/{classroomId}/task/{taskId}")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    @Operation(summary = "Get vital signs for classroom and task")
    public ResponseEntity<List<VitalSigns>> getByClassroomAndTask(
            @PathVariable String classroomId,
            @PathVariable String taskId) {
        return ResponseEntity.ok(vitalSignsService.getVitalSignsByClassroomAndTask(classroomId, taskId));
    }

    @GetMapping("/student/{studentId}/task/{taskId}")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    @Operation(summary = "Get vital signs for student and task")
    public ResponseEntity<List<VitalSigns>> getByStudentAndTask(
            @PathVariable String studentId,
            @PathVariable String taskId) {
        return ResponseEntity.ok(vitalSignsService.getVitalSignsByStudentAndTask(studentId, taskId));
    }

    @GetMapping("/student/{studentId}/task/{taskId}/pre")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    @Operation(summary = "Get pre-activity vital signs")
    public ResponseEntity<List<VitalSigns>> getPreActivity(
            @PathVariable String studentId,
            @PathVariable String taskId) {
        return ResponseEntity.ok(vitalSignsService.getPreActivityVitalSigns(studentId, taskId));
    }

    @GetMapping("/student/{studentId}/task/{taskId}/post")
    @PreAuthorize("hasAnyRole('TEACHER','STUDENT')")
    @Operation(summary = "Get post-activity vital signs")
    public ResponseEntity<List<VitalSigns>> getPostActivity(
            @PathVariable String studentId,
            @PathVariable String taskId) {
        return ResponseEntity.ok(vitalSignsService.getPostActivityVitalSigns(studentId, taskId));
    }
}


