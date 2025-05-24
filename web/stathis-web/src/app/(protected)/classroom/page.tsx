'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2, Plus, Search, School2, ArrowRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
import { CreateClassroomForm } from '@/components/classroom/create-classroom-form';
import { useMutation, useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { getTeacherClassrooms, ClassroomResponseDTO } from '@/services/api-classroom-client';
import { getCurrentUserEmail, getCurrentUserPhysicalId } from '@/lib/utils/jwt';

export default function ClassroomPage() {
  const router = useRouter();
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  
  // Get user email from JWT token or localStorage using the utility function
  const userEmail = getCurrentUserEmail();
  
  // Ensure we have a valid user email before proceeding
  useEffect(() => {
    if (!userEmail && typeof window !== 'undefined') {
      // Redirect to login if we don't have a user email
      router.push('/login');
      toast.error('User information not found. Please log in again.');
    }
  }, [userEmail, router]);
  
  // Fetch classrooms for the current teacher
  // The backend will identify the teacher using the security context from the JWT token
  const { data: classrooms, isLoading, isError, error, refetch } = useQuery({
    queryKey: ['classrooms'],
    queryFn: () => getTeacherClassrooms(),
    enabled: !!userEmail, // Only fetch if we have a user email
  });
  
  // Filter classrooms based on search term
  const filteredClassrooms = classrooms?.filter(classroom => 
    classroom.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    classroom.description.toLowerCase().includes(searchTerm.toLowerCase())
  );
  
  // Handle classroom creation success
  const handleClassroomCreated = () => {
    setShowCreateDialog(false);
    refetch();
    toast.success('Classroom created successfully!');
  };
  
  return (
    <div className="container mx-auto py-6 max-w-7xl">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Classrooms</h1>
          <p className="text-muted-foreground mt-1">Manage your physical education classrooms</p>
        </div>
        
        <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="mr-2 h-4 w-4" />
              Create Classroom
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-[600px]">
            <DialogHeader>
              <DialogTitle>Create New Classroom</DialogTitle>
              <DialogDescription>
                Fill in the details to create a new physical education classroom.
              </DialogDescription>
            </DialogHeader>
            
            <CreateClassroomForm 
              onSuccess={handleClassroomCreated} 
              onCancel={() => setShowCreateDialog(false)}
            />
          </DialogContent>
        </Dialog>
      </div>
      
      <div className="flex mb-6 w-full max-w-sm items-center space-x-2">
        <Input
          placeholder="Search classrooms..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full"
        />
        <Button variant="outline" size="icon">
          <Search className="h-4 w-4" />
        </Button>
      </div>
      
      {isLoading ? (
        <div className="flex justify-center items-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
          <span className="ml-2">Loading classrooms...</span>
        </div>
      ) : isError ? (
        <div className="bg-destructive/10 p-4 rounded-md">
          <p className="text-destructive">Error loading classrooms: {error?.message || 'Unknown error'}</p>
          <Button variant="outline" className="mt-2" onClick={() => refetch()}>
            Try Again
          </Button>
        </div>
      ) : filteredClassrooms?.length === 0 ? (
        <div className="text-center py-12 border rounded-lg bg-muted/20">
          <School2 className="mx-auto h-12 w-12 text-muted-foreground" />
          <h3 className="mt-4 text-lg font-medium">No classrooms found</h3>
          <p className="mt-1 text-muted-foreground">
            {searchTerm ? 'No results match your search criteria.' : 'Create your first classroom to get started.'}
          </p>
          {searchTerm && (
            <Button variant="outline" className="mt-4" onClick={() => setSearchTerm('')}>
              Clear Search
            </Button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredClassrooms?.map((classroom) => (
            <Card key={classroom.physicalId} className="overflow-hidden">
              <CardHeader className="pb-3">
                <div className="flex justify-between items-start">
                  <CardTitle className="text-xl">{classroom.name}</CardTitle>
                  <Badge variant={classroom.isActive ? "default" : "secondary"}>
                    {classroom.isActive ? 'Active' : 'Inactive'}
                  </Badge>
                </div>
                <CardDescription className="line-clamp-2 h-10">
                  {classroom.description}
                </CardDescription>
              </CardHeader>
              
              <CardContent>
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Capacity:</span>
                    <span>{classroom.capacity} students</span>
                  </div>
                  {classroom.startDate && (
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">Start Date:</span>
                      <span>{new Date(classroom.startDate).toLocaleDateString()}</span>
                    </div>
                  )}
                  {classroom.endDate && (
                    <div className="flex justify-between">
                      <span className="text-muted-foreground">End Date:</span>
                      <span>{new Date(classroom.endDate).toLocaleDateString()}</span>
                    </div>
                  )}
                </div>
              </CardContent>
              
              <CardFooter className="pt-3 flex justify-end">
                <Button 
                  variant="ghost" 
                  size="sm" 
                  className="text-primary" 
                  onClick={() => router.push(`/classroom/${classroom.physicalId}`)}
                >
                  View Details
                  <ArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}