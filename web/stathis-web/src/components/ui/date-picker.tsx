'use client';

import * as React from 'react';
import { format } from 'date-fns';
import { Calendar as CalendarIcon } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';

interface DatePickerProps {
  date: Date | undefined;
  setDate: (date: Date | undefined) => void;
  disabled?: boolean;
}

export function DatePicker({ date, setDate, disabled = false }: DatePickerProps) {
  // Simplified date picker without Calendar component dependency
  return (
    <div className="w-full">
      <Button
        type="button"
        variant="outline"
        className={cn(
          'w-full justify-start text-left font-normal',
          !date && 'text-muted-foreground'
        )}
        disabled={disabled}
        onClick={() => {
          // For simplicity, using input type date
          const input = document.createElement('input');
          input.type = 'date';
          input.onchange = (e) => {
            const target = e.target as HTMLInputElement;
            if (target.value) {
              setDate(new Date(target.value));
            }
          };
          input.click();
        }}
      >
        <CalendarIcon className="mr-2 h-4 w-4" />
        {date ? format(date, 'PPP') : <span>Pick a date</span>}
      </Button>
    </div>
  );
}
