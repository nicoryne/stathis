package edu.cit.stathis.posture.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.stathis.posture.dto.ClassificationResult;
import java.io.InputStream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires ONNX native runtime on the host; enable locally when available")
public class PostureModelServiceClassifyIntegrationTest {

  @Test
  public void testClassifyWithSyntheticWindow() throws Exception {
    PostureModelService postureService = new PostureModelService();
    // initialize the service manually
    postureService.init();

    int timeSteps = readTimeSteps();
    float[][][] window = new float[1][timeSteps][132];

    for (int t = 0; t < timeSteps; t++) {
      for (int i = 0; i < 33; i++) {
        int base = i * 4;
        window[0][t][base] = 0.0f;
        window[0][t][base + 1] = 0.0f;
        window[0][t][base + 2] = 0.0f;
        window[0][t][base + 3] = 0.9f;
      }
    }

    ClassificationResult result = postureService.classify(window);
    assertNotNull(result);
    assertNotNull(result.getPredictedClass());
    assertNotNull(result.getProbabilities());
    assertNotNull(result.getClassNames());
    assertEquals(result.getClassNames().size(), result.getProbabilities().length);
    assertTrue(result.getScore() >= 0.0f && result.getScore() <= 1.0f);

    postureService.close();
  }

  private int readTimeSteps() throws Exception {
    ClassPathResource res = new ClassPathResource("models/model_config.json");
    try (InputStream in = res.getInputStream()) {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode cfg = mapper.readTree(in);
      return cfg.has("time_steps") ? cfg.get("time_steps").asInt() : 30;
    }
  }
}


