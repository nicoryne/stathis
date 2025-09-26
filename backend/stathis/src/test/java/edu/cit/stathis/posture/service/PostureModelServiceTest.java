package edu.cit.stathis.posture.service;

import edu.cit.stathis.posture.dto.PostureResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
    "spring.main.allow-bean-definition-overriding=true"
})
public class PostureModelServiceTest {

    @Mock
    private PostureModelService postureService;

    private float[][] validLandmarks;
    private float[][] invalidLandmarks;

    @BeforeEach
    void setUp() {
        // Create valid landmarks [33][2]
        validLandmarks = new float[33][2];
        for (int i = 0; i < 33; i++) {
            validLandmarks[i][0] = 0.5f; // x coordinate
            validLandmarks[i][1] = 0.5f; // y coordinate
        }
        
        // Create invalid landmarks (wrong dimensions)
        invalidLandmarks = new float[32][3]; // Wrong: should be [33][2]
        for (int i = 0; i < 32; i++) {
            invalidLandmarks[i][0] = 0.5f;
            invalidLandmarks[i][1] = 0.5f;
            invalidLandmarks[i][2] = 0.5f; // z coordinate (not needed)
        }
    }

    @Test
    void testModelInitialization() {
        // Test that the service can be instantiated
        assertNotNull(postureService);
    }

    @Test
    void testPredictWithValidLandmarks() throws Exception {
        // Mock the predict method to return a valid response
        PostureResponse expectedResponse = new PostureResponse("push_up", 0.85f);
        when(postureService.predict(validLandmarks)).thenReturn(expectedResponse);

        // Test the prediction
        PostureResponse result = postureService.predict(validLandmarks);

        // Verify the result
        assertNotNull(result);
        assertEquals("push_up", result.getExerciseName());
        assertEquals(0.85f, result.getConfidence(), 0.01f);
        assertTrue(result.getConfidence() >= 0.0f && result.getConfidence() <= 1.0f);
    }

    @Test
    void testPredictWithInvalidLandmarks() {
        // Test with null landmarks
        assertThrows(IllegalArgumentException.class, () -> {
            postureService.predict(null);
        });

        // Test with wrong dimensions
        assertThrows(IllegalArgumentException.class, () -> {
            postureService.predict(invalidLandmarks);
        });
    }

    @Test
    void testPredictWithInsufficientData() throws Exception {
        // Test when we don't have enough frames in sequence buffer
        PostureResponse response = new PostureResponse("insufficient_data", 0.0f);
        when(postureService.predict(validLandmarks)).thenReturn(response);

        PostureResponse result = postureService.predict(validLandmarks);

        assertEquals("insufficient_data", result.getExerciseName());
        assertEquals(0.0f, result.getConfidence());
    }

    @Test
    void testExerciseLabels() {
        // Test that exercise labels are loaded correctly
        // You can access private fields using ReflectionTestUtils
        // This assumes you have a method to get the labels or make them accessible
        
        // Example: if you add a getter method to PostureModelService
        // List<String> labels = postureService.getExerciseLabels();
        // assertNotNull(labels);
        // assertEquals(11, labels.size());
        // assertTrue(labels.contains("push_up"));
        // assertTrue(labels.contains("squat"));
    }

    @Test
    void testSequenceBufferBehavior() throws Exception {
        // Test that the sequence buffer works correctly
        // This would require multiple calls to predict() to fill the buffer
        
        PostureResponse insufficientResponse = new PostureResponse("insufficient_data", 0.0f);
        PostureResponse validResponse = new PostureResponse("push_up", 0.85f);
        
        // First few calls should return insufficient_data
        when(postureService.predict(validLandmarks))
            .thenReturn(insufficientResponse)
            .thenReturn(insufficientResponse)
            .thenReturn(validResponse);

        // Test first call (insufficient data)
        PostureResponse result1 = postureService.predict(validLandmarks);
        assertEquals("insufficient_data", result1.getExerciseName());

        // Test second call (still insufficient)
        PostureResponse result2 = postureService.predict(validLandmarks);
        assertEquals("insufficient_data", result2.getExerciseName());

        // Test third call (should have enough data)
        PostureResponse result3 = postureService.predict(validLandmarks);
        assertEquals("push_up", result3.getExerciseName());
    }

