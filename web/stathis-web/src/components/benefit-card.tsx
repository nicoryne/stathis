'use client';

import { motion } from 'framer-motion';

interface BenefitCardProps {
  title: string;
  description: string;
  index: number;
  forWhom: string;
}

export function BenefitCard({ title, description, index, forWhom }: BenefitCardProps) {
  return (
    <motion.div
      initial={{ opacity: 0, x: index % 2 === 0 ? -20 : 20 }}
      whileInView={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.5, delay: index * 0.1 }}
      viewport={{ once: true }}
      className="border-border relative overflow-hidden rounded-lg border p-6"
    >
      <div className="bg-secondary/10 text-secondary absolute top-0 right-0 rounded-bl-md px-2 py-1 text-xs font-medium">
        {forWhom}
      </div>
      <h3 className="mb-2 text-lg font-medium">{title}</h3>
      <p className="text-muted-foreground text-sm">{description}</p>
    </motion.div>
  );
}
