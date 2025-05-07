import * as React from "react"

export interface ToastProps {
  title?: string
  description?: string
  variant?: "default" | "destructive"
}

type ToastFunction = (props: ToastProps) => void

// Simplified toast implementation
export const toast: ToastFunction = ({ title, description, variant = "default" }) => {
  // In a real implementation, this would add toasts to a state
  console.log(`Toast: ${variant} - ${title} - ${description}`);
}

export function useToast() {
  return {
    toast,
  }
} 