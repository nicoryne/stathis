"use client"

import * as React from "react"
import { CalendarIcon } from "lucide-react"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"

export interface CalendarProps extends React.HTMLAttributes<HTMLDivElement> {
  date?: Date;
  onDateChange?: (date: Date) => void;
  disabled?: boolean;
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
  ...props
}: CalendarProps) {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.value && onDateChange) {
      onDateChange(new Date(e.target.value));
    }
  };

  return (
    <div className={cn("flex flex-col space-y-2", className)} {...props}>
      <div className="relative">
        <input
          type="date"
          className={cn(
            "w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 pr-10",
            className
          )}
          value={formatDate(date)}
          onChange={handleChange}
          disabled={disabled}
        />
        <CalendarIcon className="absolute right-3 top-2.5 h-4 w-4 text-muted-foreground pointer-events-none" />
      </div>
    </div>
  )
}
Calendar.displayName = "Calendar"

export { Calendar }