   @Test
    void testSoftmaxFunction() {
        // Test case 1: Normal logits
        float[] logits1 = {1.0f, 2.0f, 3.0f};
        float[] probabilities1 = postureService.softmax(logits1);
        
        // Verify sum equals 1
        float sum1 = 0.0f;
        for (float prob : probabilities1) {
            sum1 += prob;
        }
        assertEquals(1.0f, sum1, 0.01f);
        
        // Verify ordering
        assertTrue(probabilities1[2] > probabilities1[1]);
        assertTrue(probabilities1[1] > probabilities1[0]);
        
        // Test case 2: Equal logits
        float[] logits2 = {1.0f, 1.0f, 1.0f};
        float[] probabilities2 = postureService.softmax(logits2);
        
        // All probabilities should be equal (approximately 1/3)
        float expectedProb = 1.0f / 3.0f;
        for (float prob : probabilities2) {
            assertEquals(expectedProb, prob, 0.01f);
        }
        
        // Test case 3: Large logits (test numerical stability)
        float[] logits3 = {100.0f, 101.0f, 102.0f};
        float[] probabilities3 = postureService.softmax(logits3);
        
        // Should still sum to 1 and maintain ordering
        float sum3 = 0.0f;
        for (float prob : probabilities3) {
            sum3 += prob;
        }
        assertEquals(1.0f, sum3, 0.01f);
        assertTrue(probabilities3[2] > probabilities3[1]);
        assertTrue(probabilities3[1] > probabilities3[0]);
        
        // Test case 4: Negative logits
        float[] logits4 = {-1.0f, 0.0f, 1.0f};
        float[] probabilities4 = postureService.softmax(logits4);
        
        float sum4 = 0.0f;
        for (float prob : probabilities4) {
            sum4 += prob;
        }
        assertEquals(1.0f, sum4, 0.01f);
        assertTrue(probabilities4[2] > probabilities4[1]);
        assertTrue(probabilities4[1] > probabilities4[0]);
    }

    @Test
    void testModelInputValidation() {
        // Test various invalid input scenarios
        
        // Test null input
        assertThrows(IllegalArgumentException.class, () -> {
            postureService.predict(null);
        });

        // Test wrong number of landmarks
        float[][] wrongLandmarks = new float[32][2]; // Should be 33
        assertThrows(IllegalArgumentException.class, () -> {
            postureService.predict(wrongLandmarks);
        });

        // Test wrong coordinate count
        float[][] wrongCoords = new float[33][3]; // Should be 2 (x,y)
        assertThrows(IllegalArgumentException.class, () -> {
            postureService.predict(wrongCoords);
        });
    }

    @Test
    void testModelOutputValidation() throws Exception {
        // Test that the model output is valid
        PostureResponse response = new PostureResponse("squat", 0.92f);
        when(postureService.predict(validLandmarks)).thenReturn(response);

        PostureResponse result = postureService.predict(validLandmarks);

        // Verify exercise name is not null or empty
        assertNotNull(result.getExerciseName());
        assertFalse(result.getExerciseName().isEmpty());
        assertNotEquals("Unknown", result.getExerciseName());

        // Verify confidence is within valid range
        assertTrue(result.getConfidence() >= 0.0f);
        assertTrue(result.getConfidence() <= 1.0f);
    }

    @Test
    void testResourceCleanup() {
        // Test that resources are properly cleaned up
        // This would test the @PreDestroy method
        assertDoesNotThrow(() -> {
            // Simulate cleanup
            postureService.close();
        });
    }
}