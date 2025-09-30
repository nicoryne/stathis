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
import { PlusCircle, Trash2, MoveUp, MoveDown, FileQuestion, ChevronDown, ChevronUp, Plus, X } from 'lucide-react';
import { Label } from '@/components/ui/label';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group';

// Define types for quiz content structure
export interface QuizQuestion {
  id: string; // Unique identifier for the question
  questionNumber: number;
  question: string;
  options: string[];
  answer: number; // Index of the correct answer (0-based)
}

export interface QuizContent {
  questions: QuizQuestion[];
}

interface QuizContentBuilderProps {
  initialValue?: string;
  onChange: (jsonString: string) => void;
}

export function QuizContentBuilder({ initialValue, onChange }: QuizContentBuilderProps) {
  // State to track content
  const [content, setContent] = useState<QuizContent>({ questions: [] });
  
  // Load initial value if provided - only run once on mount
  useEffect(() => {
    if (initialValue) {
      try {
        const parsedContent = JSON.parse(initialValue);
        if (parsedContent && Array.isArray(parsedContent.questions)) {
          setContent(parsedContent);
        }
      } catch (e) {
        console.error('Failed to parse initial content:', e);
        // Initialize with empty content if parsing fails
        setContent({ questions: [] });
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

  // Generate a unique ID for new questions
  const generateId = () => `question_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

  // Add a new empty question
  const addQuestion = () => {
    const newQuestionNumber = content.questions.length + 1;
    const newQuestion: QuizQuestion = {
      id: generateId(),
      questionNumber: newQuestionNumber,
      question: '',
      options: ['', '', '', ''],
      answer: 0, // Default to first option
    };
    setContent(prev => ({
      ...prev,
      questions: [...prev.questions, newQuestion]
    }));
  };

  // Remove a question by ID
  const removeQuestion = (id: string) => {
    setContent(prev => {
      const updatedQuestions = prev.questions.filter(question => question.id !== id);
      // Re-number remaining questions
      return {
        ...prev,
        questions: updatedQuestions.map((question, index) => ({
          ...question,
          questionNumber: index + 1
        }))
      };
    });
  };

  // Update question content
  const updateQuestion = (id: string, field: keyof QuizQuestion, value: any) => {
    setContent(prev => {
      const updatedQuestions = prev.questions.map(question => {
        if (question.id === id) {
          return {
            ...question,
            [field]: value
          };
        }
        return question;
      });
      return {
        ...prev,
        questions: updatedQuestions
      };
    });
  };

  // Update a specific option within a question
  const updateOption = (questionId: string, optionIndex: number, value: string) => {
    setContent(prev => {
      const updatedQuestions = prev.questions.map(question => {
        if (question.id === questionId) {
          const newOptions = [...question.options];
          newOptions[optionIndex] = value;
          return {
            ...question,
            options: newOptions
          };
        }
        return question;
      });
      return {
        ...prev,
        questions: updatedQuestions
      };
    });
  };

  // Add a new option to a question
  const addOption = (questionId: string) => {
    setContent(prev => {
      const updatedQuestions = prev.questions.map(question => {
        if (question.id === questionId) {
          return {
            ...question,
            options: [...question.options, '']
          };
        }
        return question;
      });
      return {
        ...prev,
        questions: updatedQuestions
      };
    });
  };

  // Remove an option from a question
  const removeOption = (questionId: string, optionIndex: number) => {
    setContent(prev => {
      const updatedQuestions = prev.questions.map(question => {
        if (question.id === questionId) {
          // Ensure we keep at least 2 options
          if (question.options.length <= 2) {
            return question;
          }
          
          const newOptions = question.options.filter((_, index) => index !== optionIndex);
          
          // Adjust correct answer index if needed
          let newAnswerIndex = question.answer;
          if (optionIndex === question.answer) {
            // If we're removing the correct answer, default to the first option
            newAnswerIndex = 0;
          } else if (optionIndex < question.answer) {
            // If we're removing an option before the correct answer, decrement the index
            newAnswerIndex = question.answer - 1;
          }
          
          return {
            ...question,
            options: newOptions,
            answer: newAnswerIndex
          };
        }
        return question;
      });
      return {
        ...prev,
        questions: updatedQuestions
      };
    });
  };

  // Move question up in the list (decrease question number)
  const moveQuestionUp = (id: string) => {
    setContent(prev => {
      const questionIndex = prev.questions.findIndex(question => question.id === id);
      if (questionIndex <= 0) return prev; // Already at the top
      
      const newQuestions = [...prev.questions];
      // Swap with the previous question
      [newQuestions[questionIndex - 1], newQuestions[questionIndex]] = [newQuestions[questionIndex], newQuestions[questionIndex - 1]];
      
      // Update question numbers
      return {
        ...prev,
        questions: newQuestions.map((question, index) => ({
          ...question,
          questionNumber: index + 1
        }))
      };
    });
  };

  // Move question down in the list (increase question number)
  const moveQuestionDown = (id: string) => {
    setContent(prev => {
      const questionIndex = prev.questions.findIndex(question => question.id === id);
      if (questionIndex === -1 || questionIndex >= prev.questions.length - 1) return prev; // Already at the bottom
      
      const newQuestions = [...prev.questions];
      // Swap with the next question
      [newQuestions[questionIndex], newQuestions[questionIndex + 1]] = [newQuestions[questionIndex + 1], newQuestions[questionIndex]];
      
      // Update question numbers
      return {
        ...prev,
        questions: newQuestions.map((question, index) => ({
          ...question,
          questionNumber: index + 1
        }))
      };
    });
  };

  // State to track which questions are expanded
  const [expandedQuestions, setExpandedQuestions] = useState<{ [key: string]: boolean }>({});

  // Toggle question expansion
  const toggleQuestion = (id: string) => {
    setExpandedQuestions(prev => ({
      ...prev,
      [id]: !prev[id]
    }));
  };

  return (
    <div className="flex flex-col h-full space-y-4" style={{ maxHeight: 'calc(100vh - 300px)', minHeight: '400px' }}>
      <div className="flex justify-between items-center sticky top-0 bg-background z-10 py-2">
        <h3 className="text-lg font-medium">Quiz Content Builder</h3>
        <Button 
          type="button" 
          onClick={(e) => {
            e.preventDefault(); // Prevent any form submission
            e.stopPropagation(); // Stop event bubbling
            addQuestion();
          }} 
          size="sm" 
          className="flex items-center gap-1"
        >
          <PlusCircle className="h-4 w-4" />
          Add Question
        </Button>
      </div>

      <div className="flex-1 overflow-y-auto pr-1 pb-4" style={{ scrollbarWidth: 'thin' }}>
        {content.questions.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-40 border border-dashed rounded-md p-6 text-center text-muted-foreground">
            <FileQuestion className="h-12 w-12 mb-2 opacity-50" />
            <p>No questions added yet. Click "Add Question" to start building your quiz.</p>
          </div>
        ) : (
          <div className="space-y-4">
            {content.questions.map((question) => (
              <Card key={question.id} className="relative border-l-4 shadow-sm hover:shadow">
                <CardHeader className="pb-2 pt-3 cursor-pointer" onClick={() => toggleQuestion(question.id)}>
                  <div className="flex justify-between items-center">
                    <CardTitle className="text-base flex items-center gap-2">
                      Question {question.questionNumber}
                      {expandedQuestions[question.id] ? 
                        <ChevronUp className="h-4 w-4" /> : 
                        <ChevronDown className="h-4 w-4" />
                      }
                    </CardTitle>
                    <div className="flex items-center gap-1">
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={(e) => {
                          e.stopPropagation();
                          moveQuestionUp(question.id);
                        }}
                        disabled={question.questionNumber === 1}
                      >
                        <MoveUp className="h-4 w-4" />
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8"
                        onClick={(e) => {
                          e.stopPropagation();
                          moveQuestionDown(question.id);
                        }}
                        disabled={question.questionNumber === content.questions.length}
                      >
                        <MoveDown className="h-4 w-4" />
                      </Button>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-destructive"
                        onClick={(e) => {
                          e.stopPropagation();
                          removeQuestion(question.id);
                        }}
                      >
                        <Trash2 className="h-4 w-4" />
                      </Button>
                    </div>
                  </div>
                  <CardDescription className="line-clamp-1">
                    {question.question || "No question text yet..."}
                  </CardDescription>
                </CardHeader>

                {expandedQuestions[question.id] && (
                  <CardContent className="pt-2 pb-4">
                    <div className="space-y-4">
                      <div>
                        <Label htmlFor={`question-${question.id}`}>Question Text</Label>
                        <Textarea
                          id={`question-${question.id}`}
                          value={question.question}
                          onChange={(e) => updateQuestion(question.id, 'question', e.target.value)}
                          placeholder="Enter your question here..."
                          className="mt-1"
                        />
                      </div>

                      <div>
                        <div className="flex justify-between items-center mb-2">
                          <Label>Answer Options</Label>
                          <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            onClick={(e) => {
                              e.preventDefault();
                              addOption(question.id);
                            }}
                            disabled={question.options.length >= 6}
                            className="h-7 text-xs"
                          >
                            <Plus className="h-3 w-3 mr-1" /> Add Option
                          </Button>
                        </div>

                        <RadioGroup 
                          value={question.answer.toString()} 
                          onValueChange={(value: string) => updateQuestion(question.id, 'answer', parseInt(value))}
                          className="space-y-2"
                        >
                          {question.options.map((option, index) => (
                            <div key={`${question.id}-option-${index}`} className="flex items-center gap-2">
                              <RadioGroupItem value={index.toString()} id={`${question.id}-option-${index}`} />
                              <div className="flex-1">
                                <Input
                                  value={option}
                                  onChange={(e) => updateOption(question.id, index, e.target.value)}
                                  placeholder={`Option ${index + 1}`}
                                  className="flex-1"
                                />
                              </div>
                              {question.options.length > 2 && (
                                <Button
                                  type="button"
                                  variant="ghost"
                                  size="icon"
                                  className="h-8 w-8 text-destructive"
                                  onClick={() => removeOption(question.id, index)}
                                >
                                  <X className="h-4 w-4" />
                                </Button>
                              )}
                            </div>
                          ))}
                        </RadioGroup>
                        <p className="text-sm text-muted-foreground mt-2">
                          Select the radio button next to the correct answer.
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
    </div>
  );
}
