import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';

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
  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader>
        <CardTitle className="text-base">Safety Alerts</CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        {alerts.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-6 text-center">
            <p className="text-muted-foreground mb-2 text-sm">No active alerts</p>
            <p className="text-muted-foreground text-xs">All students are exercising safely</p>
          </div>
        ) : (
          <div className="space-y-0 divide-y">
            {alerts.map((alert) => (
              <div key={alert.id} className="flex items-center justify-between p-4">
                <div className="space-y-1">
                  <p className="text-sm leading-none font-medium">{alert.student}</p>
                  <p className="text-muted-foreground text-xs">{alert.issue}</p>
                  <p className="text-muted-foreground text-xs">{alert.time}</p>
                </div>
                <div className="flex items-center gap-2">
                  <Badge
                    variant={
                      alert.severity === 'high'
                        ? 'destructive'
                        : alert.severity === 'medium'
                          ? 'default'
                          : 'outline'
                    }
                  >
                    {alert.severity === 'high'
                      ? 'High'
                      : alert.severity === 'medium'
                        ? 'Medium'
                        : 'Low'}
                  </Badge>
                  <Button size="sm" variant="outline">
                    View
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
