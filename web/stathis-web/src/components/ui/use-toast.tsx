import * as React from "react"

// We're just exporting the toast function here, not the hook
// Components should import useToast directly from toast-provider
import { ToastProvider } from "./toast-provider"
export { ToastProvider }
export type { ToastType } from "./toast-provider"

// Simplified toast function for compatibility
export const toast = (props: {
  title?: string
  description?: string
  variant?: "default" | "destructive" | "success" | "warning" | "info"
  duration?: number
}) => {
  // We're using this approach to avoid breaking existing code
  // Ideally, component code should be updated to use the useToast hook directly
  try {
    // Try to import dynamically to avoid SSR issues
    import("./toast-provider").then(({ useToast }) => {
      try {
        const { addToast } = useToast()
        addToast({
          title: props.title,
          description: props.description,
          variant: props.variant || "default",
          duration: props.duration
        })
      } catch (e) {
        // Fallback to console if hook fails (likely not in provider context)
        console.log(`Toast: ${props.variant} - ${props.title} - ${props.description}`)
      }
    }).catch(err => {
      // Fallback to console if provider not available
      console.log(`Toast: ${props.variant} - ${props.title} - ${props.description}`)
    })
  } catch (e) {
    console.log(`Toast: ${props.variant} - ${props.title} - ${props.description}`)
  }
}

// This is a backward compatibility function
export function useToast() {
  return {
    toast,
  }
} 