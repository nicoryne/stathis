'use client';

import { serverApiClient } from '@/lib/api/server-client';

/**
 * Function to test the aggregated student progress API endpoint
 * @param studentId The student ID to test with
 * @param classroomId Optional classroom ID to filter by
 */
export async function testStudentProgressEndpoint(studentId: string, classroomId?: string) {
  console.log(`Testing student progress API endpoint for student: ${studentId}`);
  
  // Build the URL with optional classroom parameter
  let url = `/v1/student-progress/${studentId}`;
  if (classroomId) {
    url += `?classroomId=${encodeURIComponent(classroomId)}`;
  } else if (studentId.includes('-')) {
    // Try to extract classroom ID from student ID
    const parts = studentId.split('-');
    if (parts.length >= 2) {
      const extractedClassroomId = `${parts[0]}-${parts[1]}`;
      url += `?classroomId=${encodeURIComponent(extractedClassroomId)}`;
      console.log(`Using extracted classroom ID: ${extractedClassroomId}`);
    }
  }
  
  try {
    console.log(`Sending request to: ${url}`);
    const result = await serverApiClient.get(url);
    
    console.log(`Response status: ${result.status}`);
    console.log(`Data is array: ${Array.isArray(result.data)}`);
    console.log(`Number of items: ${Array.isArray(result.data) ? result.data.length : 0}`);
    
    if (Array.isArray(result.data) && result.data.length > 0) {
      // Log the structure of the first item
      const firstItem = result.data[0];
      console.log('Sample item structure:', {
        taskId: firstItem.taskId,
        taskName: firstItem.taskName,
        taskType: firstItem.taskType,
        completed: firstItem.completed,
        score: firstItem.score,
        maxScore: firstItem.maxScore
      });
    }
    
    return {
      success: result.status < 400,
      status: result.status,
      itemCount: Array.isArray(result.data) ? result.data.length : 0,
      data: result.data
    };
  } catch (error) {
    console.error('Error testing student progress endpoint:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : 'Unknown error'
    };
  }
}

/**
 * Function to test multiple API endpoints and find which one works
 */
export async function testUserProfileEndpoints() {
  const endpointsToTry = [
    '/users/profile',
    '/user/profile',
    '/users',
    '/profile',
    '/api/users/profile',
    '/profile/teacher'
  ];
  
  console.log('Testing multiple API endpoints to find the working one');
  
  const results = await Promise.allSettled(
    endpointsToTry.map(async endpoint => {
      try {
        console.log(`Trying endpoint: ${endpoint}`);
        const result = await serverApiClient.get(endpoint);
        return {
          endpoint,
          status: result.status,
          success: result.status < 400,
          data: result.data
        };
      } catch (error) {
        return {
          endpoint,
          success: false,
          error: error instanceof Error ? error.message : 'Unknown error'
        };
      }
    })
  );
  
  console.log('API endpoint test results:', results);
  
  // Find any successful endpoint
  const successfulEndpoint = results.find(
    result => result.status === 'fulfilled' && result.value.success
  );
  
  if (successfulEndpoint && successfulEndpoint.status === 'fulfilled') {
    console.log('Found working endpoint:', successfulEndpoint.value.endpoint);
    return successfulEndpoint.value;
  } else {
    console.error('No working endpoints found');
    return null;
  }
}

/**
 * Function to test different minimal payloads for profile update
 */
export async function testProfileUpdate() {
  const payloadsToTry = [
    { firstName: 'Test', lastName: 'User' },
    { first_name: 'Test', last_name: 'User' },
    { name: 'Test User' }
  ];
  
  console.log('Testing different payloads for profile update');
  
  for (const payload of payloadsToTry) {
    try {
      console.log(`Trying payload:`, payload);
      const result = await serverApiClient.put('/users/profile', payload);
      console.log(`Result for payload ${JSON.stringify(payload)}:`, result);
      
      if (result.status < 400) {
        console.log('Found working payload:', payload);
        return { payload, result };
      }
    } catch (error) {
      console.error(`Error with payload ${JSON.stringify(payload)}:`, error);
    }
  }
  
  console.error('No working payloads found');
  return null;
}
