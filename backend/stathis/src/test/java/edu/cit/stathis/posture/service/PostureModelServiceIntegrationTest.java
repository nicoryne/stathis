package edu.cit.stathis.posture.service;

import edu.cit.stathis.posture.dto.PostureResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
public class PostureModelServiceIntegrationTest {

    @Autowired
    private PostureModelService postureService;

    @Test
    public void testRealModelPrediction() throws Exception {
        // Create realistic pose landmarks (push-up position)
        float[][] landmarks = createPushUpLandmarks();
        
        // Make multiple calls to fill the sequence buffer
        PostureResponse response = null;
        for (int i = 0; i < 35; i++) { // More than 30 to ensure buffer is full
            response = postureService.predict(landmarks);
        }
        
        // Verify the response
        assertNotNull(response);
        assertNotNull(response.getExerciseName());
        assertTrue(response.getConfidence() >= 0.0f && response.getConfidence() <= 1.0f);
        
        System.out.println("Predicted exercise: " + response.getExerciseName());
        System.out.println("Confidence: " + response.getConfidence());
    }

    private float[][] createPushUpLandmarks() {
        float[][] landmarks = new float[33][2];
        
        // Nose
        landmarks[0][0] = 0.5f; landmarks[0][1] = 0.1f;
        // Left eye inner
        landmarks[1][0] = 0.48f; landmarks[1][1] = 0.12f;
        // Left eye
        landmarks[2][0] = 0.46f; landmarks[2][1] = 0.12f;
        // Left eye outer
        landmarks[3][0] = 0.44f; landmarks[3][1] = 0.13f;
        // Right eye inner
        landmarks[4][0] = 0.52f; landmarks[4][1] = 0.12f;
        // Right eye
        landmarks[5][0] = 0.54f; landmarks[5][1] = 0.12f;
        // Right eye outer
        landmarks[6][0] = 0.56f; landmarks[6][1] = 0.13f;
        // Left ear
        landmarks[7][0] = 0.42f; landmarks[7][1] = 0.15f;
        // Right ear
        landmarks[8][0] = 0.58f; landmarks[8][1] = 0.15f;
        // Mouth left
        landmarks[9][0] = 0.47f; landmarks[9][1] = 0.18f;
        // Mouth right
        landmarks[10][0] = 0.53f; landmarks[10][1] = 0.18f;
        
        // Shoulders (push-up position)
        landmarks[11][0] = 0.3f; landmarks[11][1] = 0.3f;  // Left shoulder
        landmarks[12][0] = 0.7f; landmarks[12][1] = 0.3f;  // Right shoulder
        
        // Elbows
        landmarks[13][0] = 0.2f; landmarks[13][1] = 0.5f;  // Left elbow
        landmarks[14][0] = 0.8f; landmarks[14][1] = 0.5f;  // Right elbow
        
        // Wrists
        landmarks[15][0] = 0.1f; landmarks[15][1] = 0.7f;   // Left wrist
        landmarks[16][0] = 0.9f; landmarks[16][1] = 0.7f;  // Right wrist
        
        // Fill remaining landmarks with default values
        for (int i = 17; i < 33; i++) {
            landmarks[i][0] = 0.5f;
            landmarks[i][1] = 0.5f;
        }
        
        return landmarks;
    }
}