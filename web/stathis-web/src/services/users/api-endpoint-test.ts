'use client';

import { serverApiClient } from '@/lib/api/server-client';

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
