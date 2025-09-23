'use client';

import { HeartPulse } from 'lucide-react';
import { motion } from 'framer-motion';
import { useReducedMotion } from '@/hooks/use-reduced-motion';

interface AnimatedLogoProps {
  size?: 'sm' | 'md' | 'lg';
  showText?: boolean;
  className?: string;
}

export function AnimatedLogo({
  size = 'md',
  showText = true,
  className = ''
}: AnimatedLogoProps) {
  const prefersReducedMotion = useReducedMotion();
  
  // Size mappings
  const sizeMap = {
    sm: {
      icon: 'h-6 w-6',
      text: 'text-lg'
    },
    md: {
      icon: 'h-8 w-8',
      text: 'text-2xl'
    },
    lg: {
      icon: 'h-10 w-10',
      text: 'text-3xl'
    }
  };

  return (
    <div className={`flex items-center gap-2 ${className}`}>
      <motion.div
        animate={
          prefersReducedMotion 
            ? {} 
            : { scale: [1, 1.1, 1, 1.15, 1] }
        }
        transition={{
          duration: 1.5,
          repeat: Infinity,
          repeatType: 'loop',
          ease: 'easeInOut'
        }}
        className={`relative ${sizeMap[size].icon}`}
        aria-hidden="true"
      >
        <HeartPulse className={`text-primary ${sizeMap[size].icon}`} />
        {!prefersReducedMotion && (
          <motion.div
            className="absolute inset-0 rounded-full bg-primary/20"
            animate={{ scale: [1, 1.5, 1], opacity: [0.7, 0, 0.7] }}
            transition={{ duration: 1.5, repeat: Infinity, repeatType: 'loop' }}
          />
        )}
      </motion.div>
      
      {showText && (
        <span className={`font-bold tracking-tight ${sizeMap[size].text}`}>
          Stathis
        </span>
      )}
    </div>
  );
}
