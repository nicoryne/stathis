# Decision Log

## Technical Decisions
[2025-05-23 18:11:00] - Decision: Migrate from Supabase to PostgreSQL
Rationale: Not explicitly stated, but likely for more direct database control or specific PostgreSQL features
Implications: Need to update backend and frontend integrations

[2025-05-24 23:59:00] - Decision: Use Spring Boot for backend implementation
Rationale: Robust framework with strong support for RESTful APIs, security, and database access
Implications: Need to follow Spring Boot conventions and structure

[2025-05-25 18:45:52] - Decision: Use hasRole('TEACHER') for classroom management authorization
Rationale: Aligns with Spring Security role-based authorization and the JWT token structure
Implications: Need to ensure consistent role prefixing in JwtAuthenticationFilter and security annotations

## Architecture Decisions
[2025-05-23 18:11:00] - Decision: Use local backend with deployed database
Rationale: Development approach for local testing with production data
Implications: Need to configure backend to connect to remote database while frontend connects to local backend

[2025-05-24 23:59:00] - Decision: Organize backend by domain (auth, classroom, task)
Rationale: Domain-driven design provides clear separation of concerns and better maintainability
Implications: Each domain has its own controllers, services, repositories, and entities

[2025-05-24 23:59:00] - Decision: Implement JWT-based authentication
Rationale: Provides stateless, secure authentication for the RESTful API
Implications: Frontend must manage token storage and include in requests

## Implementation Decisions
[2025-05-23 18:11:00] - Decision: Configure database connection through environment variables
Rationale: Standard practice for separating configuration from code
Implications: Need to update .env file with correct PostgreSQL connection parameters

[2025-05-24 23:59:00] - Decision: Use DTO pattern for API contracts
Rationale: Separates API representation from domain entities for better encapsulation
Implications: Need to maintain mapping between DTOs and entities

[2025-05-24 23:59:00] - Decision: Configure database connection pooling with HikariCP
Rationale: Efficient connection management for better performance
Implications: May need to tune connection pool parameters (observed connection closed errors)
