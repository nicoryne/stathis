"use client"

import * as React from "react"
import { CalendarIcon } from "lucide-react"

import { cn } from "@/lib/utils"

export interface CalendarProps extends React.HTMLAttributes<HTMLDivElement> {
  date?: Date;
  onDateChange?: (date: Date) => void;
  disabled?: boolean;
  min?: Date;
  max?: Date;
}

function formatDate(date: Date): string {
  const day = date.getDate().toString().padStart(2, '0');
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const year = date.getFullYear();
  return `${year}-${month}-${day}`;
}

function Calendar({
  className,
  date = new Date(),
  onDateChange,
  disabled = false,
  min,
  max,
  ...props
}: CalendarProps) {
  // Create a ref for the input element
  const inputRef = React.useRef<HTMLInputElement>(null);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.value && onDateChange) {
      onDateChange(new Date(e.target.value));
    }
  };

  // Function to programmatically open the date picker when clicking on the container
  const handleContainerClick = () => {
    // Focus and show the native date picker
    if (inputRef.current && !disabled) {
      inputRef.current.showPicker();
    }
  };

  return (
    <div 
      className={cn("relative cursor-pointer", className)} 
      onClick={handleContainerClick}
      {...props}
    >
      <input
        ref={inputRef}
        type="date"
        className={cn(
          "w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 pr-10"
        )}
        value={formatDate(date)}
        onChange={handleChange}
        disabled={disabled}
        min={min ? formatDate(min) : undefined}
        max={max ? formatDate(max) : undefined}
      />
      <CalendarIcon className="absolute right-3 top-2.5 h-4 w-4 text-muted-foreground pointer-events-none" />
    </div>
  )
}
Calendar.displayName = "Calendar"

export { Calendar }
