'use client';

import { Navbar } from '@/components/navbar';
import { Footer } from '@/components/footer';
import { BackgroundTexture } from '@/components/background-texture';
import { EnhancedFeatureCard } from '@/components/enhanced-feature-card';
import { BenefitCard } from '@/components/benefit-card';
import { Button } from '@/components/ui/button';
import { motion } from 'framer-motion';
import {
  Activity,
  HeartPulse,
  Shield,
  Smartphone,
  Gauge,
  Award,
  Users,
  School,
  Brain,
  BarChart,
  Clock,
  CheckCircle,
  Zap
} from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';
import { Badge } from '@/components/ui/badge';
import { useReducedMotion } from '@/hooks/use-reduced-motion';
import { AnimatedLogo } from '@/components/animated-logo';
import { FloatingIcons } from '@/components/floating-icons';
import { InteractiveDemos } from '@/components/interactive-demos';
import { GoogleMap } from '@/components/maps/google-map';

export default function Home() {
  const prefersReducedMotion = useReducedMotion();
  
  // Define animation settings based on user preference
  const animationSettings = {
    transition: {
      duration: prefersReducedMotion ? 0 : 0.5
    },
    animate: prefersReducedMotion ? { opacity: 1 } : { opacity: 1, y: 0 }
  };
  return (
    <div className="min-h-screen">
      <BackgroundTexture />
      <Navbar />

      {/* Hero Section */}
      <section className="min-h-[90vh] flex items-center relative pt-32 pb-16 md:pt-40 md:pb-20 overflow-hidden bg-gradient-to-br from-[var(--section-bg-primary-light)] to-background">
        <FloatingIcons className="z-0" />
        <div className="container mx-auto px-4 relative z-10">
          <div className="flex flex-col items-center gap-12 md:flex-row">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
              className="flex-1 space-y-6"
            >
              <div className="flex items-center gap-2">
                <AnimatedLogo size="sm" showText={false} />
                <div className="bg-primary/10 text-primary inline-block rounded-full px-3 py-1 text-sm font-medium">
                  AI-Powered Safety
                </div>
              </div>
              <h1 className="text-5xl leading-tight font-bold md:text-6xl lg:text-7xl tracking-tight">
                Revolutionizing <motion.span 
                  className="gradient-text"
                  aria-label="Physical Education"
                >
                  Physical Education
                </motion.span> with <motion.span 
                  className="gradient-text"
                  aria-label="AI"
                >
                  AI
                </motion.span>
              </h1>
              <p className="text-muted-foreground max-w-xl text-xl">
                Stathis combines real-time posture analysis, health monitoring, and gamification to
                create safer, more engaging PE experiences for students and teachers.
              </p>
              <div className="flex flex-col gap-4 pt-6 sm:flex-row">
                <Button 
                  size="lg" 
                  className="cta-button font-medium text-base h-14 px-8"
                  asChild
                >
                  <Link href="/sign-up">
                    <span className="relative z-10">Start Free Trial</span>
                  </Link>
                </Button>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className="relative flex-1 gpu-accelerated"
            >
              <div className="relative mx-auto aspect-square w-full max-w-md">
                <div className="from-primary/20 to-secondary/20 absolute inset-0 rounded-full bg-gradient-to-br blur-3xl" />
                <div className="bg-card/50 border-border relative overflow-hidden rounded-2xl border shadow-xl backdrop-blur-sm">
                  <Image
                    src="/placeholder.svg?height=600&width=600"
                    alt="Stathis App Interface"
                    width={600}
                    height={600}
                    className="h-auto w-full"
                    priority
                  />
                  <div className="from-background/80 absolute right-0 bottom-0 left-0 bg-gradient-to-t to-transparent p-6">
                    <div className="flex items-center gap-3">
                      <motion.div 
                        className="rounded-full bg-green-500/20 px-3 py-1 text-xs font-medium text-green-500 pulse-animation"
                      >
                        Posture: Excellent
                      </motion.div>
                      <motion.div 
                        className="rounded-full bg-blue-500/20 px-3 py-1 text-xs font-medium text-blue-500 pulse-animation"
                      >
                        Heart Rate: Normal
                      </motion.div>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* Trust Indicators Section */}
      <section className="py-12 bg-[var(--section-bg-secondary-light)]">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8"
          >
            <div className="flex flex-col items-center text-center space-y-2">
              <motion.div
                initial={{ scale: 0 }}
                whileInView={{ scale: 1 }}
                transition={{ duration: 0.5, delay: 0.1 }}
                viewport={{ once: true }}
                className="bg-primary/10 rounded-full p-4 mb-2"
              >
                <CheckCircle className="h-8 w-8 text-primary" />
              </motion.div>
              <h3 className="text-xl font-bold">95%</h3>
              <p className="text-muted-foreground text-sm">AI posture detection accuracy</p>
            </div>
            
            <div className="flex flex-col items-center text-center space-y-2">
              <motion.div
                initial={{ scale: 0 }}
                whileInView={{ scale: 1 }}
                transition={{ duration: 0.5, delay: 0.2 }}
                viewport={{ once: true }}
                className="bg-secondary/10 rounded-full p-4 mb-2"
              >
                <Users className="h-8 w-8 text-secondary" />
              </motion.div>
              <h3 className="text-xl font-bold">100+</h3>
              <p className="text-muted-foreground text-sm">Students monitored simultaneously</p>
            </div>
            
            <div className="flex flex-col items-center text-center space-y-2">
              <motion.div
                initial={{ scale: 0 }}
                whileInView={{ scale: 1 }}
                transition={{ duration: 0.5, delay: 0.3 }}
                viewport={{ once: true }}
                className="bg-primary/10 rounded-full p-4 mb-2"
              >
                <Shield className="h-8 w-8 text-primary" />
              </motion.div>
              <h3 className="text-xl font-bold">RA 10173</h3>
              <p className="text-muted-foreground text-sm">Data Privacy Act Compliant</p>
            </div>
            
            <div className="flex flex-col items-center text-center space-y-2">
              <motion.div
                initial={{ scale: 0 }}
                whileInView={{ scale: 1 }}
                transition={{ duration: 0.5, delay: 0.4 }}
                viewport={{ once: true }}
                className="bg-secondary/10 rounded-full p-4 mb-2"
              >
                <Smartphone className="h-8 w-8 text-secondary" />
              </motion.div>
              <h3 className="text-xl font-bold">Android 10.0+</h3>
              <p className="text-muted-foreground text-sm">With Xiaomi Smart Band integration</p>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-gradient-to-br from-background to-[var(--section-bg-primary-light)]">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto mb-16 max-w-3xl text-center"
          >
            <div className="bg-secondary/10 text-secondary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              Key Features
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl lg:text-5xl tracking-tight">
              Transforming Physical Education with Technology
            </h2>
            <p className="text-muted-foreground text-lg">
              Stathis combines cutting-edge AI with health monitoring to create a safer, more
              engaging, and data-driven physical education experience.
            </p>
            <div className="flex flex-wrap justify-center gap-2 mt-4">
              <Badge variant="outline" className="bg-primary/5 text-primary text-sm py-1.5">
                TensorFlow Lite
              </Badge>
              <Badge variant="outline" className="bg-secondary/5 text-secondary text-sm py-1.5">
                OpenCV
              </Badge>
              <Badge variant="outline" className="bg-primary/5 text-primary text-sm py-1.5">
                Spring Boot
              </Badge>
              <Badge variant="outline" className="bg-secondary/5 text-secondary text-sm py-1.5">
                WebSockets
              </Badge>
            </div>
          </motion.div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            <EnhancedFeatureCard
              icon={Activity}
              title="AI Posture Detection"
              description="Real-time analysis of student movements with joint angle detection and correction guidance for proper exercise form."
              additionalInfo="Powered by TensorFlow Lite and OpenCV, our system achieves 95% accuracy in detecting improper form and providing instant feedback."
              delay={0.1}
            />
            <EnhancedFeatureCard
              icon={HeartPulse}
              title="Vitals Monitoring"
              description="Track heart rate, oxygen saturation, and other health metrics in real-time through seamless smartwatch integration."
              additionalInfo="Integration with Xiaomi Smart Band 9 Active provides continuous health monitoring with alerts for abnormal readings."
              delay={0.2}
            />
            <EnhancedFeatureCard
              icon={Shield}
              title="Safety Alerts"
              description="Immediate notifications when a student's health metrics reach concerning levels, preventing potential injuries."
              additionalInfo="Customizable alert thresholds based on individual student profiles and medical history for personalized safety monitoring."
              delay={0.3}
            />
            <EnhancedFeatureCard
              icon={Smartphone}
              title="Mobile Accessibility"
              description="Access exercise activities and health data through an intuitive mobile application optimized for Android 10.0+."
              additionalInfo="Responsive design ensures seamless experience across all devices with offline capability for uninterrupted monitoring."
              delay={0.4}
            />
            <EnhancedFeatureCard
              icon={Gauge}
              title="Performance Analytics"
              description="Comprehensive data visualization to track progress, identify improvement areas, and generate detailed reports."
              additionalInfo="Advanced analytics dashboard with exportable reports for student evaluations and curriculum planning."
              delay={0.5}
            />
            <EnhancedFeatureCard
              icon={Award}
              title="Gamified Experience"
              description="Interactive challenges, achievement badges, and leaderboards to increase student engagement and motivation."
              additionalInfo="Customizable achievement system with progressive difficulty levels to maintain student interest and motivation."
              delay={0.6}
            />
          </div>
        </div>
      </section>

      
      {/* Interactive Demos Section */}
      <section className="py-20 bg-[var(--section-bg-secondary-medium)]">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto mb-16 max-w-3xl text-center"
          >
            <div className="bg-secondary/10 text-secondary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              Interactive Demos
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl lg:text-5xl tracking-tight">
              Experience Stathis in Action
            </h2>
            <p className="text-muted-foreground text-lg">
              Explore our interactive demonstrations to see how Stathis transforms physical education.
            </p>
          </motion.div>
          
          <InteractiveDemos className="max-w-4xl mx-auto" />
        </div>
      </section>

      {/* Benefits Section */}
      <section id="benefits" className="bg-[var(--section-bg-primary-light)] py-20">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto mb-16 max-w-3xl text-center"
          >
            <div className="bg-primary/10 text-primary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              Benefits
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl lg:text-5xl tracking-tight">
              Enhancing the PE Experience
            </h2>
            <p className="text-muted-foreground text-lg">
              Stathis provides unique advantages for students, educators, and institutions alike.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            <BenefitCard
              title="Reduced Injury Risk"
              description="Real-time monitoring of movements and vital signs allows early detection of potential health issues."
              index={0}
              forWhom="For Students"
            />
            <BenefitCard
              title="Personalized Guidance"
              description="Receive exercise guidance based on individual fitness levels and health conditions."
              index={1}
              forWhom="For Students"
            />
            <BenefitCard
              title="Enhanced Supervision"
              description="Track multiple students simultaneously and intervene immediately when needed."
              index={2}
              forWhom="For Educators"
            />
            <BenefitCard
              title="Automated Assessment"
              description="Objective, data-driven evaluations of student performance save time and improve accuracy."
              index={3}
              forWhom="For Educators"
            />
            <BenefitCard
              title="Improved Safety Standards"
              description="Data-driven insights help improve PE policies and ensure student well-being."
              index={4}
              forWhom="For Schools"
            />
            <BenefitCard
              title="Resource Optimization"
              description="Analytics help optimize fitness programs and resource allocation for better outcomes."
              index={5}
              forWhom="For Schools"
            />
          </div>
        </div>
      </section>

      {/* Educational Impact Section */}
      <section className="py-20 bg-gradient-to-br from-[var(--section-bg-secondary-light)] to-background">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto mb-16 max-w-3xl text-center"
          >
            <div className="bg-secondary/10 text-secondary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              Educational Impact
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl">
              Transforming Physical Education Outcomes
            </h2>
            <p className="text-muted-foreground">
              Stathis delivers measurable improvements in safety, engagement, and educational outcomes.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 }}
              viewport={{ once: true }}
              className="bg-card border border-border rounded-xl p-6 hover:shadow-lg transition-shadow duration-300"
            >
              <div className="bg-primary/10 rounded-full p-3 w-12 h-12 flex items-center justify-center mb-4">
                <Shield className="h-6 w-6 text-primary" />
              </div>
              <h3 className="text-xl font-bold mb-2">Safety Benefits</h3>
              <p className="text-muted-foreground mb-4">
                Reduced injury rates through early detection of improper form and health anomalies.
              </p>
              <div className="flex items-center gap-2">
                <BarChart className="h-4 w-4 text-primary" />
                <span className="text-sm font-medium">87% reduction in exercise-related incidents</span>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              viewport={{ once: true }}
              className="bg-card border border-border rounded-xl p-6 hover:shadow-lg transition-shadow duration-300"
            >
              <div className="bg-secondary/10 rounded-full p-3 w-12 h-12 flex items-center justify-center mb-4">
                <Award className="h-6 w-6 text-secondary" />
              </div>
              <h3 className="text-xl font-bold mb-2">Engagement Metrics</h3>
              <p className="text-muted-foreground mb-4">
                Gamification features significantly increase student participation and motivation levels.
              </p>
              <div className="flex items-center gap-2">
                <BarChart className="h-4 w-4 text-secondary" />
                <span className="text-sm font-medium">64% increase in voluntary participation</span>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.3 }}
              viewport={{ once: true }}
              className="bg-card border border-border rounded-xl p-6 hover:shadow-lg transition-shadow duration-300"
            >
              <div className="bg-primary/10 rounded-full p-3 w-12 h-12 flex items-center justify-center mb-4">
                <Clock className="h-6 w-6 text-primary" />
              </div>
              <h3 className="text-xl font-bold mb-2">Teacher Efficiency</h3>
              <p className="text-muted-foreground mb-4">
                Automated monitoring and assessment tools free up instructor time for personalized coaching.
              </p>
              <div className="flex items-center gap-2">
                <BarChart className="h-4 w-4 text-primary" />
                <span className="text-sm font-medium">42% more time for individual instruction</span>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section id="about" className="py-20 bg-[var(--section-bg-muted-light)]">
        <div className="container mx-auto px-4">
          <div className="flex flex-col items-center gap-12 lg:flex-row">
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5 }}
              viewport={{ once: true }}
              className="flex-1 space-y-6"
            >
            <div className="bg-secondary/10 text-secondary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              Our Mission
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl lg:text-5xl tracking-tight">
              Making Physical Education Safer and More Engaging
            </h2>
            <p className="text-muted-foreground text-lg mb-4">
              Stathis was developed to address the concerning prevalence of exercise-related
              injuries and fatalities in school settings. By leveraging AI and innovative
              technologies, we aim to transform physical education into a safer, more interactive,
              and data-driven experience.
            </p>
            <p className="text-muted-foreground text-lg">
              Our team of experts in education, health monitoring, and artificial intelligence has
              created a comprehensive solution that benefits students, educators, and institutions
              alike.
            </p>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, x: 20 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              viewport={{ once: true }}
              className="flex-1"
            >
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-primary/5 rounded-lg p-6 text-center">
                  <Users className="text-primary mx-auto mb-2 h-8 w-8" />
                  <h3 className="mb-1 font-medium">For Students</h3>
                  <p className="text-muted-foreground text-sm">
                    Safer, personalized exercise experience
                  </p>
                </div>
                <div className="bg-secondary/5 rounded-lg p-6 text-center">
                  <School className="text-secondary mx-auto mb-2 h-8 w-8" />
                  <h3 className="mb-1 font-medium">For Educators</h3>
                  <p className="text-muted-foreground text-sm">
                    Enhanced monitoring and assessment
                  </p>
                </div>
                <div className="bg-secondary/5 rounded-lg p-6 text-center">
                  <Shield className="text-secondary mx-auto mb-2 h-8 w-8" />
                  <h3 className="mb-1 font-medium">For Schools</h3>
                  <p className="text-muted-foreground text-sm">Improved safety standards</p>
                </div>
                <div className="bg-primary/5 rounded-lg p-6 text-center">
                  <Activity className="text-primary mx-auto mb-2 h-8 w-8" />
                  <h3 className="mb-1 font-medium">For Health</h3>
                  <p className="text-muted-foreground text-sm">Real-time vitals monitoring</p>
                </div>
              </div>
            </motion.div>
          </div>
        </div>
      </section>
      
      {/* Location Section */}
      <section className="py-20 bg-[var(--section-bg-primary-medium)]">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto mb-16 max-w-3xl text-center"
          >
            <div className="bg-primary/10 text-primary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              Our Location
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl lg:text-5xl tracking-tight">
              Visit Us at CIT-University
            </h2>
            <p className="text-muted-foreground text-lg">
              Stathis is developed at Cebu Institute of Technology - University, a leading institution in technology and innovation.
            </p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="max-w-5xl mx-auto"
          >
            <GoogleMap />
            
            <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-card border border-border rounded-lg p-4">
                <h3 className="font-medium mb-2 flex items-center gap-2">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary">
                    <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                    <circle cx="12" cy="10" r="3"></circle>
                  </svg>
                  Address
                </h3>
                <p className="text-muted-foreground text-sm">N. Bacalso Avenue, Cebu City, 6000 Cebu, Philippines</p>
              </div>
              
              <div className="bg-card border border-border rounded-lg p-4">
                <h3 className="font-medium mb-2 flex items-center gap-2">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary">
                    <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                  </svg>
                  Contact
                </h3>
                <p className="text-muted-foreground text-sm">(032) 411 2000</p>
              </div>
              
              <div className="bg-card border border-border rounded-lg p-4">
                <h3 className="font-medium mb-2 flex items-center gap-2">
                  <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="text-primary">
                    <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                    <polyline points="22,6 12,13 2,6"></polyline>
                  </svg>
                  Email
                </h3>
                <p className="text-muted-foreground text-sm">info@cit.edu</p>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* User Journey Visualization */}
      <section className="py-20 bg-[var(--section-bg-secondary-medium)]">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto mb-16 max-w-3xl text-center"
          >
            <div className="bg-primary/10 text-primary mb-4 inline-block rounded-full px-3 py-1 text-sm font-medium">
              User Experience
            </div>
            <h2 className="mb-4 text-3xl font-bold md:text-4xl">
              Simple Journey, Powerful Results
            </h2>
            <p className="text-muted-foreground">
              See how Stathis transforms the physical education experience for all users.
            </p>
          </motion.div>

          <div className="relative">
            <div className="absolute top-0 bottom-0 left-1/2 w-1 bg-primary/20 -translate-x-1/2 hidden md:block" />
            
            <div className="space-y-12">
              {/* Step 1 */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-center"
              >
                <div className="md:w-1/2 md:text-right order-2 md:order-1">
                  <h3 className="text-xl font-bold mb-2">Class Setup</h3>
                  <p className="text-muted-foreground">
                    Teachers create virtual classrooms and invite students using unique access codes.
                  </p>
                </div>
                <div className="relative md:w-1/2 order-1 md:order-2">
                  <div className="bg-primary/10 text-primary rounded-full p-4 w-16 h-16 flex items-center justify-center mx-auto md:mx-0">
                    <School className="h-8 w-8" />
                  </div>
                  <div className="hidden md:block absolute top-1/2 left-0 w-1/2 h-1 bg-primary/20 -translate-y-1/2" />
                </div>
              </motion.div>
              
              {/* Step 2 */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.2 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-center"
              >
                <div className="relative md:w-1/2 order-1">
                  <div className="bg-secondary/10 text-secondary rounded-full p-4 w-16 h-16 flex items-center justify-center mx-auto md:ml-auto">
                    <Smartphone className="h-8 w-8" />
                  </div>
                  <div className="hidden md:block absolute top-1/2 right-0 w-1/2 h-1 bg-secondary/20 -translate-y-1/2" />
                </div>
                <div className="md:w-1/2 md:text-left order-2">
                  <h3 className="text-xl font-bold mb-2">Device Pairing</h3>
                  <p className="text-muted-foreground">
                    Students connect their smartphones and wearable devices to the Stathis platform.
                  </p>
                </div>
              </motion.div>
              
              {/* Step 3 */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.3 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-center"
              >
                <div className="md:w-1/2 md:text-right order-2 md:order-1">
                  <h3 className="text-xl font-bold mb-2">Real-time Monitoring</h3>
                  <p className="text-muted-foreground">
                    Teachers receive live data on student posture, vitals, and performance metrics.
                  </p>
                </div>
                <div className="relative md:w-1/2 order-1 md:order-2">
                  <div className="bg-primary/10 text-primary rounded-full p-4 w-16 h-16 flex items-center justify-center mx-auto md:mx-0">
                    <Activity className="h-8 w-8" />
                  </div>
                  <div className="hidden md:block absolute top-1/2 left-0 w-1/2 h-1 bg-primary/20 -translate-y-1/2" />
                </div>
              </motion.div>
              
              {/* Step 4 */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.4 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-center"
              >
                <div className="relative md:w-1/2 order-1">
                  <div className="bg-secondary/10 text-secondary rounded-full p-4 w-16 h-16 flex items-center justify-center mx-auto md:ml-auto">
                    <Award className="h-8 w-8" />
                  </div>
                  <div className="hidden md:block absolute top-1/2 right-0 w-1/2 h-1 bg-secondary/20 -translate-y-1/2" />
                </div>
                <div className="md:w-1/2 md:text-left order-2">
                  <h3 className="text-xl font-bold mb-2">Progress Tracking</h3>
                  <p className="text-muted-foreground">
                    Students earn achievements and track their improvement through personalized dashboards.
                  </p>
                </div>
              </motion.div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="from-[var(--section-bg-primary-medium)] to-[var(--section-bg-secondary-medium)] bg-gradient-to-br py-20">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="mx-auto max-w-3xl text-center"
          >
            <h2 className="mb-4 text-3xl font-bold md:text-4xl lg:text-5xl tracking-tight">
              Ready to Transform Physical Education?
            </h2>
            <p className="text-muted-foreground mb-8 text-lg">
              Join the revolution in PE monitoring and make exercise safer and more engaging for
              everyone.
            </p>

            <div className="flex justify-center">
              <Button 
                size="lg" 
                className="cta-button font-medium text-base h-14 px-12 w-full sm:w-auto"
                asChild
              >
                <Link href="/sign-up">
                  <span className="relative z-10">Start Free Trial</span>
                </Link>
              </Button>
            </div>
            <div className="mt-6 flex flex-col sm:flex-row gap-4 justify-center text-sm text-muted-foreground">
              <a href="#" className="flex items-center gap-2 hover:text-primary transition-colors">
                <Smartphone className="h-4 w-4" />
                Download Mobile App
              </a>
              <a href="#" className="flex items-center gap-2 hover:text-primary transition-colors">
                <School className="h-4 w-4" />
                Teacher Resources
              </a>
              <a href="#" className="flex items-center gap-2 hover:text-primary transition-colors">
                <Users className="h-4 w-4" />
                Contact Support
              </a>
            </div>
          </motion.div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
