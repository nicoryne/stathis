import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';
import { motion } from 'framer-motion';
import { TrendingUp, TrendingDown, BarChart3, CheckCircle, AlertTriangle, Minus } from 'lucide-react';

interface OverviewCardProps {
  title: string;
  description?: string;
  metrics: {
    label: string;
    value: string | number;
    target?: string | number;
    progress?: number;
    trend?: {
      value: number;
      positive: boolean;
    };
    status?: 'positive' | 'warning' | 'negative' | 'neutral';
  }[];
  className?: string;
}

export function OverviewCard({ title, description, metrics, className }: OverviewCardProps) {
  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'positive':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'warning':
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      case 'negative':
        return <AlertTriangle className="h-4 w-4 text-red-500" />;
      case 'neutral':
        return <Minus className="h-4 w-4 text-gray-500" />;
      default:
        return <BarChart3 className="h-4 w-4 text-primary" />;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'positive':
        return 'bg-green-100 text-green-700 dark:bg-green-900/20 dark:text-green-400';
      case 'warning':
        return 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400';
      case 'negative':
        return 'bg-red-100 text-red-700 dark:bg-red-900/20 dark:text-red-400';
      case 'neutral':
        return 'bg-gray-100 text-gray-700 dark:bg-gray-900/20 dark:text-gray-400';
      default:
        return 'bg-primary/10 text-primary';
    }
  };

  return (
    <Card className={`rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300 h-full min-h-[280px] flex flex-col ${className}`}>
      <CardHeader className="pb-4">
        <CardTitle className="text-lg font-semibold flex items-center gap-2">
          <div className="relative">
            <div className="absolute -inset-1 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-sm" />
            <BarChart3 className="relative h-5 w-5 text-primary" />
          </div>
          {title}
        </CardTitle>
        {description && <CardDescription className="text-sm">{description}</CardDescription>}
      </CardHeader>
      <CardContent className="flex-1 flex flex-col justify-between">
        <div className="space-y-6">
          {metrics.map((metric, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: index * 0.1 }}
              className="space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  {metric.status && getStatusIcon(metric.status)}
                  <div className="text-sm font-medium">{metric.label}</div>
                </div>
                <div className="flex items-center gap-3">
                  <div className="text-right">
                    <div className="font-semibold text-lg">{metric.value}</div>
                    {metric.target && (
                      <div className="text-muted-foreground text-xs">/ {metric.target}</div>
                    )}
                  </div>
                  
                  {metric.trend && (
                    <div className="flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-medium bg-primary/10 text-primary">
                      {metric.trend.positive ? (
                        <TrendingUp className="h-3 w-3" />
                      ) : (
                        <TrendingDown className="h-3 w-3" />
                      )}
                      {Math.abs(metric.trend.value)}%
                    </div>
                  )}
                  
                  {metric.status && (
                    <Badge className={`text-xs font-medium px-2 py-1 rounded-lg ${getStatusColor(metric.status)}`}>
                      {metric.status === 'positive'
                        ? 'Good'
                        : metric.status === 'warning'
                          ? 'Warning'
                          : metric.status === 'negative'
                            ? 'Alert'
                            : 'Neutral'}
                    </Badge>
                  )}
                </div>
              </div>
              
              {metric.progress !== undefined && (
                <div className="space-y-2">
                  <div className="flex justify-between text-xs text-muted-foreground">
                    <span>Progress</span>
                    <span>{metric.progress}%</span>
                  </div>
                  <Progress 
                    value={metric.progress} 
                    className="h-2 bg-muted/50"
                  />
                </div>
              )}
            </motion.div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
