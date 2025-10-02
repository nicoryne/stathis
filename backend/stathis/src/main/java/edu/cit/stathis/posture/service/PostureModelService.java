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

  public ClassificationResult classify(float[][][] window) throws OrtException {
    if (window == null || window.length != 1 || window[0].length != timeSteps || window[0][0].length != NUM_FEATURES) {
      throw new IllegalArgumentException("Input window must be shaped [1," + timeSteps + "," + NUM_FEATURES + "]");
    }

    OnnxTensor tensor = OnnxTensor.createTensor(env, window);

    String inputName = getFirstInputName(session);
    Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, tensor);

    try (OrtSession.Result results = session.run(inputs)) {
      OnnxValue outputValue = results.get(0);
      float[][] logits = (float[][]) outputValue.getValue(); // [1][C]

      float[] probs = softmax(logits[0]);
      int bestIdx = argmax(probs);
      String predicted = bestIdx >= 0 && bestIdx < classNames.size() ? classNames.get(bestIdx) : "unknown";

      tensor.close();

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

      // Rules are applied in controller where PostureRulesService is available; return base data here
      ClassificationResult result = new ClassificationResult();
      result.setPredictedClass(predicted);
      result.setScore(probs[bestIdx]);
      result.setProbabilities(probs);
      result.setClassNames(classNames);
      return result;
    }
  }

  private static String getFirstInputName(OrtSession session) {
    Iterator<String> it = session.getInputInfo().keySet().iterator();
    if (!it.hasNext()) {
      throw new IllegalStateException("ONNX model has no inputs");
    }
    return it.next();
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
