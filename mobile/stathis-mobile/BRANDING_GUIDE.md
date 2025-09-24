# Stathis Mobile App - Branding & Design Guide

## 🎨 Brand Identity

### Core Brand Colors
- **Primary Purple**: `#9334EA` - Main brand color, CTAs, highlights
- **Secondary Teal**: `#25ACA4` - Success states, achievements, health indicators

### Extended Color Palette
- **Light Purple**: `#F0DBFF` - Backgrounds, subtle highlights
- **Light Teal**: `#9DF2EA` - Health indicators, gentle backgrounds
- **Success Green**: `#4CAF50` - Achievements, completions
- **Warning Orange**: `#FFA000` - Caution states
- **Error Red**: `#F44336` - Error states

## 🔤 Typography

### Font Families
- **Display**: Outfit (existing) - Headlines, mascot speech bubbles
- **Body**: Manrope (existing) - Body text, descriptions

### Text Hierarchy
- **Hero Title**: 32sp, Bold - Main page titles
- **Page Title**: 24sp, Bold - Section headers
- **Section Title**: 20sp, Bold - Subsection headers
- **Body Large**: 18sp, Medium - Important body text
- **Body Medium**: 16sp, Normal - Standard body text
- **Body Small**: 14sp, Normal - Secondary information
- **Button Large**: 18sp, Bold - Primary buttons
- **Button Medium**: 16sp, Bold - Secondary buttons
- **Caption**: 12sp, Normal - Labels and captions

## 🎯 Duolingo-Inspired Design Principles

### 1. Simplicity First
- **One main action per screen**
- **Large, clear CTAs**
- **Minimal cognitive load**
- **Big text, generous spacing**

### 2. Gamification Elements
- **Progress rings and bars**
- **Achievement celebrations**
- **Streak indicators**
- **Level progression**
- **Mascot interactions**

### 3. Visual Hierarchy
- **Purple for primary actions**
- **Teal for success/achievements**

## 🚀 Modern UI Design Principles

### 1. Full-Screen Immersive Experience
- **Transparent/floating navigation bars**
- **Edge-to-edge content with proper padding**
- **No constricted white headers/footers**
- **Content flows naturally across the screen**

### 2. Creative Information Display
- **Mascot-centered main menu design**
- **Carousel of learning modules**
- **Streak counter in upper right corner**
- **Personalized greeting: "Hello, {user's name}"**
- **Full-screen learn menu without scrolling**

### 3. Modern Navigation Patterns
- **Floating bottom navigation**
- **Transparent top bars with blur effects**
- **Gesture-based interactions**
- **Contextual navigation elements**

### 4. Visual Consistency Rules
- **Avoid color mismatches**
- **Maintain consistent visual hierarchy**
- **Ensure responsive design across screen sizes**
- **Use creative layouts over standard patterns**

### 5. Information Architecture
- **Primary content takes full screen real estate**
- **Secondary actions accessible but not intrusive**
- **Mascot as central focal point**
- **Learning modules as interactive carousel**
- **Progress indicators integrated naturally**
- **Orange for warnings**
- **Red for errors**
- **Neutral grays for secondary content**

## 🎭 Mascot Integration

### Mascot Color Scheme
- **Primary**: Purple (`#9334EA`)
- **Secondary**: Teal (`#25ACA4`)
- **Accent**: Gold (`#FFD700`) for special moments

### Mascot States
- **Happy**: Green (`#4CAF50`)
- **Encouraging**: Teal (`#25ACA4`)
- **Celebrating**: Gold (`#FFD700`)
- **Concerned**: Orange (`#FFA000`)

### Mascot Speech Bubbles
- **Background**: Light Purple (`#F0DBFF`)
- **Text**: Primary Purple (`#9334EA`)
- **Shape**: Rounded corners (20dp)

## 🎮 Gamification Colors

### Streak System
- **Active Streak**: Green (`#4CAF50`)
- **Inactive Streak**: Disabled Gray

### Level System
- **Beginner**: Teal (`#25ACA4`)
- **Intermediate**: Purple (`#9334EA`)
- **Advanced**: Gold (`#FFD700`)

### Achievement System
- **Gold Achievement**: Gold (`#FFD700`)
- **Silver Achievement**: Silver (`#C0C0C0`)
- **Bronze Achievement**: Bronze (`#CD7F32`)

