package edu.cit.stathis.posture.controller;

import edu.cit.stathis.posture.dto.LandmarkRequest;
import edu.cit.stathis.posture.dto.PostureResponse;
import edu.cit.stathis.posture.service.PostureModelService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posture")
@Tag(name = "Posture", description = "Endpoints related to posture analysis")
public class PostureController {

  private final PostureModelService postureService;

  public PostureController(PostureModelService postureService) {
    this.postureService = postureService;
  }

  @PostMapping("/analyze")
  public ResponseEntity<?> analyzePosture(@RequestBody LandmarkRequest request) {
    try {
      if (request.getLandmarks() == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "Landmarks data is missing."));
      }
      PostureResponse response = postureService.predict(request.getLandmarks());
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Error processing posture: " + e.getMessage()));
    }
  }
}
