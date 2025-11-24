# ONNX Model Specification

## Model File
- **File**: `model.onnx`
- **Format**: ONNX Runtime (opset version 17, auto-upgraded to 18)
- **Size**: ~24.5M parameters
- **Framework**: Converted from PyTorch

## Input Specification

### Input Name
`pose_sequence`

### Input Shape
```
(batch_size, 45, 132)
```

Where:
- **batch_size**: Number of sequences to process (can be dynamic, e.g., 1, 4, 8, etc.)
- **45**: Sequence length (temporal window size in frames)
- **132**: Features per frame (33 landmarks × 4 values)

### Input Data Type
`float32` (numpy array)

### Input Format Details

Each frame contains **33 pose landmarks** from Google ML Kit Pose Detection, where each landmark has **4 values**:

1. **X coordinate** (0.0 - 1.0): Normalized horizontal position
2. **Y coordinate** (0.0 - 1.0): Normalized vertical position
3. **Z coordinate** (-0.5 - 0.5): Relative depth information
4. **InFrameLikelihood** (0.0 - 1.0): Confidence that landmark is visible and accurate

**Total**: 33 landmarks × 4 values = **132 features per frame**

**Sequence**: 45 consecutive frames = **5,940 total values per sequence**

### Input Example

```python
import numpy as np

# Single sequence (batch_size=1)
pose_sequence = np.random.randn(1, 45, 132).astype(np.float32)

# Batch of 4 sequences
pose_sequence = np.random.randn(4, 45, 132).astype(np.float32)
```

## Output Specification

The model produces **two outputs** simultaneously:

### Output 1: Pose Classification

**Name**: `pose_classification`

**Shape**: `(batch_size, 5)`

**Type**: `float32` (logits, not probabilities)

**Description**: Raw classification scores (logits) for 5 pose classes:
- Index 0: `plank`
- Index 1: `push_up`
- Index 2: `rest`
- Index 3: `sit_up`
- Index 4: `squat`

**Post-processing**: Apply softmax to convert logits to probabilities:
```python
import numpy as np

# Get logits from model
classification_logits = outputs[0]  # Shape: (batch_size, 5)

# Convert to probabilities
exp_logits = np.exp(classification_logits - np.max(classification_logits, axis=1, keepdims=True))
probabilities = exp_logits / np.sum(exp_logits, axis=1, keepdims=True)

# Get predicted class
predicted_class_idx = np.argmax(probabilities, axis=1)
```

### Output 2: Form Confidence

**Name**: `form_confidence`

**Shape**: `(batch_size, 1)`

**Type**: `float32`

**Description**: Exercise form quality score (already sigmoid-applied, range 0.0-1.0)

**Interpretation**:
- **0.0 - 0.4**: Poor form (needs correction)
- **0.5 - 0.7**: Moderate form (needs improvement)
- **0.8 - 1.0**: Good form (correct technique)
- **For 'rest' pose**: Always 0.0 (form confidence not applicable)

**Note**: Form confidence is only meaningful for non-rest poses. For 'rest' class, ignore the form confidence value.

## Usage Example

```python
import numpy as np
import onnxruntime as ort

# Load model
session = ort.InferenceSession("model.onnx")

# Get input/output names
input_name = session.get_inputs()[0].name
output_names = [output.name for output in session.get_outputs()]

# Prepare input (replace with real ML Kit pose data)
pose_sequence = np.random.randn(1, 45, 132).astype(np.float32)

# Run inference
outputs = session.run(output_names, {input_name: pose_sequence})

# Process outputs
classification_logits = outputs[0]  # Shape: (1, 5)
form_confidence = outputs[1]  # Shape: (1, 1)

# Convert logits to probabilities
exp_logits = np.exp(classification_logits - np.max(classification_logits, axis=1, keepdims=True))
probabilities = exp_logits / np.sum(exp_logits, axis=1, keepdims=True)

# Get results
pose_classes = ['plank', 'push_up', 'rest', 'sit_up', 'squat']
predicted_class_idx = np.argmax(probabilities[0])
predicted_class = pose_classes[predicted_class_idx]
confidence = probabilities[0, predicted_class_idx]
form_score = form_confidence[0, 0]

print(f"Predicted pose: {predicted_class} ({confidence:.2%})")
print(f"Form confidence: {form_score:.2f}")
```

## Performance

- **Inference time**: ~10-50ms per sequence (depending on hardware)
- **Batch processing**: Supported (process multiple sequences at once)
- **Memory**: ~100MB model size

## Platform Support

- ✅ **CPU**: ONNX Runtime CPU
- ✅ **GPU**: ONNX Runtime GPU (CUDA)
- ✅ **Mobile**: ONNX Runtime Mobile
- ✅ **Web**: ONNX.js (with conversion)
- ✅ **Edge**: ONNX Runtime for edge devices

## Notes

1. **Input normalization**: Ensure pose landmarks are in the correct range:
   - X, Y: [0.0, 1.0]
   - Z: [-0.5, 0.5]
   - InFrameLikelihood: [0.0, 1.0]

2. **Temporal windowing**: The model expects exactly 45 frames. If you have fewer frames, pad with the last frame. If you have more, create overlapping windows.

3. **Form confidence masking**: For 'rest' class predictions, form confidence should be ignored (always 0.0).

4. **Batch processing**: The model supports dynamic batch sizes, so you can process 1 or multiple sequences at once for better throughput.

