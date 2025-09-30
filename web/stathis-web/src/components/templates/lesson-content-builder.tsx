'use client';

import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { PlusCircle, Trash2, MoveUp, MoveDown, File, ChevronDown, ChevronUp } from 'lucide-react';
import { Label } from '@/components/ui/label';

// Define types for lesson content structure
export interface LessonPage {
  id: string; // Unique identifier for the page
  pageNumber: number;
  subtitle: string;
  paragraph: string[] | string;
}

export interface LessonContent {
  pages: LessonPage[];
}

interface LessonContentBuilderProps {
  initialValue?: string;
  onChange: (jsonString: string) => void;
}

export function LessonContentBuilder({ initialValue, onChange }: LessonContentBuilderProps) {
  // State to track content
  const [content, setContent] = useState<LessonContent>({ pages: [] });
  
  // Load initial value if provided - only run once on mount
  useEffect(() => {
    if (initialValue) {
      try {
        const parsedContent = JSON.parse(initialValue);
        if (parsedContent && Array.isArray(parsedContent.pages)) {
          setContent(parsedContent);
        }
      } catch (e) {
        console.error('Failed to parse initial content:', e);
        // Initialize with empty content if parsing fails
        setContent({ pages: [] });
      }
    }
  }, []); // Empty dependency array - only run once on mount

  // Update parent component when content changes
  // But prevent unnecessary updates by memoizing the stringified content
  const [lastEmittedJson, setLastEmittedJson] = useState<string>('');
  
  useEffect(() => {
    const jsonString = JSON.stringify(content);
    
    // Only call onChange if the content has actually changed
    if (jsonString !== lastEmittedJson) {
      setLastEmittedJson(jsonString);
      onChange(jsonString);
    }
  }, [content, onChange, lastEmittedJson]);

  // Generate a unique ID for new pages
  const generateId = () => `page_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

  // Add a new empty page
  const addPage = () => {
    const newPageNumber = content.pages.length + 1;
    const newPage: LessonPage = {
      id: generateId(),
      pageNumber: newPageNumber,
      subtitle: '',
      paragraph: '',
    };
    setContent(prev => ({
      ...prev,
      pages: [...prev.pages, newPage]
    }));
  };

  // Remove a page by ID
  const removePage = (id: string) => {
    setContent(prev => {
      const updatedPages = prev.pages.filter(page => page.id !== id);
      // Re-number remaining pages
      return {
        ...prev,
        pages: updatedPages.map((page, index) => ({
          ...page,
          pageNumber: index + 1
        }))
      };
    });
  };

  // Update page content
  const updatePage = (id: string, field: keyof LessonPage, value: any) => {
    setContent(prev => {
      const updatedPages = prev.pages.map(page => {
        if (page.id === id) {
          return {
            ...page,
            [field]: value
          };
        }
        return page;
      });
      return {
        ...prev,
        pages: updatedPages
      };
    });
  };

  // Move page up in the list (decrease page number)
  const movePageUp = (id: string) => {
    setContent(prev => {
      const pageIndex = prev.pages.findIndex(page => page.id === id);
      if (pageIndex <= 0) return prev; // Already at the top
      
      const newPages = [...prev.pages];
      // Swap with the previous page
      [newPages[pageIndex - 1], newPages[pageIndex]] = [newPages[pageIndex], newPages[pageIndex - 1]];
      
      // Update page numbers
      return {
        ...prev,
        pages: newPages.map((page, index) => ({
          ...page,
          pageNumber: index + 1
        }))
      };
    });
  };

  // Move page down in the list (increase page number)
  const movePageDown = (id: string) => {
    setContent(prev => {
      const pageIndex = prev.pages.findIndex(page => page.id === id);
      if (pageIndex === -1 || pageIndex >= prev.pages.length - 1) return prev; // Already at the bottom
      
      const newPages = [...prev.pages];
      // Swap with the next page
      [newPages[pageIndex], newPages[pageIndex + 1]] = [newPages[pageIndex + 1], newPages[pageIndex]];
      
      // Update page numbers
      return {
        ...prev,
        pages: newPages.map((page, index) => ({
          ...page,
          pageNumber: index + 1
        }))
      };
    });
  };

  // Convert paragraph from string to array (for future expansion)
  const convertParagraphToArray = (id: string) => {
    setContent(prev => {
      const updatedPages = prev.pages.map(page => {
        if (page.id === id && typeof page.paragraph === 'string') {
          return {
            ...page,
            paragraph: [page.paragraph]
          };
        }
        return page;
      });
      return {
        ...prev,
        pages: updatedPages
      };
    });
  };

  // State to track which pages are expanded
  const [expandedPages, setExpandedPages] = useState<{ [key: string]: boolean }>({});

  // Toggle page expansion
  const togglePage = (id: string) => {
    setExpandedPages(prev => ({
      ...prev,
      [id]: !prev[id]
    }));
  };

  return (
    <div className="flex flex-col h-full space-y-4" style={{ maxHeight: 'calc(100vh - 300px)', minHeight: '400px' }}>
      <div className="flex justify-between items-center sticky top-0 bg-background z-10 py-2">
        <h3 className="text-lg font-medium">Lesson Content Builder</h3>
        <Button 
          type="button" 
          onClick={(e) => {
            e.preventDefault(); // Prevent any form submission
            e.stopPropagation(); // Stop event bubbling
            addPage();
          }} 
          size="sm" 
          className="flex items-center gap-1"
        >
          <PlusCircle className="h-4 w-4" />
          Add Page
        </Button>
      </div>

      <div className="flex-1 overflow-y-auto pr-1 pb-4" style={{ scrollbarWidth: 'thin' }}>
        {content.pages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-40 border border-dashed rounded-md p-6 text-center text-muted-foreground">
            <File className="h-12 w-12 mb-2 opacity-50" />
            <p>No pages added yet. Click "Add Page" to start building your lesson.</p>
          </div>
        ) : (
          <div className="space-y-3">
            {content.pages.map((page, index) => (
              <Card key={page.id} className="border rounded-lg overflow-hidden">
                <CardHeader className="p-3 pb-0">
                  <div className="flex justify-between items-center">
                    <div 
                      className="flex-1 flex items-center cursor-pointer" 
                      onClick={() => togglePage(page.id)}
                    >
                      <CardTitle className="text-md flex items-center gap-2">
                        <span className="bg-primary/10 rounded-full h-6 w-6 flex items-center justify-center text-sm">
                          {page.pageNumber}
                        </span>
                        <span className="flex-1">{page.subtitle || 'Untitled Page'}</span>
                        {expandedPages[page.id] ? 
                          <ChevronUp className="h-4 w-4" /> : 
                          <ChevronDown className="h-4 w-4" />}
                      </CardTitle>
                    </div>
                    <div className="flex items-center gap-1">
                      <Button 
                        variant="ghost" 
                        size="icon" 
                        onClick={() => movePageUp(page.id)}
                        disabled={index === 0}
                      >
                        <MoveUp className="h-4 w-4" />
                      </Button>
                      <Button 
                        variant="ghost" 
                        size="icon"
                        onClick={() => movePageDown(page.id)}
                        disabled={index === content.pages.length - 1}
                      >
                        <MoveDown className="h-4 w-4" />
                      </Button>
                      <Button 
                        variant="ghost" 
                        size="icon"
                        onClick={() => removePage(page.id)}
                      >
                        <Trash2 className="h-4 w-4 text-red-500" />
                      </Button>
                    </div>
                  </div>
                </CardHeader>
                {expandedPages[page.id] && (
                  <CardContent className="p-3 pt-6">
                    <div className="space-y-4">
                      <div>
                        <Label htmlFor={`subtitle-${page.id}`}>Title</Label>
                        <Input
                          id={`subtitle-${page.id}`}
                          value={page.subtitle}
                          onChange={(e) => updatePage(page.id, 'subtitle', e.target.value)}
                          placeholder="Page Title"
                        />
                      </div>
                      <div>
                        <Label htmlFor={`paragraph-${page.id}`}>Content</Label>
                        <Textarea
                          id={`paragraph-${page.id}`}
                          value={typeof page.paragraph === 'string' ? page.paragraph : page.paragraph.join('\n\n')}
                          onChange={(e) => updatePage(page.id, 'paragraph', e.target.value)}
                          placeholder="Page content or description..."
                          className="min-h-[150px]"
                        />
                        <p className="text-xs text-muted-foreground mt-1">
                          Write the main content for this page. Use paragraphs to organize your text.
                        </p>
                      </div>
                    </div>
                  </CardContent>
                )}
              </Card>
            ))}
          </div>
        )}
      </div>

      {/* Preview section (optional) */}
      {/* <Card className="mt-6">
        <CardHeader>
          <CardTitle>JSON Preview</CardTitle>
          <CardDescription>Generated JSON content</CardDescription>
        </CardHeader>
        <CardContent>
          <pre className="bg-muted p-2 rounded-md overflow-x-auto text-xs">
            {JSON.stringify(content, null, 2)}
          </pre>
        </CardContent>
      </Card> */}
    </div>
  );
}
