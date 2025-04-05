'use client';

import { motion } from 'framer-motion';
import type { LucideIcon } from 'lucide-react';

interface FeatureCardProps {
  icon: LucideIcon;
  title: string;
  description: string;
  delay?: number;
}

export function FeatureCard({ icon: Icon, title, description, delay = 0 }: FeatureCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay }}
      viewport={{ once: true }}
      className="bg-card/50 border-border rounded-lg border p-6 backdrop-blur-sm transition-shadow hover:shadow-lg"
    >
      <div className="bg-primary/10 mb-4 flex h-12 w-12 items-center justify-center rounded-full">
        <Icon className="text-primary h-6 w-6" />
      </div>
      <h3 className="mb-2 text-lg font-medium">{title}</h3>
      <p className="text-muted-foreground">{description}</p>
    </motion.div>
  );
}
