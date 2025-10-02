package edu.cit.stathis.posture.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PostureRulesServiceTest {

  private PostureRulesService rulesService;
  private float[][] frame;

  @BeforeEach
  void setup() {
    rulesService = new PostureRulesService();
    frame = new float[33][4];
    for (int i = 0; i < 33; i++) {
      frame[i][0] = 0f;
      frame[i][1] = 0f;
      frame[i][2] = 0f;
      frame[i][3] = 0.9f;
    }
  }

  @Test
  void squatDepthLowFlagged() {
    frame[23][1] = 0.0f; // L hip
    frame[24][1] = 0.0f; // R hip
    frame[25][1] = 0.1f; // L knee slightly flexed
    frame[26][1] = 0.1f; // R knee slightly flexed
    frame[27][1] = 0.2f; // L ankle
    frame[28][1] = 0.2f; // R ankle

    PostureRulesService.RulesResult res = rulesService.evaluate("squat", frame);
    assertTrue(res.flags.contains("depth_low"));
  }

  @Test
  void pushUpSagOrPikeDetected() {
    frame[11][1] = -0.1f; // L shoulder
    frame[12][1] = -0.1f; // R shoulder
    frame[23][1] = 0.2f; // hips below line -> sag
    frame[24][1] = 0.2f;
    frame[27][1] = 0.0f; // ankles
    frame[28][1] = 0.0f;

    PostureRulesService.RulesResult res = rulesService.evaluate("push_up", frame);
    assertTrue(res.flags.contains("sag") || res.flags.contains("pike"));
  }
}


