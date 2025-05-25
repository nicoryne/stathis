# Product Context

## Project Overview
[2025-05-23 18:11:00] - Stathis is a web and mobile application that was initially using Supabase but has now migrated to PostgreSQL.
[2025-05-24 23:57:00] - The application follows a microservice-inspired architecture with domain-driven design principles.

## Components
[2025-05-23 18:11:00] - Backend: Spring Boot application with Java 17
[2025-05-24 23:57:00] - Backend follows a layered architecture with controllers, services, repositories, and entities
[2025-05-23 18:11:00] - Web Frontend: Next.js application using React and TypeScript
[2025-05-23 18:11:00] - Mobile App: Not our focus per instructions
[2025-05-23 18:11:00] - Database: PostgreSQL hosted at 188.166.246.153:5432/stathisdb
[2025-05-24 23:57:00] - Connection managed through HikariCP connection pool

## Backend Architecture
[2025-05-24 23:57:00] - Domain Structure: Organized into domain-specific packages (auth, classroom, task)
[2025-05-24 23:57:00] - Each domain has its own controllers, services, repositories, entities, and DTOs
[2025-05-24 23:57:00] - Security: JWT-based authentication with Spring Security
[2025-05-24 23:57:00] - API Documentation: OpenAPI/Swagger integration
[2025-05-24 23:57:00] - WebSocket support for real-time communication

## Organization
[2025-05-23 18:11:00] - Project structure includes separate directories for backend, web, and mobile
[2025-05-24 23:57:00] - Backend follows standard Spring Boot project structure

## Standards
[2025-05-23 18:11:00] - Use local backend with deployed database
[2025-05-23 18:11:00] - Backend configuration via .env file
[2025-05-24 23:57:00] - Environmental configuration using Spring profiles (dev, prod)
[2025-05-24 23:57:00] - RESTful API design principles
[2025-05-24 23:57:00] - DTO pattern for data transfer between layers
