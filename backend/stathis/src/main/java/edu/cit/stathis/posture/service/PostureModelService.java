package edu.cit.stathis.posture.service;

import ai.onnxruntime.*;
import edu.cit.stathis.posture.dto.PostureResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
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
  private final String ACTUAL_INPUT_NODE_NAME = "input_pose";

  @PostConstruct
  public void init() throws OrtException, IOException {
    env = OrtEnvironment.getEnvironment();

    InputStream modelStream = new ClassPathResource("models/pose_model.onnx").getInputStream();
    byte[] modelBytes = modelStream.readAllBytes();
    modelStream.close();

    session = env.createSession(modelBytes, new OrtSession.SessionOptions());

    System.out.println("ONNX Model Input Info: " + session.getInputInfo());
    if (session.getInputInfo().containsKey(ACTUAL_INPUT_NODE_NAME)) {
      System.out.println(
          "Details for '"
              + ACTUAL_INPUT_NODE_NAME
              + "': "
              + session.getInputInfo().get(ACTUAL_INPUT_NODE_NAME).getInfo().toString());
    } else {
      System.err.println(
          "ERROR: Expected input node name '" + ACTUAL_INPUT_NODE_NAME + "' not found in model!");
      System.err.println("Available input nodes: " + session.getInputInfo().keySet());
    }
    System.out.println("ONNX Model Output Info: " + session.getOutputInfo());

    loadExerciseLabels();
  }

  private void loadExerciseLabels() {
    exerciseLabels =
        Arrays.asList(
            "bent over row",
            "chest dips",
            "glute bridge",
            "lunges",
            "plank",
            "pullup",
            "pushup",
            "shoulder press",
            "squat");
    System.out.println("Loaded exercise labels: " + exerciseLabels);
  }

  public PostureResponse predict(float[][][] landmarks) throws OrtException {
    if (landmarks == null
        || landmarks.length != 1
        || landmarks[0].length != 33
        || landmarks[0][0].length != 3) {
      throw new IllegalArgumentException(
          "Landmarks must be of shape [1][33][3]. Received shape: ["
              + (landmarks == null
                  ? "null"
                  : landmarks.length
                      + "]["
                      + (landmarks != null && landmarks.length > 0 ? landmarks[0].length : "N/A")
                      + "]["
                      + (landmarks != null && landmarks.length > 0 && landmarks[0].length > 0
                          ? landmarks[0][0].length
                          : "N/A")
                      + "]"));
    }

    OnnxTensor inputTensor = OnnxTensor.createTensor(env, landmarks);
    Map<String, OnnxTensor> inputs = Collections.singletonMap(ACTUAL_INPUT_NODE_NAME, inputTensor);

    try (OrtSession.Result results = session.run(inputs)) {

      OnnxValue classLogitsValue = results.get(0);
      float[][] predictedClassLogits = (float[][]) classLogitsValue.getValue();

      OnnxValue postureScoreValue = results.get(1);
      float[][] postureScoreOutput = (float[][]) postureScoreValue.getValue();

      inputTensor.close();

      int predictedClassIndex = 0;
      float maxLogit = Float.NEGATIVE_INFINITY;
      if (predictedClassLogits.length > 0 && predictedClassLogits[0].length > 0) {
        for (int i = 0; i < predictedClassLogits[0].length; i++) {
          if (predictedClassLogits[0][i] > maxLogit) {
            maxLogit = predictedClassLogits[0][i];
            predictedClassIndex = i;
          }
        }
      } else {
        throw new OrtException("Classification output is empty or malformed.");
      }

      String predictedExerciseName = "Unknown";
      if (predictedClassIndex >= 0 && predictedClassIndex < exerciseLabels.size()) {
        predictedExerciseName = exerciseLabels.get(predictedClassIndex);
      } else {
        System.err.println(
            "Warning: Predicted class index "
                + predictedClassIndex
                + " is out of bounds for loaded labels (size: "
                + exerciseLabels.size()
                + "). Using default 'Unknown'.");
      }

      float finalPostureScore = 0.0f;
      if (postureScoreOutput.length > 0 && postureScoreOutput[0].length > 0) {
        finalPostureScore = postureScoreOutput[0][0];
        finalPostureScore = Math.max(0.0f, Math.min(1.0f, finalPostureScore));
      } else {
        throw new OrtException("Score output is empty or malformed.");
      }

      return new PostureResponse(predictedExerciseName, finalPostureScore);
    }
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
