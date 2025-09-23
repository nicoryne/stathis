# Vitals Webhook Feature

## Overview
The Vitals Webhook feature allows students' real-time vitals data to be transmitted to the teacher's dashboard during exercise sessions. This enables teachers to monitor students' health metrics in real-time, ensuring both safety and improved training outcomes.

## Architecture
The feature uses a WebSocket connection to transmit vitals data from the mobile app to the backend server, which then forwards it to the teacher's dashboard.

### Key Components

1. **VitalsWebSocketClient** - Core component that manages WebSocket connection and data transmission
2. **VitalsWebSocketDTO** - Data Transfer Object for formatting vitals data for transmission
3. **HealthConnectManager** - Retrieves vitals data from the device's health sensors
4. **ExerciseViewModel** - Integrates the WebSocket client into the exercise flow
5. **MonitorRealTimeVitalsUseCase** - Handles the business logic for monitoring and retrieving vitals

## Implementation Details

### Data Flow
1. When a student starts an exercise, the ExerciseViewModel initializes the VitalsWebSocketClient
2. The client connects to the WebSocket server with the student's auth token
3. The HealthConnectManager retrieves heart rate and oxygen saturation from the device
4. Data is packaged into a VitalsWebSocketDTO and sent over the WebSocket
5. The backend receives the data and forwards it to the teacher's dashboard
6. The student sees the connection status on their exercise screen

### Dependency Injection
All components are integrated using Dagger Hilt for dependency injection:
- VitalsModule provides the singleton instances of VitalsWebSocketClient and HealthConnectManager
- VitalsViewModelModule provides scoped instances for view models

### WebSocket Protocol
The WebSocket connection sends JSON-formatted vitals data with:
- Student ID
- Classroom ID
- Task ID
- Heart rate
- Oxygen saturation
- Timestamp
- Activity phase (pre/during/post)

### UI Integration
The feature includes UI elements to show:
- Connection status (Connected, Connecting, Error, Disconnected)
- Current heart rate

## Security Considerations
- All WebSocket connections are authenticated using JWT tokens
- No personally identifiable information beyond the student ID is transmitted
- Connection uses secure WebSocket protocol (wss://)

## Error Handling
The implementation includes robust error handling:
- Automatic reconnection attempts on connection failure
- Graceful degradation when health sensors are unavailable
- Clear user feedback when connection issues occur

## Testing
Test the feature by:
1. Ensuring the WebSocket connection is established during exercise
2. Verifying data appears on the teacher's dashboard
3. Testing reconnection behavior when network is interrupted
4. Checking behavior when health sensors are unavailable
