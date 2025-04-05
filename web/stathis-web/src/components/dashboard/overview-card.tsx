import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Progress } from '@/components/ui/progress';

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
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        {description && <CardDescription>{description}</CardDescription>}
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {metrics.map((metric, index) => (
            <div key={index} className="space-y-2">
              <div className="flex items-center justify-between">
                <div className="text-sm font-medium">{metric.label}</div>
                <div className="flex items-center gap-2">
                  <div className="font-medium">
                    {metric.value}
                    {metric.target && (
                      <span className="text-muted-foreground ml-1 text-xs">/ {metric.target}</span>
                    )}
                  </div>
                  {metric.trend && (
                    <Badge
                      variant={metric.trend.positive ? 'default' : 'destructive'}
                      className="text-xs"
                    >
                      {metric.trend.positive ? '+' : '-'}
                      {Math.abs(metric.trend.value)}%
                    </Badge>
                  )}
                  {metric.status && (
                    <Badge
                      variant={
                        metric.status === 'positive'
                          ? 'default'
                          : metric.status === 'warning'
                            ? 'secondary'
                            : metric.status === 'negative'
                              ? 'destructive'
                              : 'outline'
                      }
                      className="text-xs"
                    >
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
                <Progress value={metric.progress} className="h-2" />
              )}
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
