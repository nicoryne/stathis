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

### Source of Truth for Colors (in code)
- Use Material 3 color schemes defined under:
  - `app/src/main/java/citu/edu/stathis/mobile/core/theme/Color.kt`
  - `app/src/main/java/citu/edu/stathis/mobile/core/theme/Theme.kt`
- Access colors via `MaterialTheme.colorScheme` inside composables.
- Wrap screens with `StathisTheme` (from `AppThemeWithProvider`) so the correct scheme is provided.

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

### Insets & System Bars
- Respect system bars using insets utilities:
  - Top: `Modifier.windowInsetsPadding(WindowInsets.statusBars)` for top bars
  - Bottom: `Modifier.windowInsetsPadding(WindowInsets.navigationBars)` for bottom areas
  - Bottom-only: `WindowInsets.navigationBars.only(sides = WindowInsetsSides.Bottom)`
  - Combine with content padding from `Scaffold` when using bottom navigation

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

### CTA Buttons
Use Material 3 `Button` with brand colors from `MaterialTheme.colorScheme`.

Primary CTA (filled):
```kotlin
Button(
    onClick = { },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(horizontal = 24.dp)
) {
    Text(
        text = "Start Learning",
        style = MaterialTheme.typography.labelLarge
    )
}
```

Secondary CTA (text/tonal):
```kotlin
TextButton(
    onClick = { },
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
) {
    Text(
        text = "Maybe later",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
}
```

### Achievement Card
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Achievement Unlocked!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Complete your first exercise",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
```

### Mascot Speech Bubble
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
) {
    Text(
        text = "Great job! Ready for your next challenge?",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(24.dp)
    )
}
```

### Insets usage in a screen
```kotlin
Scaffold(
    bottomBar = { /* Bottom nav */ }
) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (/* has bottom bar */ true) Modifier.padding(innerPadding)
                else Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
            .windowInsetsPadding(WindowInsets.navigationBars.only(sides = WindowInsetsSides.Bottom))
            .padding(horizontal = 24.dp)
    ) {
        // Content
    }
}
```

## 🚀 Implementation Guidelines

### 1. Color Usage
- **Use MaterialTheme.colorScheme** - don't hardcode colors
- **Define/adjust brand palettes** only in `core/theme/Color.kt`
- **Primary (purple)** for main CTAs and active indicators
- **Tertiary (teal)** for success/accent states
- **Orange/Red** for warnings/errors respectively

### 2. Typography
- **Use MaterialTheme.typography** - don't hardcode font sizes
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

---

## Onboarding Pattern (source of truth)

Use this as the canonical style to keep future screens consistent with the polished onboarding.

- **Theme defaults**: Dark theme by default; dynamic color disabled. Use brand palettes from `Color.kt`.
- **Background**: `MaterialTheme.colorScheme.surface` (dark navy tone), content on `onSurface` and `onSurfaceVariant`.
- **Typography**:
  - Title: `typography.headlineLarge` (Inter Bold)
  - Body: `typography.bodyLarge` (Outfit Regular)
  - Buttons/links: `typography.labelLarge` (uppercase when needed)
- **Spacing**:
  - Horizontal screen padding: `24.dp`
  - Vertical rhythm: `12.dp` between title and body; `16.dp` around CTA areas
  - Respect system bars: `WindowInsets.navigationBars.only(Bottom)` padding for bottom actions
- **Mascot usage**:
  - Placement: centered in top half
  - Scale: full width, fixed height ≈ `320.dp`, `ContentScale.Fit`
  - Single illustration per page; keep background uncluttered
- **CTA**:
  - Primary button: full width, height `56.dp`, pill-ish radius (Material 3 default), container = `colorScheme.primary`, content = `onPrimary`
  - Secondary link: uppercase, centered, uses `onSurface`
- **Pager**:
  - Horizontal pager with 3 slides
  - Dots indicator: 3 circular dots; active = `primary`; inactive = `onSurfaceVariant` 50% alpha; size `8.dp` (active `+2.dp`)
- **Copy style**:
  - Short headline (3–4 words)
  - One-sentence body; positive, action-oriented
- **Color usage**:
  - Primary: brand purple for CTAs and active indicators
  - Tertiary: teal for success accents (not used on onboarding buttons)
  - Error: red reserved for validation only

### Theme Selection Screen (pattern)

- **Purpose**: Let users preview and choose Light or Dark theme before continuing.
- **Layout**: Vertically centered column; large title; two option cards; helper text; confirm button.
- **Option cards**:
  - Container: `colorScheme.surfaceVariant`
  - Content: emoji (🌞/🌙) + label; text uses `onSurface`
  - Selected state: higher contrast border or elevation; keep background `surfaceVariant`
- **Live preview**: On tap, call `themeViewModel.setThemeMode(...)` and DO NOT navigate; reveal a "Confirm" button so colors visibly change.
- **Confirm CTA**: Full-width 56dp button; `containerColor = colorScheme.primary`; `contentColor = onPrimary`.
- **Background**: `colorScheme.surface` on the root container.
- **Insets**: `WindowInsets.navigationBars.only(Bottom)` padding.
- **Typography**: Title `headlineLarge` (Inter Bold), helper `bodyMedium` (Outfit Regular).

### Height Layout Pattern (two-column)

- **Goal**: Keep mascot/title/body centered while anchoring CTAs at the bottom.
- **Structure**:
  - Root `Column` uses `Modifier.fillMaxSize()`.
  - A first inner `Column` with `Modifier.weight(1f).fillMaxWidth()` contains the visual content (mascot, title, body). This column expands to take remaining height.
  - Below it, place the bottom interaction block (dots/CTA/secondary link). It stays at the bottom and "squishes" only if space is tight.
- **Why**: Guarantees consistent bottom-aligned actions across devices, while keeping the hero area vertically balanced.

Code references to mirror:
- Image: `modifier = Modifier.fillMaxWidth().height(320.dp), contentScale = ContentScale.Fit`
- Pager padding: `Modifier.padding(horizontal = 24.dp)` and bottom `navigationBars` insets
- Text styles: `headlineLarge`, `bodyLarge`, `labelLarge`
- Button: `Modifier.fillMaxWidth().height(56.dp)`, `ButtonDefaults.buttonColors(containerColor = colorScheme.primary)`
