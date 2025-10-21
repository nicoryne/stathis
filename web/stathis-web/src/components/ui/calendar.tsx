"use client"

import * as React from "react"
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon } from "lucide-react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { motion, AnimatePresence } from "framer-motion"

export interface CalendarProps extends React.HTMLAttributes<HTMLDivElement> {
  date?: Date;
  onDateChange?: (date: Date) => void;
  disabled?: boolean;
  min?: Date;
  max?: Date;
}

const MONTHS = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December"
];

const DAYS = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];

function Calendar({
  className,
  date = new Date(),
  onDateChange,
  disabled = false,
  min,
  max,
  ...restProps
}: CalendarProps) {
  const [currentMonth, setCurrentMonth] = React.useState(() => date.getMonth());
  const [currentYear, setCurrentYear] = React.useState(() => date.getFullYear());
  const [selectedDate, setSelectedDate] = React.useState(() => date);

  // Exclude conflicting event handlers that framer-motion uses
  const { 
    onDrag, onDragStart, onDragEnd, onDragEnter, onDragLeave, onDragOver, onDrop,
    onAnimationStart, onAnimationEnd, onAnimationIteration,
    ...props 
  } = restProps as any;
  // Memoize the date to prevent unnecessary re-renders
  const memoizedDate = React.useMemo(() => {
    return date ? new Date(date.getFullYear(), date.getMonth(), date.getDate()) : null;
  }, [date?.getFullYear(), date?.getMonth(), date?.getDate()]);

  React.useEffect(() => {
    if (memoizedDate) {
      // Only update if the values are actually different to prevent infinite loops
      const newMonth = memoizedDate.getMonth();
      const newYear = memoizedDate.getFullYear();
      
      if (currentMonth !== newMonth || currentYear !== newYear) {
        setCurrentMonth(newMonth);
        setCurrentYear(newYear);
      }
      
      // Only update selectedDate if it's different
      const currentSelectedDate = new Date(currentYear, currentMonth, selectedDate.getDate());
      if (memoizedDate.getTime() !== currentSelectedDate.getTime()) {
        setSelectedDate(memoizedDate);
      }
    }
  }, [memoizedDate, currentMonth, currentYear, selectedDate]);

  const handleDateSelect = (day: number) => {
    const newDate = new Date(currentYear, currentMonth, day);
    
    // Check if the date is within min/max bounds (compare only date parts)
    if (min) {
      const minDate = new Date(min.getFullYear(), min.getMonth(), min.getDate());
      const checkDate = new Date(currentYear, currentMonth, day);
      if (checkDate < minDate) return;
    }
    if (max) {
      const maxDate = new Date(max.getFullYear(), max.getMonth(), max.getDate());
      const checkDate = new Date(currentYear, currentMonth, day);
      if (checkDate > maxDate) return;
    }
    
    setSelectedDate(newDate);
    if (onDateChange) {
      onDateChange(newDate);
    }
  };

  const handlePrevMonth = () => {
    let newMonth = currentMonth === 0 ? 11 : currentMonth - 1;
    let newYear = currentMonth === 0 ? currentYear - 1 : currentYear;
    
    // Check if the new month/year is within bounds
    if (min) {
      const minDate = new Date(min.getFullYear(), min.getMonth(), min.getDate());
      const newDate = new Date(newYear, newMonth, 1);
      if (newDate < minDate) return;
    }
    
    setCurrentMonth(newMonth);
    setCurrentYear(newYear);
  };

  const handleNextMonth = () => {
    let newMonth = currentMonth === 11 ? 0 : currentMonth + 1;
    let newYear = currentMonth === 11 ? currentYear + 1 : currentYear;
    
    // Check if the new month/year is within bounds
    if (max) {
      const maxDate = new Date(max.getFullYear(), max.getMonth(), max.getDate());
      const newDate = new Date(newYear, newMonth, 1);
      if (newDate > maxDate) return;
    }
    
    setCurrentMonth(newMonth);
    setCurrentYear(newYear);
  };

  const handleToday = () => {
    const today = new Date();
    setCurrentMonth(today.getMonth());
    setCurrentYear(today.getFullYear());
    setSelectedDate(today);
    if (onDateChange) {
      onDateChange(today);
    }
  };

  const handleClear = () => {
    const clearedDate = new Date();
    setSelectedDate(clearedDate);
    if (onDateChange) {
      onDateChange(clearedDate);
    }
  };

  const getDaysInMonth = (month: number, year: number) => {
    return new Date(year, month + 1, 0).getDate();
  };

  const getFirstDayOfMonth = (month: number, year: number) => {
    return new Date(year, month, 1).getDay();
  };

  const isDateDisabled = (day: number) => {
    const checkDate = new Date(currentYear, currentMonth, day);
    if (min) {
      const minDate = new Date(min.getFullYear(), min.getMonth(), min.getDate());
      if (checkDate < minDate) return true;
    }
    if (max) {
      const maxDate = new Date(max.getFullYear(), max.getMonth(), max.getDate());
      if (checkDate > maxDate) return true;
    }
    return false;
  };

  const isToday = (day: number) => {
    const today = new Date();
    return (
      day === today.getDate() &&
      currentMonth === today.getMonth() &&
      currentYear === today.getFullYear()
    );
  };

  const isSelected = (day: number) => {
    return (
      day === selectedDate.getDate() &&
      currentMonth === selectedDate.getMonth() &&
      currentYear === selectedDate.getFullYear()
    );
  };

  const renderCalendarDays = () => {
    const daysInMonth = getDaysInMonth(currentMonth, currentYear);
    const firstDay = getFirstDayOfMonth(currentMonth, currentYear);
    const days = [];

    // Previous month's trailing days
    const prevMonth = currentMonth === 0 ? 11 : currentMonth - 1;
    const prevYear = currentMonth === 0 ? currentYear - 1 : currentYear;
    const daysInPrevMonth = getDaysInMonth(prevMonth, prevYear);
    
    for (let i = firstDay - 1; i >= 0; i--) {
      days.push(
        <motion.div
          key={`prev-${i}`}
          className="h-10 w-10 flex items-center justify-center text-sm text-muted-foreground/50"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: i * 0.02 }}
        >
          {daysInPrevMonth - i}
        </motion.div>
      );
    }

    // Current month's days
    for (let day = 1; day <= daysInMonth; day++) {
      const disabled = isDateDisabled(day);
      const today = isToday(day);
      const selected = isSelected(day);
      
      days.push(
        <motion.button
          key={day}
          onClick={() => !disabled && handleDateSelect(day)}
          disabled={disabled}
          className={cn(
            "h-10 w-10 flex items-center justify-center text-sm font-medium rounded-xl transition-all duration-200 relative",
            "hover:bg-primary/10 hover:scale-105 focus:outline-none focus:ring-2 focus:ring-primary/20",
            disabled && "opacity-30 cursor-not-allowed hover:bg-transparent hover:scale-100",
            today && !selected && "bg-accent/20 text-accent font-semibold",
            selected && "bg-gradient-to-r from-primary to-secondary text-white shadow-lg scale-105"
          )}
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ delay: (firstDay + day - 1) * 0.02 }}
          whileHover={!disabled ? { scale: 1.05 } : {}}
          whileTap={!disabled ? { scale: 0.95 } : {}}
        >
          {selected && (
            <motion.div
              className="absolute inset-0 rounded-xl bg-gradient-to-r from-primary to-secondary"
              layoutId="selected-date"
              transition={{ type: "spring", stiffness: 300, damping: 30 }}
            />
          )}
          <span className="relative z-10">{day}</span>
        </motion.button>
      );
    }

    // Next month's leading days
    const remainingDays = Math.max(0, 42 - days.length); // Ensure non-negative
    for (let day = 1; day <= remainingDays; day++) {
      days.push(
        <motion.div
          key={`next-${day}`}
          className="h-10 w-10 flex items-center justify-center text-sm text-muted-foreground/50"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: (daysInMonth + firstDay + day - 1) * 0.02 }}
        >
          {day}
        </motion.div>
      );
    }

    return days;
  };

  return (
    <motion.div
      className={cn(
        "bg-card/95 backdrop-blur-xl border border-border/30 rounded-2xl p-6 shadow-2xl",
        className
      )}
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      {...props}
    >
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <motion.div
          className="flex items-center gap-2"
          initial={{ opacity: 0, x: -10 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: 0.1 }}
        >
          <div className="w-8 h-8 rounded-full bg-gradient-to-r from-primary/20 to-secondary/20 flex items-center justify-center">
            <CalendarIcon className="h-4 w-4 text-primary" />
          </div>
          <h3 className="text-lg font-semibold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
            {MONTHS[currentMonth]} {currentYear}
          </h3>
        </motion.div>
        
        <div className="flex items-center gap-1">
          <motion.button
            onClick={handlePrevMonth}
            className="h-8 w-8 rounded-lg bg-background/60 backdrop-blur-sm border border-border/30 flex items-center justify-center hover:bg-background/80 transition-all duration-200"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <ChevronLeft className="h-4 w-4" />
          </motion.button>
          <motion.button
            onClick={handleNextMonth}
            className="h-8 w-8 rounded-lg bg-background/60 backdrop-blur-sm border border-border/30 flex items-center justify-center hover:bg-background/80 transition-all duration-200"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <ChevronRight className="h-4 w-4" />
          </motion.button>
        </div>
      </div>

      {/* Days of week */}
      <div className="grid grid-cols-7 gap-1 mb-4">
        {DAYS.map((day, index) => (
          <motion.div
            key={day}
            className="h-8 flex items-center justify-center text-xs font-medium text-muted-foreground"
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 + index * 0.05 }}
          >
            {day}
          </motion.div>
        ))}
      </div>

      {/* Calendar grid */}
      <div className="grid grid-cols-7 gap-1 mb-6">
        <AnimatePresence mode="wait">
          {renderCalendarDays()}
        </AnimatePresence>
      </div>

      {/* Footer */}
      <div className="flex items-center justify-between pt-4 border-t border-border/20">
        <motion.button
          onClick={handleClear}
          className="text-sm text-primary hover:text-primary/80 font-medium transition-colors duration-200"
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
        >
          Clear
        </motion.button>
        <motion.button
          onClick={handleToday}
          className="text-sm text-primary hover:text-primary/80 font-medium transition-colors duration-200"
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
        >
          Today
        </motion.button>
      </div>
    </motion.div>
  )
}

Calendar.displayName = "Calendar"

export { Calendar }