## 📐 Spacing & Layout

### Spacing Scale
- **XS**: 4dp - Minimal spacing
- **SM**: 8dp - Small spacing
- **MD**: 16dp - Standard spacing
- **LG**: 24dp - Large spacing
- **XL**: 32dp - Extra large spacing
- **XXL**: 48dp - Maximum spacing

### Component Spacing
- **Card Padding**: 16dp
- **Screen Padding**: 24dp
- **Button Padding**: 16dp
- **Mascot Padding**: 24dp

## 🔲 Shapes & Corners

### Corner Radius
- **Cards**: 12dp (standard), 16dp (large)
- **Buttons**: 24dp (pill-shaped), 12dp (small)
- **Mascot Containers**: 20dp
- **Progress Indicators**: 8dp

## ⚡ Animations

### Duration
- **Fast**: 150ms - Quick feedback
- **Normal**: 300ms - Standard transitions
- **Slow**: 500ms - Complex animations
- **Mascot Reaction**: 800ms - Mascot interactions

### Easing
- **Smooth curves** for delightful feel
- **Bounce effects** for celebrations
- **Fade transitions** for state changes

## 🎨 Component Examples

### Primary Button
```kotlin
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = StathisColors.Primary,
        contentColor = Color.White
    ),
    shape = StathisShapes.ButtonShape,
    modifier = Modifier.padding(StathisSpacing.MD)
) {
    Text(
        text = "Start Learning",
        style = StathisTypography.ButtonLarge
    )
}
```

### Achievement Card
```kotlin
Card(
    shape = StathisShapes.CardShape,
    colors = CardDefaults.cardColors(
        containerColor = StathisColors.AchievementLight
    ),
    elevation = CardDefaults.cardElevation(
        defaultElevation = StathisElevation.Card
    )
) {
    Column(
        modifier = Modifier.padding(StathisSpacing.CardPadding)
    ) {
        Text(
            text = "Achievement Unlocked!",
            style = StathisTypography.CardTitle,
            color = StathisColors.Achievement
        )
        Text(
            text = "Complete your first exercise",
            style = StathisTypography.CardSubtitle
        )
    }
}
```

### Mascot Speech Bubble
```kotlin
Card(
    shape = StathisShapes.MascotContainerShape,
    colors = CardDefaults.cardColors(
        containerColor = StathisColors.Mascot.SpeechBubbleBackground
    )
) {
    Text(
        text = "Great job! Ready for your next challenge?",
        style = StathisTypography.MascotSpeech,
        color = StathisColors.Mascot.SpeechBubbleText,
        modifier = Modifier.padding(StathisSpacing.MascotPadding)
    )
}
```

## 🚀 Implementation Guidelines

### 1. Color Usage
- **Always use design system colors** - don't hardcode colors
- **Purple for primary actions** - buttons, links, highlights
- **Teal for success states** - achievements, completions, health
- **Orange for warnings** - caution, attention needed
- **Red for errors** - failures, critical issues

### 2. Typography
- **Use semantic text styles** - don't hardcode font sizes
- **Maintain hierarchy** - larger text for more important content
- **Keep readability** - sufficient contrast, appropriate line height

### 3. Spacing
- **Use spacing scale** - maintain consistent rhythm
- **Group related elements** - use smaller spacing within groups
- **Separate sections** - use larger spacing between sections

### 4. Animations
- **Keep it delightful** - smooth, purposeful animations
- **Don't overdo it** - animations should enhance, not distract
- **Consider performance** - lightweight animations for better UX

## 📱 Screen-Specific Guidelines

### Learn Tab (Main Hub)
- **Primary Purple** for main CTA buttons
- **Large, bold headlines** for focus
- **Mascot guidance** with speech bubbles
- **Single task focus** - one main action

### Progress Tab
- **Teal accents** for achievements
- **Gold highlights** for special accomplishments
- **Celebration animations** for milestones
- **Visual progress indicators**

### Profile Tab
- **Minimal design** - focus on essential info
- **Mascot customization** options
- **Purple accents** for interactive elements
- **Clean, organized layout**

This branding guide ensures consistency across the app while maintaining the Duolingo-inspired simplicity and gamification elements that make learning engaging and fun!
