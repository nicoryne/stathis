# Active Context

## Current Focus
[2025-05-23 18:26:00] - Completed web API integration to use local backend with deployed PostgreSQL database
[2025-05-24 23:59:00] - Analyzing backend architecture to understand data flow and troubleshoot classroom API issues
[2025-05-25 18:45:52] - Implementing classroom management UI with classroom creation and detailed view functionality
[2025-05-26 23:35:33] - Fixing UI issues in task creation and display components

## Recent Changes
[2025-05-23 18:11:00] - Migration from Supabase to PostgreSQL database
[2025-05-23 18:18:00] - Created API client for frontend to connect to local backend
[2025-05-23 18:18:00] - Created authentication services using local backend instead of Supabase
[2025-05-23 18:18:00] - Created migration guide with instructions
[2025-05-25 18:45:52] - Fixed classroom creation functionality by updating DTOs to match backend
[2025-05-25 18:45:52] - Implemented classroom detail page with dynamic routing
[2025-05-23 18:26:00] - Updated all application components to use new API services
[2025-05-23 18:26:00] - Replaced Supabase middleware with custom API middleware
[2025-05-23 18:26:00] - Updated OAuth callback handler
[2025-05-23 18:26:00] - Added Supabase directory to .gitignore as deprecated
[2025-05-24 23:59:00] - Backend updates to Classroom entity and DTOs to fix classroom API issues (pulled from dev branch)
[2025-05-24 23:59:00] - Changes to ClassroomBodyDTO, Classroom entity, ClassroomStudents entity, ClassroomService, and TaskBodyDTO
[2025-05-26 23:35:33] - Fixed task creation form to correctly label "Max Attempts" instead of "Points"
[2025-05-26 23:35:33] - Fixed date formatting issue in task list display by improving error handling and using correct property names

## Open Questions/Issues
[2025-05-23 18:26:00] - Need to verify frontend properly connects to local backend
[2025-05-23 18:26:00] - Need to ensure local backend connects to remote PostgreSQL database
[2025-05-23 18:26:00] - Consider removing Supabase packages from package.json if no longer needed
[2025-05-24 23:59:00] - Debugging Classroom API with issues in GET and POST operations
[2025-05-24 23:59:00] - Database connection issues observed ("connection has been closed", potential max lifetime configuration issue)
