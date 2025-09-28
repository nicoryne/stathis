'use client';

import { Activity, HeartPulse, Smartphone, Brain, Shield } from 'lucide-react';
import { motion } from 'framer-motion';
import { useReducedMotion } from '@/hooks/use-reduced-motion';

interface FloatingIconsProps {
  className?: string;
}

export function FloatingIcons({ className = '' }: FloatingIconsProps) {
  const prefersReducedMotion = useReducedMotion();
  
  // Don't render animations if user prefers reduced motion
  if (prefersReducedMotion) {
    return null;
  }

  return (
    <div className={`absolute inset-0 overflow-hidden pointer-events-none ${className}`} aria-hidden="true">
      {/* Heart icon */}
      <motion.div
        className="absolute top-[15%] left-[10%]"
        animate={{
          y: [0, -20, 0, 20, 0],
          x: [0, 10, 20, 10, 0],
          rotate: [0, 5, 0, -5, 0]
        }}
        transition={{
          duration: 6,
          repeat: Infinity,
          repeatType: 'loop',
          ease: 'easeInOut'
        }}
      >
        <div className="bg-primary/10 rounded-full p-3">
          <HeartPulse className="h-8 w-8 text-primary/40" />
        </div>
      </motion.div>
      
      {/* Activity icon */}
      <motion.div
        className="absolute top-[30%] right-[15%]"
        animate={{
          y: [0, 20, 0, -20, 0],
          x: [0, -10, -20, -10, 0],
          rotate: [0, -5, 0, 5, 0]
        }}
        transition={{
          duration: 6,
          repeat: Infinity,
          repeatType: 'loop',
          ease: 'easeInOut',
          delay: 1
        }}
      >
        <div className="bg-secondary/10 rounded-full p-3">
          <Activity className="h-6 w-6 text-secondary/40" />
        </div>
      </motion.div>
      
      {/* Smartphone icon */}
      <motion.div
        className="absolute bottom-[25%] left-[20%]"
        animate={{
          y: [0, -15, 0, 15, 0],
          x: [0, 15, 30, 15, 0],
          rotate: [0, 10, 0, -10, 0]
        }}
        transition={{
          duration: 6,
          repeat: Infinity,
          repeatType: 'loop',
          ease: 'easeInOut',
          delay: 2
        }}
      >
        <div className="bg-primary/10 rounded-full p-3">
          <Smartphone className="h-7 w-7 text-primary/40" />
        </div>
      </motion.div>
      
      {/* Brain icon */}
      <motion.div
        className="absolute bottom-[40%] right-[10%]"
        animate={{
          y: [0, 20, 0, -20, 0],
          x: [0, -20, -10, -5, 0],
          rotate: [0, -8, 0, 8, 0]
        }}
        transition={{
          duration: 6,
          repeat: Infinity,
          repeatType: 'loop',
          ease: 'easeInOut',
          delay: 3
        }}
      >
        <div className="bg-secondary/10 rounded-full p-3">
          <Brain className="h-6 w-6 text-secondary/40" />
        </div>
      </motion.div>
      
      {/* Shield icon */}
      <motion.div
        className="absolute top-[60%] left-[15%]"
        animate={{
          y: [0, -10, 0, 10, 0],
          x: [0, 5, 10, 5, 0],
          rotate: [0, 3, 0, -3, 0]
        }}
        transition={{
          duration: 6,
          repeat: Infinity,
          repeatType: 'loop',
          ease: 'easeInOut',
          delay: 1.5
        }}
      >
        <div className="bg-primary/10 rounded-full p-3">
          <Shield className="h-5 w-5 text-primary/40" />
        </div>
      </motion.div>
    </div>
  );
}
