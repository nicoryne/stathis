'use client';

import { motion } from 'framer-motion';
import { useReducedMotion } from '@/hooks/use-reduced-motion';
import { useState } from 'react';
import { Activity, HeartPulse, CheckCircle, AlertCircle, Smartphone, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

interface DemoTabsProps {
  className?: string;
}

export function InteractiveDemos({ className = '' }: DemoTabsProps) {
  const prefersReducedMotion = useReducedMotion();
  const [activeTab, setActiveTab] = useState<'posture' | 'vitals' | 'smartwatch'>('posture');
  
  return (
    <div className={`w-full ${className}`}>
      <div className="flex flex-wrap gap-2 mb-6 justify-center">
        <Button 
          variant={activeTab === 'posture' ? 'default' : 'outline'} 
          onClick={() => setActiveTab('posture')}
          className="flex items-center gap-2"
        >
          <Activity className="h-4 w-4" />
          Posture Detection
        </Button>
        <Button 
          variant={activeTab === 'vitals' ? 'default' : 'outline'} 
          onClick={() => setActiveTab('vitals')}
          className="flex items-center gap-2"
        >
          <HeartPulse className="h-4 w-4" />
          Vitals Dashboard
        </Button>
        <Button 
          variant={activeTab === 'smartwatch' ? 'default' : 'outline'} 
          onClick={() => setActiveTab('smartwatch')}
          className="flex items-center gap-2"
        >
          <Smartphone className="h-4 w-4" />
          Smartwatch Connection
        </Button>
      </div>
      
      <div className="relative bg-card border border-border rounded-xl overflow-hidden">
        {/* Posture Detection Demo */}
        <motion.div 
          className="p-6"
          initial={{ opacity: 0, x: 20 }}
          animate={{ 
            opacity: activeTab === 'posture' ? 1 : 0,
            x: activeTab === 'posture' ? 0 : 20,
            display: activeTab === 'posture' ? 'block' : 'none'
          }}
          transition={{ duration: prefersReducedMotion ? 0 : 0.3 }}
        >
          <div className="relative aspect-video bg-muted rounded-lg overflow-hidden">
            <div className="absolute inset-0 flex items-center justify-center">
              {/* Skeleton overlay */}
              <svg width="280" height="300" viewBox="0 0 280 300" fill="none" xmlns="http://www.w3.org/2000/svg" className="opacity-70">
                {/* Head */}
                <circle cx="140" cy="40" r="20" stroke="currentColor" strokeWidth="2" className="text-primary" />
                
                {/* Body */}
                <line x1="140" y1="60" x2="140" y2="150" stroke="currentColor" strokeWidth="2" className="text-primary" />
                
                {/* Arms */}
                <line x1="140" y1="80" x2="90" y2="120" stroke="currentColor" strokeWidth="2" className="text-primary" />
                <line x1="140" y1="80" x2="190" y2="120" stroke="currentColor" strokeWidth="2" className="text-primary" />
                
                {/* Legs */}
                <line x1="140" y1="150" x2="110" y2="220" stroke="currentColor" strokeWidth="2" className="text-primary" />
                <line x1="140" y1="150" x2="170" y2="220" stroke="currentColor" strokeWidth="2" className="text-primary" />
                
                {/* Joints */}
                <circle cx="140" cy="80" r="5" fill="currentColor" className="text-secondary" />
                <circle cx="90" cy="120" r="5" fill="currentColor" className="text-secondary" />
                <circle cx="190" cy="120" r="5" fill="currentColor" className="text-secondary" />
                <circle cx="140" cy="150" r="5" fill="currentColor" className="text-secondary" />
                <circle cx="110" cy="220" r="5" fill="currentColor" className="text-secondary" />
                <circle cx="170" cy="220" r="5" fill="currentColor" className="text-secondary" />
                
                {/* Angle indicators */}
                <motion.path 
                  d="M 140,80 C 130,90 120,100 90,120" 
                  stroke="currentColor" 
                  strokeWidth="1" 
                  strokeDasharray="3 3" 
                  className="text-green-500"
                  animate={{ opacity: [0.5, 1, 0.5] }}
                  transition={{ duration: 2, repeat: Infinity }}
                />
                
                <motion.text 
                  x="115" 
                  y="100" 
                  className="text-xs text-green-500 fill-current"
                  animate={{ opacity: [0.7, 1, 0.7] }}
                  transition={{ duration: 2, repeat: Infinity }}
                >
                  135°
                </motion.text>
              </svg>
              
              <motion.div
                className="absolute bottom-4 left-4 right-4 bg-background/80 backdrop-blur-sm p-3 rounded-lg border border-border"
                animate={{ y: [0, -5, 0] }}
                transition={{ duration: 2, repeat: Infinity }}
              >
                <div className="flex justify-between items-center">
                  <div className="flex items-center gap-2">
                    <CheckCircle className="h-5 w-5 text-green-500" />
                    <span className="font-medium">Correct Form Detected</span>
                  </div>
                  <Badge variant="outline" className="bg-green-500/10 text-green-500">
                    95% Confidence
                  </Badge>
                </div>
              </motion.div>
            </div>
          </div>
          
          <div className="mt-4 text-sm text-muted-foreground">
            <p>Stathis uses TensorFlow Lite and OpenCV to analyze joint angles and movement patterns in real-time, providing immediate feedback on exercise form.</p>
          </div>
        </motion.div>
        
        {/* Vitals Dashboard Demo */}
        <motion.div 
          className="p-6"
          initial={{ opacity: 0, x: 20 }}
          animate={{ 
            opacity: activeTab === 'vitals' ? 1 : 0,
            x: activeTab === 'vitals' ? 0 : 20,
            display: activeTab === 'vitals' ? 'block' : 'none'
          }}
          transition={{ duration: prefersReducedMotion ? 0 : 0.3 }}
        >
          <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
            {/* Student 1 */}
            <div className="bg-muted p-3 rounded-lg border border-border">
              <div className="flex justify-between items-center mb-2">
                <span className="font-medium text-sm">John D.</span>
                <Badge variant="outline" className="bg-green-500/10 text-green-500 text-xs">Normal</Badge>
              </div>
              <div className="flex items-center gap-2 mb-1">
                <HeartPulse className="h-4 w-4 text-primary" />
                <span className="text-xs">HR: 85 bpm</span>
              </div>
              <div className="flex items-center gap-2">
                <Activity className="h-4 w-4 text-secondary" />
                <span className="text-xs">SpO2: 98%</span>
              </div>
            </div>
            
            {/* Student 2 */}
            <div className="bg-muted p-3 rounded-lg border border-border">
              <div className="flex justify-between items-center mb-2">
                <span className="font-medium text-sm">Emma S.</span>
                <motion.div
                  animate={{ scale: [1, 1.1, 1] }}
                  transition={{ duration: 1, repeat: Infinity }}
                >
                  <Badge variant="outline" className="bg-amber-500/10 text-amber-500 text-xs">Caution</Badge>
                </motion.div>
              </div>
              <div className="flex items-center gap-2 mb-1">
                <HeartPulse className="h-4 w-4 text-primary" />
                <motion.span 
                  className="text-xs text-amber-500"
                  animate={{ opacity: [0.7, 1, 0.7] }}
                  transition={{ duration: 1, repeat: Infinity }}
                >
                  HR: 145 bpm
                </motion.span>
              </div>
              <div className="flex items-center gap-2">
                <Activity className="h-4 w-4 text-secondary" />
                <span className="text-xs">SpO2: 97%</span>
              </div>
            </div>
            
            {/* Student 3 */}
            <div className="bg-muted p-3 rounded-lg border border-border">
              <div className="flex justify-between items-center mb-2">
                <span className="font-medium text-sm">Alex W.</span>
                <Badge variant="outline" className="bg-green-500/10 text-green-500 text-xs">Normal</Badge>
              </div>
              <div className="flex items-center gap-2 mb-1">
                <HeartPulse className="h-4 w-4 text-primary" />
                <span className="text-xs">HR: 92 bpm</span>
              </div>
              <div className="flex items-center gap-2">
                <Activity className="h-4 w-4 text-secondary" />
                <span className="text-xs">SpO2: 99%</span>
              </div>
            </div>
            
            {/* Student 4 */}
            <div className="bg-muted p-3 rounded-lg border border-border">
              <div className="flex justify-between items-center mb-2">
                <span className="font-medium text-sm">Sarah J.</span>
                <Badge variant="outline" className="bg-green-500/10 text-green-500 text-xs">Normal</Badge>
              </div>
              <div className="flex items-center gap-2 mb-1">
                <HeartPulse className="h-4 w-4 text-primary" />
                <span className="text-xs">HR: 88 bpm</span>
              </div>
              <div className="flex items-center gap-2">
                <Activity className="h-4 w-4 text-secondary" />
                <span className="text-xs">SpO2: 98%</span>
              </div>
            </div>
            
            {/* Student 5 */}
            <div className="bg-muted p-3 rounded-lg border border-border">
              <div className="flex justify-between items-center mb-2">
                <span className="font-medium text-sm">Mike T.</span>
                <Badge variant="outline" className="bg-green-500/10 text-green-500 text-xs">Normal</Badge>
              </div>
              <div className="flex items-center gap-2 mb-1">
                <HeartPulse className="h-4 w-4 text-primary" />
                <span className="text-xs">HR: 78 bpm</span>
              </div>
              <div className="flex items-center gap-2">
                <Activity className="h-4 w-4 text-secondary" />
                <span className="text-xs">SpO2: 99%</span>
              </div>
            </div>
            
            {/* Student 6 */}
            <div className="bg-muted p-3 rounded-lg border border-border">
              <div className="flex justify-between items-center mb-2">
                <span className="font-medium text-sm">Lisa R.</span>
                <motion.div
                  animate={{ scale: [1, 1.2, 1] }}
                  transition={{ duration: 0.8, repeat: Infinity }}
                >
                  <Badge variant="outline" className="bg-red-500/10 text-red-500 text-xs">Alert</Badge>
                </motion.div>
              </div>
              <div className="flex items-center gap-2 mb-1">
                <HeartPulse className="h-4 w-4 text-primary" />
                <span className="text-xs">HR: 95 bpm</span>
              </div>
              <div className="flex items-center gap-2">
                <Activity className="h-4 w-4 text-secondary" />
                <motion.span 
                  className="text-xs text-red-500"
                  animate={{ opacity: [0.7, 1, 0.7] }}
                  transition={{ duration: 0.8, repeat: Infinity }}
                >
                  SpO2: 92%
                </motion.span>
              </div>
            </div>
          </div>
          
          <div className="mt-4 text-sm text-muted-foreground">
            <p>Teachers receive real-time health data from all students, with automatic alerts for concerning vital signs.</p>
          </div>
        </motion.div>
        
        {/* Smartwatch Connection Demo */}
        <motion.div 
          className="p-6"
          initial={{ opacity: 0, x: 20 }}
          animate={{ 
            opacity: activeTab === 'smartwatch' ? 1 : 0,
            x: activeTab === 'smartwatch' ? 0 : 20,
            display: activeTab === 'smartwatch' ? 'block' : 'none'
          }}
          transition={{ duration: prefersReducedMotion ? 0 : 0.3 }}
        >
          <div className="flex flex-col md:flex-row gap-6 items-center justify-center">
            <div className="relative w-32 h-48 bg-black rounded-xl overflow-hidden border-4 border-gray-800">
              <div className="absolute inset-0 flex items-center justify-center bg-gray-900">
                <div className="text-center">
                  <motion.div
                    animate={{ scale: [1, 1.1, 1] }}
                    transition={{ duration: 1.5, repeat: Infinity }}
                  >
                    <HeartPulse className="h-8 w-8 text-primary mx-auto" />
                  </motion.div>
                  <div className="mt-2 text-xs text-white">HR: 78 BPM</div>
                  <div className="mt-1 text-xs text-white">SpO2: 98%</div>
                </div>
              </div>
            </div>
            
            <motion.div
              className="flex-shrink-0 w-16 relative"
              animate={{ 
                x: prefersReducedMotion ? 0 : [-5, 5, -5],
                opacity: [0.5, 1, 0.5]
              }}
              transition={{ duration: 2, repeat: Infinity }}
            >
              <svg width="100%" height="2" className="absolute top-1/2 left-0 right-0">
                <line x1="0" y1="1" x2="100%" y2="1" stroke="currentColor" strokeWidth="2" strokeDasharray="4 2" className="text-primary" />
              </svg>
              <div className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 bg-primary/10 rounded-full p-2">
                <motion.div
                  animate={{ rotate: 360 }}
                  transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                >
                  <Loader2 className="h-5 w-5 text-primary" />
                </motion.div>
              </div>
            </motion.div>
            
            <div className="relative w-48 h-96 bg-muted rounded-xl overflow-hidden border border-border">
              <div className="absolute inset-0 flex flex-col">
                <div className="bg-primary h-12 flex items-center justify-center text-white text-sm font-medium">
                  Stathis App
                </div>
                <div className="flex-1 p-4">
                  <div className="text-center mb-4">
                    <motion.div
                      className="inline-block"
                      animate={{ scale: [1, 1.1, 1] }}
                      transition={{ duration: 1.5, repeat: Infinity }}
                    >
                      <Smartphone className="h-10 w-10 text-primary mx-auto" />
                    </motion.div>
                    <p className="text-xs mt-2">Connected to Xiaomi Smart Band</p>
                  </div>
                  
                  <div className="space-y-3">
                    <div className="bg-card p-2 rounded-md">
                      <div className="text-xs font-medium">Heart Rate</div>
                      <div className="flex items-center">
                        <HeartPulse className="h-4 w-4 text-primary mr-1" />
                        <span className="text-sm">78 BPM</span>
                      </div>
                    </div>
                    
                    <div className="bg-card p-2 rounded-md">
                      <div className="text-xs font-medium">Blood Oxygen</div>
                      <div className="flex items-center">
                        <Activity className="h-4 w-4 text-secondary mr-1" />
                        <span className="text-sm">98%</span>
                      </div>
                    </div>
                    
                    <div className="bg-card p-2 rounded-md">
                      <div className="text-xs font-medium">Status</div>
                      <div className="flex items-center">
                        <CheckCircle className="h-4 w-4 text-green-500 mr-1" />
                        <span className="text-sm">Data streaming</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div className="mt-4 text-sm text-muted-foreground">
            <p>Stathis seamlessly connects with Xiaomi Smart Band 9 Active to stream real-time health data to the teacher dashboard.</p>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
