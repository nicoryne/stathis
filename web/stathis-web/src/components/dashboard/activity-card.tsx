import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { motion } from 'framer-motion';
import { CheckCircle, Clock, PlayCircle } from 'lucide-react';

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
    <Card className={cn('overflow-hidden rounded-2xl border-border/50 bg-card/80 backdrop-blur-xl shadow-lg hover:shadow-xl transition-all duration-300', className)}>
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
        {activities.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-8 text-center">
            <div className="relative mb-4">
              <div className="absolute -inset-4 rounded-full bg-gradient-to-br from-muted/20 to-muted/10 blur-lg" />
              <Clock className="relative h-12 w-12 text-muted-foreground" />
            </div>
            <p className="text-muted-foreground font-medium">No activities yet</p>
            <p className="text-muted-foreground text-sm">Activities will appear here once students start working</p>
          </div>
        ) : (
          <div className="space-y-0">
            {activities.map((activity, index) => (
              <motion.div
                key={activity.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, delay: index * 0.1 }}
                className="flex items-center justify-between p-4 hover:bg-muted/30 transition-colors duration-200"
              >
                <div className="flex items-center gap-3">
                  {getStatusIcon(activity.status)}
                  <div className="space-y-1">
                    <p className="text-sm font-medium leading-none">{activity.name}</p>
                    <p className="text-muted-foreground text-xs">{activity.time}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  {activity.score !== undefined && (
                    <div className="text-right">
                      <span className="text-sm font-semibold text-primary">{activity.score}%</span>
                    </div>
                  )}
                  <Badge
                    className={cn(
                      'text-xs font-medium px-2 py-1 rounded-lg',
                      getStatusColor(activity.status)
                    )}
                  >
                    {activity.status === 'completed'
                      ? 'Completed'
                      : activity.status === 'ongoing'
                        ? 'Ongoing'
                        : 'Not Started'}
                  </Badge>
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
