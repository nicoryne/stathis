# HOW TO ADD MASCOT ASSETS

## 🎭 Overview
This guide provides comprehensive instructions for adding mascot assets to the Stathis Mobile App. The mascot system supports both static images and animated content, with automatic state management and speech bubble integration.

## 📁 File Structure & Locations

### Static Images (PNG/WebP)
```
app/src/main/res/drawable/
├── mascot_happy.png          # Happy state (celebrations, achievements)
├── mascot_celebrating.png    # Celebrating state (level ups, streaks)
├── mascot_encouraging.png    # Encouraging state (motivation, guidance)
├── mascot_concerned.png      # Concerned state (health warnings, breaks)
├── mascot_sleeping.png       # Sleeping state (rest periods, idle)
└── mascot_neutral.png        # Default neutral state
```

### Animated Content (Lottie JSON)
```
app/src/main/res/raw/
├── mascot_wave.json          # Welcome animation
├── mascot_celebration.json   # Achievement celebration
├── mascot_encouragement.json # Motivation animation
├── mascot_sleep.json         # Sleep/idle animation
└── mascot_concern.json       # Concern/warning animation
```

### Speech Text (Strings)
```
app/src/main/res/values/strings.xml
<!-- Mascot speech content -->
<string name="mascot_welcome">Welcome back! Ready to learn today?</string>
<string name="mascot_achievement">Amazing! You're doing great!</string>
<string name="mascot_encouragement">Keep going! You've got this!</string>
<string name="mascot_concern">Take a break! Your health matters.</string>
<string name="mascot_sleep">Time to rest. See you tomorrow!</string>
```

## 🖼️ Static Image Specifications

### Recommended Formats
- **Primary**: PNG (lossless, supports transparency)
- **Alternative**: WebP (smaller file size, good compression)
- **Avoid**: JPEG (no transparency support)

### Resolution Guidelines
- **Minimum**: 256x256px
- **Recommended**: 512x512px
- **Maximum**: 1024x1024px (to avoid memory issues)
- **Aspect Ratio**: 1:1 (square) for consistent display

### Design Requirements
- **Background**: Transparent (PNG with alpha channel)
- **Style**: Consistent with app's Duolingo-inspired design
- **Colors**: Should work with the app's color scheme
- **Expression**: Clear emotional state representation

### File Naming Convention
```
mascot_[state].png
```
States: `happy`, `celebrating`, `encouraging`, `concerned`, `sleeping`, `neutral`

## 🎬 Animated Content (Lottie)

### What is Lottie?
Lottie is a library that renders After Effects animations natively on mobile devices. It's perfect for mascot animations because it's:
- **Lightweight**: Small file sizes
- **Scalable**: Vector-based animations
- **Performant**: Hardware accelerated
- **Cross-platform**: Works on Android and iOS

### Converting GIFs to Lottie

#### Method 1: Using After Effects (Recommended)
1. **Import GIF**: Open After Effects and import your GIF
2. **Create Composition**: Set up composition with proper dimensions
3. **Optimize**: Remove unnecessary frames, optimize colors
4. **Export**: Use Bodymovin plugin to export as JSON
5. **Validate**: Test the JSON file in LottieFiles preview

