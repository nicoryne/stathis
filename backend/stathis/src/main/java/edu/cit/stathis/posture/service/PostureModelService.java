package edu.cit.stathis.posture.service;

import ai.onnxruntime.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.cit.stathis.posture.dto.ClassificationResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class PostureModelService {
  private OrtEnvironment env;
  private OrtSession session;
  private int timeSteps;
  private List<String> classNames = new ArrayList<>();
  private static final int NUM_FEATURES = 132; // 33 landmarks * (x,y,z,visibility)

  @PostConstruct
  public void init() throws OrtException, IOException {
    env = OrtEnvironment.getEnvironment();

    InputStream modelStream = new ClassPathResource("models/model.onnx").getInputStream();
    byte[] modelBytes = modelStream.readAllBytes();
    modelStream.close();

    session = env.createSession(modelBytes, new OrtSession.SessionOptions());

    loadModelConfig();
  }

  private void loadModelConfig() throws IOException {
    ClassPathResource cfgRes = new ClassPathResource("models/model_config.json");
    try (InputStream cfgIn = cfgRes.getInputStream()) {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode cfg = mapper.readTree(cfgIn);
      this.timeSteps = cfg.has("time_steps") ? cfg.get("time_steps").asInt() : 30;
      if (cfg.has("class_names")) {
        this.classNames = mapper.convertValue(cfg.get("class_names"), new TypeReference<List<String>>() {});
      }
    }
  }

  public ClassificationResult classify(float[][][] window) {
    if (window == null || window.length != 1 || window[0].length != timeSteps || window[0][0].length != NUM_FEATURES) {
      throw new IllegalArgumentException("Input window must be shaped [1," + timeSteps + "," + NUM_FEATURES + "]");
    }

    OnnxTensor tensor = null;
    try {
      tensor = OnnxTensor.createTensor(env, window);

      String inputName = getFirstInputName(session);
      Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, tensor);

      OrtSession.Result results = null;
      try {
        results = session.run(inputs);
        OnnxValue outputValue = results.get(0);
        float[][] logits = readOnnxOutputAs2DFloatArray(outputValue);


        float[] probs = softmax(logits[0]);
        int bestIdx = argmax(probs);
        String predicted = bestIdx >= 0 && bestIdx < classNames.size() ? classNames.get(bestIdx) : "unknown";

        // Build last frame landmarks [33][4] from last time step for rules
        float[] last = window[0][timeSteps - 1];
        float[][] lastFrame = new float[33][4];
        for (int i = 0; i < 33; i++) {
          int base = i * 4;
          lastFrame[i][0] = last[base];
          lastFrame[i][1] = last[base + 1];
          lastFrame[i][2] = last[base + 2];
          lastFrame[i][3] = last[base + 3];
        }

        ClassificationResult result = new ClassificationResult();
        result.setPredictedClass(predicted);
        result.setScore(probs[bestIdx]);
        result.setProbabilities(probs);
        result.setClassNames(classNames);
        return result;
      } finally {
        if (results != null) {
          try {
            results.close();
          } catch (Exception ignored) {}
        }
      }
    } catch (OrtException e) {
      throw new IllegalStateException("ONNX inference failed", e);
    } finally {
      if (tensor != null) {
        try {
          tensor.close();
        } catch (Exception ignored) {
        }
      }
    }
  }

  private static String getFirstInputName(OrtSession session) {
    try {
      Iterator<String> it = session.getInputInfo().keySet().iterator();
      if (!it.hasNext()) {
        throw new IllegalStateException("ONNX model has no inputs");
      }
      return it.next();
    } catch (OrtException e) {
      throw new IllegalStateException("Failed to read ONNX model input info", e);
    }
  }

  private static int argmax(float[] a) {
    int idx = 0;
    float best = Float.NEGATIVE_INFINITY;
    for (int i = 0; i < a.length; i++) {
      if (a[i] > best) {
        best = a[i];
        idx = i;
      }
    }
    return idx;
  }

  private static float[][] readOnnxOutputAs2DFloatArray(OnnxValue value) {
    try {
      Object raw = value.getValue();
      return (float[][]) raw;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to read ONNX output tensor", e);
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
