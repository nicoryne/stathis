import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { motion } from 'framer-motion';
import { AlertTriangle, Shield, Eye, CheckCircle } from 'lucide-react';

interface Alert {
  id: string;
  student: string;
  issue: string;
  time: string;
  severity: 'high' | 'medium' | 'low';
}

interface AlertCardProps {
  alerts: Alert[];
  className?: string;
}

export function AlertCard({ alerts, className }: AlertCardProps) {
  const getSeverityIcon = (severity: Alert['severity']) => {
    switch (severity) {
      case 'high':
        return <AlertTriangle className="h-4 w-4 text-red-500" />;
      case 'medium':
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      case 'low':
        return <Shield className="h-4 w-4 text-blue-500" />;
    }
  };

  const getSeverityColor = (severity: Alert['severity']) => {
    switch (severity) {
      case 'high':
        return 'bg-red-100 text-red-700 dark:bg-red-900/20 dark:text-red-400';
      case 'medium':
        return 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900/20 dark:text-yellow-400';
      case 'low':
        return 'bg-blue-100 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400';
    }
  };

  return (
    <Card className={cn('overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300', className)}>
      <CardHeader className="pb-4">
        <CardTitle className="text-lg font-semibold flex items-center gap-2">
          <div className="relative">
            <div className="absolute -inset-1 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-sm" />
            <AlertTriangle className="relative h-5 w-5 text-primary" />
          </div>
          Safety Alerts
        </CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        {alerts.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-8 text-center">
            <div className="relative mb-4">
              <div className="absolute -inset-4 rounded-full bg-gradient-to-br from-green-500/20 to-emerald-500/20 blur-lg" />
              <CheckCircle className="relative h-12 w-12 text-green-500" />
            </div>
            <p className="text-muted-foreground font-medium">No active alerts</p>
            <p className="text-muted-foreground text-sm">All students are exercising safely</p>
          </div>
        ) : (
          <div className="space-y-0">
            {alerts.map((alert, index) => (
              <motion.div
                key={alert.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, delay: index * 0.1 }}
                className="flex items-center justify-between p-4 hover:bg-muted/30 transition-colors duration-200"
              >
                <div className="flex items-center gap-3">
                  {getSeverityIcon(alert.severity)}
                  <div className="space-y-1">
                    <p className="text-sm font-medium leading-none">{alert.student}</p>
                    <p className="text-muted-foreground text-xs">{alert.issue}</p>
                    <p className="text-muted-foreground text-xs">{alert.time}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <Badge className={cn('text-xs font-medium px-2 py-1 rounded-lg', getSeverityColor(alert.severity))}>
                    {alert.severity === 'high'
                      ? 'High'
                      : alert.severity === 'medium'
                        ? 'Medium'
                        : 'Low'}
                  </Badge>
                  <Button 
                    size="sm" 
                    variant="outline"
                    className="rounded-lg bg-background/50 border-border/50 hover:bg-muted/50"
                  >
                    <Eye className="h-3 w-3 mr-1" />
                    View
                  </Button>
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
