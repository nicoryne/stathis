'use server';

import { apiClient } from '@/lib/api/client';

/**
 * Generic API service for handling various backend endpoints
 * This replaces direct Supabase database access with calls to your Spring Boot backend
 */
export const apiService = {
  /**
   * Fetch data from backend
   * @param endpoint The API endpoint to fetch data from
   */
  async fetch<T>(endpoint: string): Promise<T> {
    const { data, error } = await apiClient.get<T>(endpoint);
    
    if (error) {
      throw new Error(error);
    }
    
    return data as T;
  },
  
  /**
   * Create a new resource
   * @param endpoint The API endpoint
   * @param payload The data to send
   */
  async create<T>(endpoint: string, payload: any): Promise<T> {
    const { data, error } = await apiClient.post<T>(endpoint, payload);
    
    if (error) {
      throw new Error(error);
    }
    
    return data as T;
  },
  
  /**
   * Update an existing resource
   * @param endpoint The API endpoint
   * @param payload The data to update
   */
  async update<T>(endpoint: string, payload: any): Promise<T> {
    const { data, error } = await apiClient.put<T>(endpoint, payload);
    
    if (error) {
      throw new Error(error);
    }
    
    return data as T;
  },
  
  /**
   * Delete a resource
   * @param endpoint The API endpoint
   */
  async delete(endpoint: string): Promise<void> {
    const { error } = await apiClient.delete(endpoint);
    
    if (error) {
      throw new Error(error);
    }
  }
};
