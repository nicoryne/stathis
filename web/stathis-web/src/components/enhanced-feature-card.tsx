'use client';

import { LucideIcon } from 'lucide-react';
import { motion } from 'framer-motion';
import { useReducedMotion } from '@/hooks/use-reduced-motion';
import { useState } from 'react';

interface EnhancedFeatureCardProps {
  icon: LucideIcon;
  title: string;
  description: string;
  delay?: number;
  additionalInfo?: string;
  className?: string;
}

export function EnhancedFeatureCard({
  icon: Icon,
  title,
  description,
  delay = 0,
  additionalInfo,
  className = ''
}: EnhancedFeatureCardProps) {
  const prefersReducedMotion = useReducedMotion();
  const [isHovered, setIsHovered] = useState(false);
  
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      transition={{ duration: prefersReducedMotion ? 0 : 0.5, delay: prefersReducedMotion ? 0 : delay }}
      viewport={{ once: true }}
      className={`feature-card bg-card border border-border rounded-xl p-6 ${className} ${prefersReducedMotion ? '' : 'gpu-accelerated'}`}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onFocus={() => setIsHovered(true)}
      onBlur={() => setIsHovered(false)}
      tabIndex={0}
    >
      <div className="relative">
        <div className="bg-primary/10 text-primary rounded-full p-3 w-12 h-12 flex items-center justify-center mb-4">
          <Icon className="h-6 w-6" />
        </div>
        
        <h3 className="text-xl font-bold mb-2">{title}</h3>
        <p className="text-muted-foreground">{description}</p>
        
        {additionalInfo && (
          <motion.div 
            className="mt-4 pt-4 border-t border-border"
            initial={{ opacity: 0, height: 0 }}
            animate={{ 
              opacity: isHovered ? 1 : 0,
              height: isHovered ? 'auto' : 0
            }}
            transition={{ duration: 0.3 }}
          >
            <p className="text-sm text-muted-foreground">{additionalInfo}</p>
          </motion.div>
        )}
      </div>
    </motion.div>
  );
}
