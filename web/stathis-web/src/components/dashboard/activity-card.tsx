import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { motion } from 'framer-motion';
import { CheckCircle, Clock, PlayCircle } from 'lucide-react';
import Image from 'next/image';

interface Activity {
  id: string;
  name: string;
  time: string;
  status: 'completed' | 'not-started' | 'ongoing';
  score?: number;
  maxScore?: number;
}

interface ActivityCardProps {
  activities: Activity[];
  className?: string;
}

export function ActivityCard({ activities, className }: ActivityCardProps) {
  const getStatusIcon = (status: Activity['status']) => {
    switch (status) {
      case 'completed':
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case 'ongoing':
        return <PlayCircle className="h-4 w-4 text-blue-500" />;
      case 'not-started':
        return <Clock className="h-4 w-4 text-muted-foreground" />;
    }
  };

  const getStatusColor = (status: Activity['status']) => {
    switch (status) {
      case 'completed':
        return 'bg-green-100 text-green-700 dark:bg-green-900/20 dark:text-green-400';
      case 'ongoing':
        return 'bg-blue-100 text-blue-700 dark:bg-blue-900/20 dark:text-blue-400';
      case 'not-started':
        return 'bg-gray-100 text-gray-700 dark:bg-gray-900/20 dark:text-gray-400';
    }
  };

  return (
    <Card className={cn('overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300 h-full min-h-[280px] flex flex-col', className)}>
      <CardHeader className="pb-4">
        <CardTitle className="text-lg font-semibold flex items-center gap-2">
          <div className="relative">
            <div className="absolute -inset-1 rounded-full bg-gradient-to-br from-primary/20 to-secondary/20 blur-sm" />
            <PlayCircle className="relative h-5 w-5 text-primary" />
          </div>
          Recent Activities
        </CardTitle>
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
                  <span className="text-sm font-medium">
                    {activity.score}/{activity.maxScore || 100}
                  </span>
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
