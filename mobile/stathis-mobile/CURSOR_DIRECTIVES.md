# CURSOR DIRECTIVES

## 🎯 Project Overview
This is the **Stathis Mobile App** - a learning application with Duolingo-inspired UI design. Current focus areas include a simplified 3-tab home (Learn, Practice, Profile), updated theming, and mascot integration.

## 📁 Project Structure
```
stathis-mobile/
├── app/src/main/java/citu/edu/stathis/mobile/
│   ├── core/
│   │   ├── theme/           # App theme (Material 3 color schemes, provider)
│   │   └── ui/              # Reusable UI components (as added)
│   ├── features/
│   │   ├── auth/           # Login/Register (DO NOT MODIFY)
│   │   ├── home/           # App shell, bottom nav, and home screens
│   │   ├── profile/        # User profile
│   │   ├── exercise/       # Exercise tracking
│   │   ├── tasks/          # Task management
│   │   ├── vitals/         # Health monitoring
│   │   └── classroom/      # Classroom management
│   └── di/                 # Dependency injection
├── BRANDING_GUIDE.md       # Design system reference
├── UI_ANALYSIS.md          # UI architecture & decisions
├── HOW-TO-ADD-MASCOT.md    # Mascot assets & integration guide
└── CURSOR_DIRECTIVES.md    # This file
```

## 🚫 CRITICAL: Files NOT to Modify During UI Changes

### Architecture & Data Layers (DO NOT TOUCH)
- `app/src/main/java/citu/edu/stathis/mobile/features/*/data/` (data layers)
- `app/src/main/java/citu/edu/stathis/mobile/features/*/domain/` (domain layers)
- `app/src/main/java/citu/edu/stathis/mobile/features/*/di/` (DI modules)
- Authentication flow (if present) should not be altered during UI-only changes

### Backend Reference Location
If working on integration, coordinate with backend. Use backend models to inform UI data handling.

## 🎨 UI Development Guidelines

### Design System Reference
**ALWAYS CONSULT**: `BRANDING_GUIDE.md`, `UI_ANALYSIS.md`, and `HOW-TO-ADD-MASCOT.md`
- Contains complete design tokens, colors, typography
- Duolingo-inspired design principles
- Component usage examples
- Mascot integration patterns

### Core UI Files (Safe to Modify)
- `app/src/main/java/citu/edu/stathis/mobile/core/theme/AppThemeWithProvider.kt`
- `app/src/main/java/citu/edu/stathis/mobile/core/theme/Theme.kt`
- `app/src/main/java/citu/edu/stathis/mobile/core/theme/ThemeViewModel.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/home/AppShell.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/home/HomeNavHost.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/home/HomeBottomNavigation.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/home/HomeNavigationItem.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/home/ProfileScreen.kt`
- Mascot UI: Use drawable XML files directly with Image composable (see Mascot Integration section below)

## 🎭 Mascot Integration

### Mascot File Locations
**Place mascot assets in** (see also `HOW-TO-ADD-MASCOT.md`):
```
app/src/main/res/
├── drawable/               # Vector XML mascot files (PREFERRED)
│   ├── mascot_celebrate.xml
│   ├── mascot_cheer.xml
│   ├── mascot_muscles.xml
│   ├── mascot_teacher.xml
│   └── [add more as needed]
├── raw/                   # Lottie animations (optional)
│   ├── mascot_wave.json
│   ├── mascot_celebration.json
│   ├── mascot_encouragement.json
│   └── mascot_sleep.json
└── values/
    └── strings.xml         # Mascot speech text
```

### Mascot Usage Pattern
**Use drawable XML mascots directly with Image composable:**
```kotlin
// Choose mascot based on context/state
val mascotDrawableRes = when {
    state.errorMessage != null -> R.drawable.mascot_teacher
    state.isSubmitting -> R.drawable.mascot_muscles
    userCompletedTask -> R.drawable.mascot_celebrate
    else -> R.drawable.mascot_cheer
}

// Display mascot
Image(
    painter = painterResource(id = mascotDrawableRes),
    contentDescription = null,
    modifier = Modifier
        .fillMaxWidth()
        .height(320.dp),
    contentScale = ContentScale.Fit
)
```

### Speech Bubbles
**Add speech bubbles using Card composable:**
```kotlin
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )
) {
    Text(
        text = speechText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(24.dp)
    )
}
```

### Mascot Integration Points
- **Learn**: Welcome greeting and daily motivation
- **Practice**: Real-time encouragement and success celebrations
- **Profile**: Customization/empty state guidance

## 🎯 UI Development Workflow

