'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { serverApiClient } from '@/lib/api/server-client';

interface ApiResponse {
  endpoint: string;
  status: number;
  data: any;
  error: any;
}

export function ApiDebugger() {
  const [responses, setResponses] = useState<ApiResponse[]>([]);
  const [loading, setLoading] = useState(false);
  
  // Test task ID - update this if you have a real task ID
  const TEST_TASK_ID = 'TASK-123';
  // Test classroom ID
  const TEST_CLASSROOM_ID = 'ROOM-25-130';
  
  const runTest = async () => {
    setLoading(true);
    const results: ApiResponse[] = [];
    
    try {
      // Test endpoints - in sequence
      const endpoints = [
        `/scores/task/${TEST_TASK_ID}`,
        `/scores`,
        `/scores/student/STUDENT-123`,
        `/classroom-students/${TEST_CLASSROOM_ID}` // Control endpoint
      ];
      
      for (const endpoint of endpoints) {
        try {
          console.log(`Testing endpoint: ${endpoint}`);
          const response = await serverApiClient.get(endpoint);
          
          results.push({
            endpoint,
            status: response.status,
            data: response.data,
            error: response.error
          });
          
          console.log(`Response for ${endpoint}:`, response);
        } catch (error) {
          console.error(`Error testing ${endpoint}:`, error);
          results.push({
            endpoint,
            status: 0,
            data: null,
            error: error instanceof Error ? error.message : String(error)
          });
        }
      }
    } finally {
      setResponses(results);
      setLoading(false);
    }
  };
  
  return (
    <Card className="w-full mt-6">
      <CardHeader>
        <CardTitle>API Endpoint Debugger</CardTitle>
      </CardHeader>
      <CardContent>
        <Button 
          onClick={runTest} 
          disabled={loading}
          className="mb-4"
        >
          {loading ? 'Testing APIs...' : 'Test Score Endpoints'}
        </Button>
        
        {responses.length > 0 && (
          <div className="space-y-4 mt-4">
            <h3 className="text-lg font-medium">Test Results:</h3>
            {responses.map((res, index) => (
              <div key={index} className="border p-4 rounded-md">
                <div className="flex justify-between">
                  <span className="font-medium">{res.endpoint}</span>
                  <span className={`px-2 py-1 rounded text-xs ${
                    res.status >= 200 && res.status < 300 ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                  }`}>
                    {res.status ? `Status: ${res.status}` : 'Failed'}
                  </span>
                </div>
                
                {res.error && (
                  <div className="mt-2 text-red-500 text-sm">
                    Error: {typeof res.error === 'string' ? res.error : JSON.stringify(res.error)}
                  </div>
                )}
                
                <div className="mt-2 text-sm">
                  <pre className="bg-gray-100 p-2 rounded overflow-auto max-h-[150px]">
                    {JSON.stringify(res.data, null, 2) || 'No data'}
                  </pre>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
