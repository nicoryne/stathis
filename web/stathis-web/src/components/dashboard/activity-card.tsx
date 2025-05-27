import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';

interface Activity {
  id: string;
  name: string;
  time: string;
  status: 'completed' | 'not-started' | 'ongoing';
  score?: number;
}

interface ActivityCardProps {
  activities: Activity[];
  className?: string;
}

export function ActivityCard({ activities, className }: ActivityCardProps) {
  return (
    <Card className={cn('overflow-hidden', className)}>
      <CardHeader>
        <CardTitle className="text-base">Recent Activities</CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        <div className="space-y-0 divide-y">
          {activities.map((activity) => (
            <div key={activity.id} className="flex items-center justify-between p-4">
              <div className="space-y-1">
                <p className="text-sm leading-none font-medium">{activity.name}</p>
                <p className="text-muted-foreground text-xs">{activity.time}</p>
              </div>
              <div className="flex items-center gap-2">
                {activity.score !== undefined && (
                  <span className="text-sm font-medium">{activity.score}/100</span>
                )}
                <Badge
                  variant={
                    activity.status === 'completed'
                      ? 'default'
                      : activity.status === 'ongoing'
                        ? 'secondary'
                        : 'outline'
                  }
                >
                  {activity.status === 'completed'
                    ? 'Completed'
                    : activity.status === 'ongoing'
                      ? 'Ongoing'
                      : 'Not Started'}
                </Badge>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}
