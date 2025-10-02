package edu.cit.stathis.posture.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class PostureRulesService {

  public static class RulesResult {
    public List<String> flags;
    public List<String> messages;

    public RulesResult(List<String> flags, List<String> messages) {
      this.flags = flags;
      this.messages = messages;
    }
  }

  public RulesResult evaluate(String predictedClass, float[][] lastFrame) {
    Set<String> flags = new HashSet<>();
    List<String> messages = new ArrayList<>();

    if (predictedClass == null || lastFrame == null || lastFrame.length != 33 || lastFrame[0].length != 4) {
      return new RulesResult(new ArrayList<>(flags), messages);
    }

    switch (predictedClass) {
      case "squat":
        applySquatRules(lastFrame, flags, messages);
        break;
      case "push_up":
        applyPushUpRules(lastFrame, flags, messages);
        break;
      case "plank":
        applyPlankRules(lastFrame, flags, messages);
        break;
      case "sit_up":
        applySitUpRules(lastFrame, flags, messages);
        break;
      default:
        break;
    }

    return new RulesResult(new ArrayList<>(flags), messages);
  }

  private void applySquatRules(float[][] lm, Set<String> flags, List<String> messages) {
    float[] lHip = lm[23];
    float[] rHip = lm[24];
    float[] lKnee = lm[25];
    float[] rKnee = lm[26];
    float[] lAnkle = lm[27];
    float[] rAnkle = lm[28];
    float[] lShoulder = lm[11];
    float[] rShoulder = lm[12];

    float[] hipCenter = midpoint(lHip, rHip);
    float[] shoulderCenter = midpoint(lShoulder, rShoulder);

    float kneeAngleLeft = angle(lHip, lKnee, lAnkle);
    float kneeAngleRight = angle(rHip, rKnee, rAnkle);
    float minKnee = Math.min(kneeAngleLeft, kneeAngleRight);
    if (minKnee > 150f) {
      flags.add("depth_low");
      messages.add("Go deeper to at least parallel.");
    }

    boolean kneesInLeft = (Math.abs(lKnee[0] - hipCenter[0]) < Math.abs(lAnkle[0] - hipCenter[0]));
    boolean kneesInRight = (Math.abs(rKnee[0] - hipCenter[0]) < Math.abs(rAnkle[0] - hipCenter[0]));
    if (kneesInLeft && kneesInRight) {
      flags.add("knees_in");
      messages.add("Push knees outward over toes.");
    }

    float torsoLean = angleToVertical(vector(shoulderCenter, hipCenter));
    if (torsoLean > 40f) {
      flags.add("chest_up");
      messages.add("Keep chest up.");
    }
  }

  private void applyPushUpRules(float[][] lm, Set<String> flags, List<String> messages) {
    float[] shoulder = midpoint(lm[11], lm[12]);
    float[] hip = midpoint(lm[23], lm[24]);
    float[] ankle = midpoint(lm[27], lm[28]);

    float sagMetric = hip[1] - lineYAtX(shoulder, ankle, hip[0]);
    if (sagMetric < -0.1f) {
      flags.add("pike");
      messages.add("Keep a straight line from head to heels.");
    } else if (sagMetric > 0.1f) {
      flags.add("sag");
      messages.add("Avoid sagging hips.");
    }
  }

  private void applyPlankRules(float[][] lm, Set<String> flags, List<String> messages) {
    applyPushUpRules(lm, flags, messages);
    if (flags.isEmpty()) {
      messages.add("Maintain a straight line from shoulders to heels.");
    }
  }

  private void applySitUpRules(float[][] lm, Set<String> flags, List<String> messages) {
    float[] shoulder = midpoint(lm[11], lm[12]);
    float[] hip = midpoint(lm[23], lm[24]);
    if (shoulder[1] - hip[1] > -0.1f) {
      flags.add("low_rom");
      messages.add("Increase trunk flexion.");
    }
  }

  private static float[] midpoint(float[] a, float[] b) {
    return new float[] {(a[0] + b[0]) * 0.5f, (a[1] + b[1]) * 0.5f, (a[2] + b[2]) * 0.5f, 1f};
  }

  private static float[] vector(float[] from, float[] to) {
    return new float[] {to[0] - from[0], to[1] - from[1], to[2] - from[2], 1f};
  }

  private static float angle(float[] a, float[] b, float[] c) {
    float[] ba = new float[] {a[0] - b[0], a[1] - b[1], a[2] - b[2]};
    float[] bc = new float[] {c[0] - b[0], c[1] - b[1], c[2] - b[2]};
    float dot = ba[0] * bc[0] + ba[1] * bc[1] + ba[2] * bc[2];
    float nba = (float) Math.sqrt(ba[0] * ba[0] + ba[1] * ba[1] + ba[2] * ba[2]);
    float nbc = (float) Math.sqrt(bc[0] * bc[0] + bc[1] * bc[1] + bc[2] * bc[2]);
    float cos = dot / (nba * nbc + 1e-6f);
    cos = Math.max(-1f, Math.min(1f, cos));
    return (float) (Math.acos(cos) * 180.0 / Math.PI);
  }

  private static float angleToVertical(float[] v) {
    float[] vertical = new float[] {0f, -1f, 0f};
    float dot = v[0] * vertical[0] + v[1] * vertical[1] + v[2] * vertical[2];
    float nv = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    float cos = dot / (nv + 1e-6f);
    cos = Math.max(-1f, Math.min(1f, cos));
    return (float) (Math.acos(cos) * 180.0 / Math.PI);
  }

  private static float lineYAtX(float[] p1, float[] p2, float x) {
    float dx = p2[0] - p1[0];
    if (Math.abs(dx) < 1e-6f) return p1[1];
    float t = (x - p1[0]) / dx;
    return p1[1] + t * (p2[1] - p1[1]);
  }
}


