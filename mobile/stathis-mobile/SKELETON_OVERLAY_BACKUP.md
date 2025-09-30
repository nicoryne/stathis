# Skeleton Overlay Logic Backup

This file contains the preserved skeleton overlay logic from the exercise screen components.

## Files Preserved:
1. `PoseSkeletonOverlay.kt` - Custom view for rendering pose landmarks
2. `PoseSkeletonView.kt` - Alternative skeleton view implementation

## Key Features:
- Pose landmark detection and visualization
- Custom drawing with Paint objects
- Coordinate transformation for different camera orientations
- Skeleton connections between landmarks
- Confidence-based rendering
- Front/back camera handling
- Performance optimizations (double-buffering, background processing)

## Usage Notes:
- Both files implement custom Android Views for skeleton overlay
- `PoseSkeletonOverlay.kt` uses ML Kit Pose landmarks directly
- `PoseSkeletonView.kt` uses custom `LandmarkPoint` data structure
- Both handle front camera mirroring correctly
- Optimized for performance with hardware acceleration and transparency

## Dependencies:
- ML Kit Pose Detection
- Android Canvas and Paint APIs
- Custom data models for pose landmarks

This logic can be reused when rebuilding the exercise screen functionality.
