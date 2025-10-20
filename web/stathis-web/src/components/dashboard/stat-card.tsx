import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { cn } from '@/lib/utils';
import type { LucideIcon } from 'lucide-react';
import { ReactNode } from 'react';
import { motion } from 'framer-motion';
import { TrendingUp, TrendingDown } from 'lucide-react';

interface StatCardProps {
  title: string;
  value: string;
  description?: ReactNode;
  icon: LucideIcon;
  trend?: {
    value: number;
    positive: boolean;
  };
  className?: string;
}

export function StatCard({
  title,
  value,
  description,
  icon: Icon,
  trend,
  className
}: StatCardProps) {
  return (
    <motion.div
      whileHover={{ scale: 1.02 }}
      transition={{ duration: 0.2 }}
    >
      <Card className={cn('overflow-hidden rounded-2xl border-border/50 bg-card/90 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300 h-full min-h-[200px] flex flex-col', className)}>
        <CardHeader className="flex flex-row items-center justify-between pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground tracking-tight">{title}</CardTitle>
          <div className="relative">
            <div className="absolute -inset-1.5 rounded-full bg-gradient-to-br from-primary/15 to-secondary/15 blur-md" />
            <Icon className="relative text-primary h-4 w-4" />
          </div>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col gap-2">
          <div className="text-3xl md:text-4xl font-bold tabular-nums bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">{value}</div>
          {description && <div className="text-muted-foreground text-sm leading-relaxed">{description}</div>}
          {trend && (
            <div className="mt-1 flex items-center gap-2">
              <div className={cn(
                'flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-medium',
                trend.positive 
                  ? 'bg-success/10 text-success border border-success/20' 
                  : 'bg-destructive/10 text-destructive border border-destructive/20'
              )}>
                {trend.positive ? (
                  <TrendingUp className="h-3 w-3" />
                ) : (
                  <TrendingDown className="h-3 w-3" />
                )}
                {Math.abs(trend.value)}%
              </div>
              <span className="text-muted-foreground text-xs">from last week</span>
            </div>
          )}
        </CardContent>
      </Card>
    </motion.div>
  );
}
