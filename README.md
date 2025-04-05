# Stathis

**Stathis** is an AI-powered mobile application designed to revolutionize physical education by
integrating motion recognition, real-time health vitals tracking, and gamification

## 📂 Repository Contents Overview

This repository contains the source code, documentation, and necessary resources for Stathis. The key directories and files include:

- `/mobile` - Android Kotlin mobile development files.
- `/web` - Next.js web development files.
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

- 🤖 **AI-Powered Posture Tracking** – Monitors and corrects your posture in real time using AI.

- ⌚ **Smartwatch Integration** – Syncs seamlessly with your smartwatch for continuous tracking.

- 🎮 **Gamified Learning** – Makes learning fun with interactive and rewarding challenges.

- 📈 **Vitals Tracking** – Keeps an eye on key health metrics like heart rate and activity.

- 🧑‍🏫 **Student-Teacher Learning** – Supports guided learning with roles for both students and teachers.

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
> - Node.js & npm installed
> - Supabase account and database setup
> - Git for version control
> - Android Studio (for mobile development)

### Steps:

1. Clone the repository:
   ```sh
   git clone https://github.com/your-repository/sarismart.git
   cd sarismart
   ```
2. Install dependencies for web:
   ```sh
   cd web
   npm install
   ```
3. Install dependencies for mobile:
   ```sh
   cd ../mobile
   ./gradlew build
   ```
4. Configure environment variables:
   ```sh
   cp .env.example .env
   ```
   - Update the `.env` file with necessary API keys and Supabase credentials.
5. Run the web development server:
   ```sh
   npm run dev
   ```
6. Open `http://localhost:3000` in your browser.

## ⚙️ Development Configurations

- **Frontend:** Next.js (React) with Tailwind CSS.
- **Mobile:** Kotlin (Android Native Development).
- **Backend:** Supabase (PostgreSQL, Auth, Storage).
- **Authentication:** Supabase Auth / JWT.

## 🐝 Contributing

We welcome contributions! Please give us a message if you'd like to contribute!
