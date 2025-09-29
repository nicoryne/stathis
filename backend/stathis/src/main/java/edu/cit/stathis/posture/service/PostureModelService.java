package edu.cit.stathis.posture.service;

import ai.onnxruntime.*;
import edu.cit.stathis.posture.dto.PostureResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class PostureModelService {
  private OrtEnvironment env;
  private OrtSession session;
  private List<String> exerciseLabels;
  private final String INPUT_NODE_NAME = "input"; 
  
  // Add sequence buffer
  private final int SEQUENCE_LENGTH = 30;
  private final int LANDMARK_COUNT = 33;
  private final int COORDINATE_COUNT = 2; // x,y only (no z)
  private List<float[]> sequenceBuffer = new ArrayList<>();
  
  @PostConstruct
  public void init() throws OrtException, IOException {
    env = OrtEnvironment.getEnvironment();
    
    InputStream modelStream = new ClassPathResource("models/model.onnx").getInputStream();
    byte[] modelBytes = modelStream.readAllBytes();
    modelStream.close();
    
    session = env.createSession(modelBytes, new OrtSession.SessionOptions());
    
    System.out.println("ONNX Model Input Info: " + session.getInputInfo());
    System.out.println("ONNX Model Output Info: " + session.getOutputInfo());
    
    loadExerciseLabels();
  }
  
  private void loadExerciseLabels() {
    exerciseLabels = Arrays.asList(
      "bench_press", "burpee", "crunches", "dumbbell_squat", 
      "high_knees", "jumping_jacks", "pull_up", "push_up", 
      "russian_twist", "sit_up", "squat"
    );
    System.out.println("Loaded exercise labels: " + exerciseLabels);
  }
  
  public PostureResponse predict(float[][] landmarks) throws OrtException {
    // Validate input: [33][2] (x,y coordinates)
    if (landmarks == null || landmarks.length != LANDMARK_COUNT || landmarks[0].length != COORDINATE_COUNT) {
      throw new IllegalArgumentException(
        "Landmarks must be of shape [33][2] (x,y coordinates). Received: " + 
        (landmarks == null ? "null" : landmarks.length + "x" + (landmarks.length > 0 ? landmarks[0].length : "N/A"))
      );
    }
    
    // Flatten landmarks to [66] array
    float[] flattenedLandmarks = new float[LANDMARK_COUNT * COORDINATE_COUNT];
    int idx = 0;
    for (float[] landmark : landmarks) {
      flattenedLandmarks[idx++] = landmark[0]; // x
      flattenedLandmarks[idx++] = landmark[1]; // y
    }
    
    // Add to sequence buffer
    sequenceBuffer.add(flattenedLandmarks);
    
    // Keep only last SEQUENCE_LENGTH frames
    if (sequenceBuffer.size() > SEQUENCE_LENGTH) {
      sequenceBuffer.remove(0);
    }
    
    // Need at least SEQUENCE_LENGTH frames for prediction
    if (sequenceBuffer.size() < SEQUENCE_LENGTH) {
      return new PostureResponse("insufficient_data", 0.0f);
    }
    
    // Convert to tensor: [1, 30, 66]
    float[][][] inputTensor = new float[1][SEQUENCE_LENGTH][LANDMARK_COUNT * COORDINATE_COUNT];
    for (int i = 0; i < SEQUENCE_LENGTH; i++) {
      inputTensor[0][i] = sequenceBuffer.get(i);
    }
    
    OnnxTensor tensor = OnnxTensor.createTensor(env, inputTensor);
    Map<String, OnnxTensor> inputs = Collections.singletonMap(INPUT_NODE_NAME, tensor);
    
    try (OrtSession.Result results = session.run(inputs)) {
      OnnxValue outputValue = results.get(0); // Single output
      float[][] logits = (float[][]) outputValue.getValue();
      
      tensor.close();
      
      // Find predicted class
      int predictedClassIndex = 0;
      float maxLogit = Float.NEGATIVE_INFINITY;
      for (int i = 0; i < logits[0].length; i++) {
        if (logits[0][i] > maxLogit) {
          maxLogit = logits[0][i];
          predictedClassIndex = i;
        }
      }
      
      // Convert logits to probabilities (softmax)
      float[] probabilities = softmax(logits[0]);
      float confidence = probabilities[predictedClassIndex];
      
      String predictedExerciseName = "Unknown";
      if (predictedClassIndex >= 0 && predictedClassIndex < exerciseLabels.size()) {
        predictedExerciseName = exerciseLabels.get(predictedClassIndex);
      }
      
      return new PostureResponse(predictedExerciseName, confidence);
    }
  }
  
  float[] softmax(float[] logits) {
    float max = Float.NEGATIVE_INFINITY;
    for (float logit : logits) {
        if (logit > max) {
            max = logit;
        }
    }
    
    float sum = 0f;
    float[] exp = new float[logits.length];
    
    for (int i = 0; i < logits.length; i++) {
        exp[i] = (float) Math.exp(logits[i] - max);
        sum += exp[i];
    }
    
    // Normalize to probabilities
    for (int i = 0; i < exp.length; i++) {
        exp[i] /= sum;
    }
    
    return exp;
  }
  @PreDestroy
  public void close() {
    if (session != null) {
      try {
        session.close();
      } catch (OrtException e) {
        System.err.println("Error closing ONNX session: " + e.getMessage());
      }
    }
    if (env != null) {
      try {
        env.close();
      } catch (RuntimeException e) {
        System.err.println("Error closing ONNX environment: " + e.getMessage());
      }
    }
  }
}
