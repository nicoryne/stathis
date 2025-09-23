# Stathis

**Stathis** is an AI-powered physical education platform designed to revolutionize learning through motion recognition, real-time health vitals tracking, and gamification. The platform consists of three main components: a mobile app for students, a web dashboard for teachers, and a backend API server.

## 📂 Repository Contents Overview

This repository contains the source code, documentation, and necessary resources for Stathis. The key directories and files include:

- `/backend` - Spring Boot Java backend API server with PostgreSQL database.
- `/mobile` - Android Kotlin mobile application for students.
- `/web` - Next.js web dashboard for teachers and administrators.
- `README.md` - Project overview and setup instructions.

## 👥 Members

| Full Name                     | GitHub Profile                                        |
| ----------------------------- | ----------------------------------------------------- |
| **Leones**, Michael Harry P.  | [Saiiph](https://github.com/Saiiph)                   |
| **Matunog**, Margaret Anne C. | [marginggg](https://github.com/margamatunog)          |
| **Porter**, Nicolo Ryne A.    | [nicoryne](https://github.com/nicoryne)               |
| **Quijote**, John Kenny C.    | [mnemosyneiscool](https://github.com/mnemosyneiscool) |
| **San Diego**, Gabe Jeremy R. | [gabejeremy](https://github.com/gabejeremy)           |

## 🚀 Features

### Mobile App (Students)
- 🤖 **AI-Powered Posture Tracking** – Real-time posture monitoring and correction using ONNX models
- ⌚ **Health Connect Integration** – Syncs with Android Health Connect for continuous vitals tracking
- 📱 **Camera-Based Motion Detection** – Uses CameraX and ML Kit for pose detection during exercises
- 🎮 **Gamified Learning** – Interactive challenges, achievements, and progress tracking
- 📊 **Real-time Vitals Monitoring** – Heart rate and oxygen saturation tracking during exercises
- 🏫 **Classroom Management** – Join classrooms, view assignments, and track progress
- 🔐 **Biometric Authentication** – Secure login using fingerprint/face recognition

### Web Dashboard (Teachers)
- 👥 **Classroom Management** – Create and manage multiple classrooms with student enrollment
- 📈 **Real-time Monitoring** – Live vitals tracking of students during exercise sessions
- 📋 **Task & Template Creation** – Create exercises, lessons, and quizzes with customizable templates
- 📊 **Analytics & Reporting** – Comprehensive student progress analytics and performance reports
- 🎯 **Student Progress Tracking** – Individual and class-wide progress monitoring
- 🔔 **Alert System** – Health alerts and notifications for student safety

### Backend API
- 🔐 **JWT Authentication** – Secure user authentication and authorization
- 📡 **WebSocket Support** – Real-time communication for vitals streaming
- 🤖 **AI Model Integration** – ONNX runtime for posture analysis
- 📧 **Email Services** – User verification and notification system
- 🗄️ **PostgreSQL Database** – Robust data storage and management
- 📚 **RESTful APIs** – Comprehensive API endpoints for all platform features

## 🏗️ Architecture Overview

Stathis follows a three-tier architecture pattern:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Mobile App    │    │   Web Dashboard │    │  Backend API    │
│   (Students)    │    │   (Teachers)    │    │   (Spring Boot) │
│                 │    │                 │    │                 │
│ • Android/Kotlin│    │ • Next.js/React │    │ • Java/Spring   │
│ • Jetpack Compose│    │ • TypeScript    │    │ • PostgreSQL    │
│ • CameraX/ML Kit│    │ • Tailwind CSS  │    │ • JWT Auth      │
│ • Health Connect│    │ • ShadCN/UI     │    │ • WebSocket     │
│ • Biometric Auth│    │ • TanStack Query│    │ • ONNX Runtime  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    │    Database     │
                    └─────────────────┘
```

### Key Technologies
- **Frontend (Web)**: Next.js 15, React 19, TypeScript, Tailwind CSS, ShadCN/UI
- **Mobile**: Android (Kotlin), Jetpack Compose, CameraX, ML Kit, Health Connect
- **Backend**: Spring Boot 3.4, Java 17, PostgreSQL, JWT, WebSocket
- **AI/ML**: ONNX Runtime, Pose Detection Models
- **Authentication**: JWT with Spring Security
- **Real-time Communication**: WebSocket for vitals streaming

## 🌍 Branching

We follow the Gitflow workflow:

- `main` - Stable production-ready branch.
- `dev` - Latest development updates.
- `feature/{feature-name}` - Feature branches for new functionalities.
- `hotfix/{issue-name}` - Quick fixes for critical bugs.
- `bugfix/{issue-name}` - Bug fixes and minor patches.

## 🛠️ Installation Guide

> **Prerequisites:**
>
> - Node.js 18+ & npm installed
> - Java 17+ and Maven 3.6+
> - PostgreSQL 13+ database
> - Git for version control
> - Android Studio (for mobile development)
> - Android SDK API 30+ (for mobile development)

### Steps

1. Clone the repository:

   ```sh
   git clone https://github.com/nicoryne/stathis.git
   cd stathis
   ```

2. **Backend Setup:**

   ```sh
   cd backend/stathis
   mvn clean install
   ```

   - Configure database connection in `src/main/resources/application.properties`
   - Update PostgreSQL connection details
   - Run the application: `mvn spring-boot:run`

3. **Web Dashboard Setup:**

   ```sh
   cd web/stathis-web
   npm install
   ```

   - Copy environment variables: `cp .env.example .env`
   - Update the `.env` file with backend API URL and other credentials
   - Run the development server: `npm run dev`
   - Open `http://localhost:3000` in your browser

4. **Mobile App Setup:**

   ```sh
   cd mobile/stathis-mobile
   ```

   - Open the project in Android Studio
   - Configure the backend API URL in the app's configuration
   - Build and run on an Android device or emulator

### Environment Configuration

Each component requires specific environment variables:

- **Backend**: Database connection, JWT secrets, email configuration
- **Web**: Backend API URL, authentication settings
- **Mobile**: Backend API URL, Health Connect permissions

## ⚙️ Development Configurations

### Backend (Spring Boot)
- **Framework:** Spring Boot 3.4.5 with Java 17
- **Database:** PostgreSQL with JPA/Hibernate
- **Security:** Spring Security with JWT authentication
- **Real-time:** WebSocket for vitals streaming
- **AI/ML:** ONNX Runtime for posture analysis
- **Documentation:** SpringDoc OpenAPI 3
- **Build Tool:** Maven

### Web Dashboard (Next.js)
- **Framework:** Next.js 15 with React 19
- **Language:** TypeScript
- **Styling:** Tailwind CSS 4 with ShadCN/UI components
- **State Management:** TanStack Query for server state
- **Forms:** React Hook Form with Zod validation
- **Charts:** Recharts for data visualization
- **Build Tool:** npm

### Mobile App (Android)
- **Language:** Kotlin with Jetpack Compose
- **Architecture:** MVVM with Dagger Hilt dependency injection
- **UI:** Material 3 Design with Jetpack Compose
- **Camera:** CameraX for pose detection
- **ML:** ML Kit for pose detection and analysis
- **Health:** Android Health Connect for vitals data
- **Networking:** Retrofit with OkHttp
- **Build Tool:** Gradle with Kotlin DSL

## 📚 API Documentation

The backend provides comprehensive RESTful APIs and WebSocket endpoints:

### REST APIs
- **Authentication**: User registration, login, password reset
- **User Management**: Profile management, user roles
- **Classroom Management**: Create, join, and manage classrooms
- **Task Management**: Create and manage exercises, lessons, and quizzes
- **Progress Tracking**: Student progress and analytics
- **Vitals Monitoring**: Health data collection and analysis
- **Posture Analysis**: AI-powered posture detection and feedback

### WebSocket Endpoints
- **Real-time Vitals**: Live health data streaming from mobile to web dashboard
- **Exercise Monitoring**: Real-time exercise session tracking

### API Documentation
- **Swagger UI**: Available at `http://localhost:8080/swagger-ui.html` when backend is running
- **OpenAPI 3**: Complete API specification in JSON/YAML format

## 🐝 Contributing

We welcome contributions! Please give us a message if you'd like to contribute!

### Development Workflow
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes and test thoroughly
4. Commit your changes: `git commit -m 'Add your feature'`
5. Push to your branch: `git push origin feature/your-feature-name`
6. Create a Pull Request

### Code Standards
- Follow the existing code style and conventions
- Write comprehensive tests for new features
- Update documentation as needed
- Ensure all components work together properly
