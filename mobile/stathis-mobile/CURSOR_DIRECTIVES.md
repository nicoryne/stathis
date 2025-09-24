# CURSOR DIRECTIVES

## 🎯 Project Overview
This is the **Stathis Mobile App** - a learning application with Duolingo-inspired UI design. The app features classroom management, exercise tracking, health monitoring, and an interactive mascot system.

## 📁 Project Structure
```
stathis-mobile/
├── app/src/main/java/citu/edu/stathis/mobile/
│   ├── core/
│   │   ├── theme/           # Design system & branding
│   │   ├── ui/components/   # Reusable UI components
│   │   └── navigation/      # App navigation
│   ├── features/
│   │   ├── auth/           # Login/Register (DO NOT MODIFY)
│   │   ├── home/           # Main navigation & screens
│   │   ├── dashboard/      # Learn hub (main screen)
│   │   ├── progress/       # Achievements & health
│   │   ├── profile/        # User profile
│   │   ├── exercise/       # Exercise tracking
│   │   ├── tasks/          # Task management
│   │   ├── vitals/         # Health monitoring
│   │   └── classroom/      # Classroom management
│   └── di/                 # Dependency injection
├── BRANDING_GUIDE.md       # Design system reference
├── UI_ANALYSIS.md          # UI architecture & decisions
└── CURSOR_DIRECTIVES.md    # This file
```

## 🚫 CRITICAL: Files NOT to Modify During UI Changes

### Backend Integration Files (DO NOT TOUCH)
- `app/src/main/java/citu/edu/stathis/mobile/core/navigation/CoreNavigationController.kt`
- `app/src/main/java/citu/edu/stathis/mobile/core/navigation/CoreNavigationViewModel.kt`
- `app/src/main/java/citu/edu/stathis/mobile/di/` (entire directory)
- `app/src/main/java/citu/edu/stathis/mobile/features/auth/` (entire directory)
- `app/src/main/java/citu/edu/stathis/mobile/features/*/data/` (all data layers)
- `app/src/main/java/citu/edu/stathis/mobile/features/*/domain/` (all domain layers)
- `app/src/main/java/citu/edu/stathis/mobile/features/*/di/` (all DI modules)

### Backend Reference Location
**ALWAYS CHECK BACKEND FIRST**: `../backend/stathis/src/main/java/edu/cit/stathis/`
- Contains all API endpoints, services, and data models
- Reference for understanding data structures and business logic
- Use backend models to inform UI data handling

## 🎨 UI Development Guidelines

### Design System Reference
**ALWAYS CONSULT**: `BRANDING_GUIDE.md` and `UI_ANALYSIS.md`
- Contains complete design tokens, colors, typography
- Duolingo-inspired design principles
- Component usage examples
- Mascot integration patterns

### Core Design Files (Safe to Modify)
- `app/src/main/java/citu/edu/stathis/mobile/core/theme/StathisDesignSystem.kt`
- `app/src/main/java/citu/edu/stathis/mobile/core/ui/components/MascotAvatar.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/home/HomeScreen.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/home/HomeNavigationItem.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/home/HomeBottomNavigation.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/dashboard/ui/DashboardScreen.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/progress/ui/ProgressScreen.kt`
- `app/src/main/java/citu/edu/stathis/mobile/features/profile/ui/ProfileScreen.kt`

## 🎭 Mascot Integration

### Mascot File Locations
**Place mascot assets in**:
```
app/src/main/res/
├── drawable/               # Static mascot images
│   ├── mascot_happy.png
│   ├── mascot_celebrating.png
│   ├── mascot_encouraging.png
│   ├── mascot_concerned.png
│   ├── mascot_sleeping.png
│   └── mascot_neutral.png
├── raw/                   # Lottie animations
│   ├── mascot_wave.json
│   ├── mascot_celebration.json
│   ├── mascot_encouragement.json
│   └── mascot_sleep.json
└── values/
    └── strings.xml         # Mascot speech text
```

### Mascot Component Usage
The `MascotAvatar.kt` component supports:
- **Static Images**: Place in `drawable/` folder
- **Lottie Animations**: Place in `raw/` folder
- **Speech Bubbles**: Configured in component
- **Emotional States**: 6 different states with automatic transitions

### Mascot Integration Points
- **Learn Hub**: Welcome greeting and daily motivation
- **Progress Screen**: Achievement celebrations
- **Profile Screen**: Customization options
- **Exercise Screen**: Real-time encouragement
- **Task Completion**: Success celebrations

## 🎯 UI Development Workflow

### Before Making Changes
1. **Read Backend**: Check `../backend/` for API structure
2. **Read Branding Guide**: Consult `BRANDING_GUIDE.md`
3. **Read UI Analysis**: Review `UI_ANALYSIS.md`
4. **Check Design System**: Use `StathisDesignSystem.kt` tokens

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
- **3-Tab Navigation**: Learn, Progress, Profile
- **Legacy Compatibility**: Old routes redirect to new structure
- **Bottom Navigation**: Uses `StathisColors` and `StathisSpacing`

### Component Architecture
- **Reusable Components**: Place in `core/ui/components/`
- **Screen-Specific**: Place in `features/*/ui/`
- **Design Tokens**: Always use `StathisDesignSystem.kt`

### State Management
- **UI State**: Use Compose state management
- **Data Integration**: Connect to existing ViewModels
- **Mascot State**: Automatic based on user progress

## 📱 Screen-Specific Guidelines

### Learn Hub (DashboardScreen)
- **Focus**: Single task/exercise per day
- **Mascot**: Welcome greeting with level/streak info
- **Classroom Selection**: Simplified enrollment
- **Quick Actions**: Exercise and Tasks buttons

### Progress Screen
- **Level Display**: Circular badge with XP progress
- **Streak Counter**: Fire icon with day count
- **Achievements**: Visual badges with unlock states
- **Health Summary**: Mascot health coaching

### Profile Screen
- **Minimal Design**: Essential info only
- **Mascot Customization**: Preview and options
- **Classroom Overview**: Joined classrooms list
- **Settings**: Edit profile and logout

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
- Check `StathisDesignSystem.kt` for correct tokens
- Verify mascot component state management
- Ensure proper navigation routing
- Test on different screen sizes

### Integration Issues
- Verify backend API compatibility
- Check data flow from ViewModels
- Ensure proper state management
- Test navigation between screens

## 📚 Reference Documents

1. **BRANDING_GUIDE.md** - Complete design system
2. **UI_ANALYSIS.md** - Architecture decisions and implementation
3. **Backend API** - `../backend/stathis/src/main/java/edu/cit/stathis/`
4. **Design System** - `StathisDesignSystem.kt`
5. **Mascot Component** - `MascotAvatar.kt`

## 🎯 Quick Reference

### For UI Changes:
1. Read `BRANDING_GUIDE.md`
2. Check `UI_ANALYSIS.md`
3. Use `StathisDesignSystem.kt` tokens
4. Integrate mascot where appropriate
5. Follow Duolingo-inspired patterns

### For Backend Integration:
1. Check `../backend/` structure
2. Don't modify data/domain layers
3. Use existing ViewModels
4. Maintain API compatibility

### For Mascot Integration:
1. Place assets in `app/src/main/res/`
2. Use `MascotAvatar.kt` component
3. Configure emotional states
4. Add speech bubble text

---

**Remember**: This is a learning app with a mascot companion. Keep it simple, engaging, and fun! 🎭✨