### Before Making Changes
1. **Read Backend**: Check `../backend/` for API structure
2. **Read Branding Guide**: Consult `BRANDING_GUIDE.md`
3. **Read UI Analysis**: Review `UI_ANALYSIS.md`
4. **Check Theme**: Use `StathisTheme` and Material 3 tokens

### Design Principles (Duolingo-Inspired)
- **Simplicity First**: One main action per screen
- **Large CTAs**: Bold, pill-shaped buttons
- **Generous Spacing**: 16dp-24dp padding
- **Mascot Integration**: Emotional reactions and guidance
- **Gamification**: Progress bars, achievements, celebrations

### Color Usage
- **Primary**: Purple (`#9334EA`) - Main actions, brand
- **Secondary**: Teal (`#25ACA4`) - Success, achievements, health
- **Accent**: Light Purple (`#F0DBFF`) - Backgrounds
- **Success**: Green (`#4CAF50`) - Completions
- **Warning**: Orange (`#FFA000`) - Caution
- **Error**: Red (`#F44336`) - Errors

### Typography
- **Display**: Outfit (headlines, mascot speech)
- **Body**: Manrope (descriptions, content)
- **Hierarchy**: Large bold for CTAs, medium for body, small for secondary

## 🔧 Technical Guidelines

### Navigation Structure
- **3-Tab Navigation**: Learn, Practice, Profile
- **Start Destination**: `Practice`
- **Bottom Navigation**: `HomeBottomNavigation` with Material 3 `NavigationBar`

### Component Architecture
- **Reusable Components**: Place in `core/ui/` (or feature-local as needed)
- **Screen-Specific**: Place in `features/*/`
- **Theme**: Use `AppThemeWithProvider`/`StathisTheme` and `MaterialTheme`

### State Management
- **UI State**: Use Compose state
- **Theme**: Provided via `ThemeViewModel` and `ThemeProvider`
- **Data Integration**: Connect to existing ViewModels
- **Mascot State**: Derive from user progress and context

## 📱 Screen-Specific Guidelines

### Learn
- **Focus**: Guided learning entry points
- **Mascot**: Welcome greeting and streak info
- **Quick Actions**: Start practice

### Practice
- **Focus**: Exercises and drills
- **Mascot**: Real-time encouragement and celebrations

### Profile
- **Minimal Design**: Essential info and session actions
- **Settings**: Access via top-right icon in `Profile`

## 🚨 Common Mistakes to Avoid

### DO NOT:
- Modify backend integration files
- Change authentication flow
- Alter data models or repositories
- Modify dependency injection
- Change API service interfaces
- Remove mascot integration points

### DO:
- Use design system tokens consistently
- Follow Duolingo-inspired patterns
- Integrate mascot throughout the app
- Maintain 3-tab navigation structure
- Keep UI simple and focused
- Use proper spacing and typography

## 🔍 Debugging & Testing

### UI Issues
- Verify theme usage via `AppThemeWithProvider`/`StathisTheme`
- Verify mascot component state management
- Ensure proper navigation routing (`HomeNavHost`)
- Test on different screen sizes

### Integration Issues
- Verify backend API compatibility
- Check data flow from ViewModels
- Ensure proper state management
- Test navigation between screens

## 📚 Reference Documents

1. **BRANDING_GUIDE.md** - Design tokens and visuals
2. **UI_ANALYSIS.md** - Architecture decisions and implementation
3. **HOW-TO-ADD-MASCOT.md** - Mascot assets and component guidance
4. **Theme** - `core/theme/AppThemeWithProvider.kt`, `core/theme/Theme.kt`
5. **Home Navigation** - `features/home/AppShell.kt`, `HomeNavHost.kt`, `HomeBottomNavigation.kt`, `HomeNavigationItem.kt`

## 🎯 Quick Reference

### For UI Changes:
1. Read `BRANDING_GUIDE.md`
2. Check `UI_ANALYSIS.md`
3. Wrap screens in `StathisTheme` (via `AppThemeWithProvider`)
4. Integrate mascot per `HOW-TO-ADD-MASCOT.md`
5. Follow Duolingo-inspired patterns

### For Backend Integration:
1. Coordinate with backend as needed
2. Don't modify data/domain/DI layers
3. Use existing ViewModels
4. Maintain API compatibility

### For Mascot Integration:
1. Place assets in `app/src/main/res/`
2. Implement/Use mascot component per `HOW-TO-ADD-MASCOT.md`
3. Configure emotional states
4. Add speech bubble text

---

**Remember**: This is a learning app with a mascot companion. Keep it simple, engaging, and fun! 🎭✨


