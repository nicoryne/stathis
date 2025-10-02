package edu.cit.stathis.posture.controller;

import edu.cit.stathis.posture.dto.ClassificationRequest;
import edu.cit.stathis.posture.dto.ClassificationResult;
import edu.cit.stathis.posture.service.PostureModelService;
import edu.cit.stathis.posture.service.PostureRulesService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posture")
@Tag(name = "Posture", description = "Endpoints related to posture analysis")
public class PostureController {

  private final PostureModelService postureService;
  private final PostureRulesService rulesService;

  public PostureController(PostureModelService postureService, PostureRulesService rulesService) {
    this.postureService = postureService;
    this.rulesService = rulesService;
  }

  @PostMapping("/classify")
  public ResponseEntity<?> classify(@RequestBody ClassificationRequest request) {
    try {
      if (request.getWindow() == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "Window data is missing."));
      }

      ClassificationResult result = postureService.classify(request.getWindow());
      float[] last = request.getWindow()[0][request.getWindow()[0].length - 1];
      float[][] lastFrame = new float[33][4];
      for (int i = 0; i < 33; i++) {
        int base = i * 4;
        lastFrame[i][0] = last[base];
        lastFrame[i][1] = last[base + 1];
        lastFrame[i][2] = last[base + 2];
        lastFrame[i][3] = last[base + 3];
      }
      PostureRulesService.RulesResult rules = rulesService.evaluate(result.getPredictedClass(), lastFrame);
      result.setFlags(rules.flags);
      result.setMessages(rules.messages);
      return ResponseEntity.ok(result);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      e.printStackTrace();
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Error processing posture: " + e.getMessage()));
    }
  }
}