#### Method 2: Using Online Converters
1. **Upload GIF**: Use tools like:
   - [LottieFiles Converter](https://lottiefiles.com/tools/converter)
   - [GIF to Lottie Converter](https://convertio.co/gif-lottie/)
2. **Download JSON**: Get the converted Lottie JSON file
3. **Test**: Verify the animation works correctly

#### Method 3: Using Figma (For Designers)
1. **Create Animation**: Design animation in Figma
2. **Export**: Use Figma's Lottie export feature
3. **Download**: Get the JSON file directly

### Lottie File Specifications
- **Format**: JSON files
- **File Size**: Keep under 100KB for optimal performance
- **Duration**: 2-5 seconds for mascot animations
- **Loop**: Set to loop for continuous animations
- **Dimensions**: Match your static image dimensions

### Recommended Animation Types
- **Wave**: Simple hand/arm wave for greetings
- **Celebration**: Jumping, clapping, or cheering
- **Encouragement**: Thumbs up, nodding, or pointing
- **Concern**: Shaking head, worried expression
- **Sleep**: Gentle breathing, closed eyes

## 🔧 Implementation Guide

### Step 1: Add Static Images
1. **Prepare Images**: Ensure all 6 states are ready
2. **Optimize**: Compress images while maintaining quality
3. **Place Files**: Copy to `app/src/main/res/drawable/`
4. **Test**: Verify images display correctly

### Step 2: Add Lottie Animations
1. **Convert Animations**: Use methods above to create JSON files
2. **Place Files**: Copy to `app/src/main/res/raw/`
3. **Update Component**: Modify `MascotAvatar.kt` to use Lottie

### Step 3: Update MascotAvatar Component
```kotlin
// Add Lottie dependency to build.gradle
implementation "com.airbnb.android:lottie-compose:6.1.0"

// Update MascotAvatar.kt to support animations
@Composable
fun MascotAvatar(
    state: MascotState,
    size: Int = 80,
    speechText: String? = null,
    showSpeechBubble: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Use LottieAnimation for animated states
    // Fall back to static images for non-animated states
}
```

### Step 4: Add Speech Content
1. **Define Strings**: Add speech text to `strings.xml`
2. **Update Component**: Modify mascot to use string resources
3. **Localize**: Add translations for different languages

## 🎨 Design Guidelines

### Color Palette Integration
- **Primary**: Use app's purple (`#9334EA`) for main elements
- **Secondary**: Use app's teal (`#25ACA4`) for accents
- **Background**: Ensure mascot works on both light and dark themes
- **Contrast**: Maintain good contrast for accessibility

### Animation Principles
- **Smooth**: Use easing curves for natural movement
- **Subtle**: Avoid overly dramatic animations
- **Purposeful**: Each animation should have a clear meaning
- **Performance**: Keep animations lightweight

### Emotional States
- **Happy**: Bright, energetic, welcoming
- **Celebrating**: Excited, proud, triumphant
- **Encouraging**: Supportive, motivating, positive
- **Concerned**: Caring, protective, gentle warning
- **Sleeping**: Peaceful, calm, restful
- **Neutral**: Balanced, friendly, approachable

## 🛠️ Technical Implementation

### MascotAvatar Component Usage
```kotlin
// Basic usage
MascotAvatar(
    state = MascotState.HAPPY,
    size = 80,
    speechText = "Welcome back!",
    showSpeechBubble = true
)

// With animation
MascotAvatar(
    state = MascotState.CELEBRATING,
    size = 100,
    speechText = "Amazing work!",
    showSpeechBubble = true,
    modifier = Modifier.padding(16.dp)
)
```

### State Management
```kotlin
// Automatic state changes based on user progress
val mascotState = when {
    userAchievedGoal -> MascotState.CELEBRATING
    userNeedsBreak -> MascotState.CONCERNED
    userIsActive -> MascotState.ENCOURAGING
    userIsIdle -> MascotState.SLEEPING
    else -> MascotState.NEUTRAL
}
```

### Speech Bubble Integration
```kotlin
// Dynamic speech based on context
val speechText = when (context) {
    "welcome" -> "Welcome back! Ready to learn?"
    "achievement" -> "Great job! You're improving!"
    "break" -> "Take a break! Your health matters."
    else -> null
}
```

## 📱 Testing & Validation

### Image Testing
1. **Display**: Verify images show correctly on all screen sizes
2. **Performance**: Check memory usage with large images
3. **Quality**: Ensure images look crisp on high-DPI screens
4. **Accessibility**: Test with screen readers

### Animation Testing
1. **Playback**: Verify animations play smoothly
2. **Looping**: Check continuous animations loop correctly
3. **Performance**: Monitor CPU usage during animations
4. **Battery**: Ensure animations don't drain battery excessively

### Integration Testing
1. **State Changes**: Test automatic state transitions
2. **Speech Bubbles**: Verify text displays correctly
3. **Navigation**: Ensure mascot works across all screens
4. **Themes**: Test with light and dark themes

## 🚀 Performance Optimization

### Image Optimization
- **Compression**: Use tools like TinyPNG or ImageOptim
- **Format**: Choose WebP for smaller file sizes
- **Dimensions**: Use appropriate sizes for different screen densities
- **Caching**: Implement proper image caching

### Animation Optimization
- **File Size**: Keep Lottie files under 100KB
- **Complexity**: Avoid overly complex animations
- **Frame Rate**: Use 30fps for smooth playback
- **Memory**: Monitor memory usage during animations

### Best Practices
- **Lazy Loading**: Load animations only when needed
- **Preloading**: Cache frequently used animations
- **Cleanup**: Properly dispose of animation resources
- **Testing**: Test on various devices and performance levels

## 🔍 Troubleshooting

### Common Issues

#### Images Not Displaying
- **Check file names**: Ensure exact naming convention
- **Verify location**: Files must be in `drawable/` folder
- **Check format**: Use PNG or WebP formats
- **Validate dimensions**: Ensure proper image dimensions

#### Animations Not Playing
- **Check JSON format**: Validate Lottie JSON file
- **Verify file size**: Keep under 100KB
- **Test compatibility**: Ensure Lottie version compatibility
- **Check resources**: Verify file is in `raw/` folder

#### Performance Issues
- **Optimize images**: Compress and resize images
- **Simplify animations**: Reduce animation complexity
- **Monitor memory**: Check for memory leaks
- **Test devices**: Test on various device specifications

### Debug Tools
- **Lottie Preview**: Use LottieFiles preview to test animations
- **Image Tools**: Use tools like ImageOptim for optimization
- **Performance Profiler**: Use Android Studio profiler for performance
- **Memory Analyzer**: Monitor memory usage during development

## 📚 Resources

### Design Tools
- **Figma**: For creating mascot designs
- **Adobe After Effects**: For complex animations
- **LottieFiles**: For animation inspiration and tools
- **Material Design**: For design guidelines

### Conversion Tools
- **LottieFiles Converter**: Online GIF to Lottie converter
- **Bodymovin**: After Effects plugin for Lottie export
- **Figma Lottie**: Figma plugin for Lottie export
- **ImageOptim**: Image compression tool

### Testing Tools
- **Lottie Preview**: Test Lottie animations
- **Android Studio**: For development and testing
- **Device Farm**: For testing on various devices
- **Performance Profiler**: For optimization

## 🎯 Quick Start Checklist

### For Static Images:
- [ ] Create 6 mascot states (happy, celebrating, encouraging, concerned, sleeping, neutral)
- [ ] Use PNG format with transparency
- [ ] Optimize to 512x512px resolution
- [ ] Place files in `app/src/main/res/drawable/`
- [ ] Test display on different screen sizes

### For Animated Content:
- [ ] Convert GIFs to Lottie JSON format
- [ ] Keep file sizes under 100KB
- [ ] Create 2-5 second animations
- [ ] Place files in `app/src/main/res/raw/`
- [ ] Test playback performance

### For Integration:
- [ ] Add speech text to `strings.xml`
- [ ] Update `MascotAvatar.kt` component
- [ ] Test state transitions
- [ ] Verify speech bubble functionality
- [ ] Test across all app screens

---

**Remember**: The mascot is a key part of the user experience. Take time to create high-quality assets that enhance the learning journey! 🎭✨


