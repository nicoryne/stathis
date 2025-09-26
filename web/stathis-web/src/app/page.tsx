'use client';

import { Navbar } from '@/components/navbar';
import { Footer } from '@/components/footer';
import { Button } from '@/components/ui/button';
import { motion } from 'framer-motion';
import {
  Users,
  BookOpen,
  Clock,
  TrendingUp,
  Play,
  CheckCircle,
  School,
  BarChart,
  Award,
  MapPin,
  Phone,
  Mail,
  Smartphone,
  Target,
  Zap
} from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';
import { Badge } from '@/components/ui/badge';
import { useReducedMotion } from '@/hooks/use-reduced-motion';
import { GoogleMap } from '@/components/maps/google-map';

export default function Home() {
  const prefersReducedMotion = useReducedMotion();
  
  return (
    <div className="min-h-screen bg-background">
      <Navbar />

      {/* Hero Section */}
      <section className="min-h-screen flex items-center relative pt-20 pb-16 overflow-hidden gradient-bg-animation">
        {/* Floating Particles */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute top-1/4 left-1/4 w-4 h-4 bg-primary rounded-full particle-animation opacity-30" style={{animationDelay: '0s'}}></div>
          <div className="absolute top-1/3 right-1/3 w-3 h-3 bg-accent rounded-full particle-animation opacity-40" style={{animationDelay: '2s'}}></div>
          <div className="absolute bottom-1/4 left-1/3 w-5 h-5 bg-secondary rounded-full particle-animation opacity-25" style={{animationDelay: '4s'}}></div>
          <div className="absolute top-1/2 right-1/4 w-2 h-2 bg-primary rounded-full particle-animation opacity-50" style={{animationDelay: '1s'}}></div>
          <div className="absolute bottom-1/3 right-1/2 w-4 h-4 bg-accent rounded-full particle-animation opacity-35" style={{animationDelay: '3s'}}></div>
        </div>
        
        <div className="container mx-auto px-4 relative z-10">
          <div className="flex flex-col items-center gap-12 lg:flex-row">
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
              className="flex-1 space-y-8 text-center lg:text-left"
            >
              <div className="flex items-center justify-center lg:justify-start gap-2">
                <div className="hero-badge text-white inline-block rounded-full px-4 py-2 text-sm font-medium">
                  🎓 Statistics Learning Platform
                </div>
              </div>
              
              <h1 className="text-hero text-white leading-tight hero-text-shadow">
                Transform <span className="text-white font-extrabold hero-text-strong-shadow">Statistics Learning</span>
              </h1>
              
              <p className="text-white/90 max-w-2xl text-xl leading-relaxed subtitle-text-shadow">
                Master statistics with interactive lessons, real-world examples, and personalized learning paths. 
                Join thousands of students already excelling with STATHIS.
              </p>
              
              <div className="flex flex-col gap-4 pt-4 sm:flex-row justify-center lg:justify-start">
                <Button 
                  size="lg" 
                  className="cta-button font-medium text-base h-14 px-8 rounded-xl"
                  asChild
                >
                  <Link href="/sign-up">
                    <span className="relative z-10">Start Learning</span>
                  </Link>
                </Button>
                <Button 
                  size="lg" 
                  variant="outline"
                  className="font-medium text-base h-14 px-8 rounded-xl border-2 border-primary text-primary hover:bg-primary hover:text-primary-foreground transition-all duration-300"
                  asChild
                >
                  <Link href="#demo">
                    <Play className="w-5 h-5 mr-2" />
                    Watch Demo
                  </Link>
                </Button>
              </div>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="relative flex-1 gpu-accelerated flex items-center justify-center"
            >
              <div className="relative w-full max-w-md lg:max-w-lg flex items-center justify-center">
                {/* Phone with Glow Effect */}
                <div className="relative flex items-center justify-center">
                  <div className="absolute inset-0 pulse-glow-animation rounded-full w-80 h-80 -translate-x-1/2 -translate-y-1/2 top-1/2 left-1/2"></div>
                  <Image
                    src="/images/stathis_phone.png"
                    alt="STATHIS Statistics Learning Phone"
                    width={420}
                    height={840}
                    className="relative z-10 float-animation"
                    priority
                  />
                </div>
              </div>
            </motion.div>
          </div>
        </div>
      </section>

      {/* About STATHIS Section */}
      <section id="about" className="py-20 bg-muted/30">
        <div className="container mx-auto px-4">
          <div className="flex flex-col lg:flex-row items-start gap-16">
            {/* Left Side - About Description */}
          <motion.div
              initial={{ opacity: 0, x: -20 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6 }}
            viewport={{ once: true }}
              className="flex-1 space-y-8"
            >
              <div>
                <h2 className="text-4xl md:text-5xl font-bold mb-6 text-foreground">
                  About <span className="text-primary">STATHIS</span>
                </h2>
                
                <div className="mb-8">
                  <h3 className="text-xl font-semibold mb-4 text-foreground/80">What It Is</h3>
                  <p className="text-muted-foreground leading-relaxed text-lg">
                    STATHIS is a web-based learning platform designed to make statistics engaging and 
                    accessible. Whether you're a student, educator, or enthusiast, STATHIS helps you learn 
                    through interactive quizzes, performance tracking, and peer competition.
                  </p>
            </div>
            </div>
            
              {/* Statistics Cards */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: 0.2 }}
                viewport={{ once: true }}
                  className="bg-card rounded-2xl p-6 shadow-lg border border-border"
              >
                  <h3 className="text-3xl font-bold text-primary mb-2">10K+</h3>
                  <p className="text-muted-foreground font-medium">Active Learners</p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.6, delay: 0.3 }}
            viewport={{ once: true }}
                  className="bg-card rounded-2xl p-6 shadow-lg border border-border"
              >
                  <h3 className="text-3xl font-bold text-accent mb-2">500+</h3>
                  <p className="text-muted-foreground font-medium">Quiz Questions</p>
              </motion.div>
            </div>
          </motion.div>

            {/* Right Side - Mascot with Stats */}
            <motion.div
              initial={{ opacity: 0, x: 20 }}
              whileInView={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.6, delay: 0.4 }}
              viewport={{ once: true }}
              className="flex-1 flex justify-center relative"
            >
              <div className="relative">
                {/* Quiz Score Badge */}
          <motion.div
                  initial={{ opacity: 0, scale: 0.8 }}
                  whileInView={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.6, delay: 0.5 }}
            viewport={{ once: true }}
                  className="absolute -top-4 -right-8 bg-card rounded-2xl p-4 shadow-lg border border-border float-animation"
                  style={{animationDelay: '0.5s'}}
              >
                  <p className="text-sm text-muted-foreground mb-1">Quiz Score</p>
                  <p className="text-2xl font-bold text-primary">95%</p>
          </motion.div>
          
                {/* Mascot with Background */}
                <div className="mx-auto w-80 h-80 flex items-center justify-center relative">
                  {/* Background circle with glow effect - subtle in light mode, prominent in dark mode */}
                  <div className="absolute inset-0 rounded-full bg-gradient-to-br from-primary/10 to-accent/5 dark:from-card dark:to-muted border border-border/20 dark:border-border/30 pulse-glow-animation mascot-glow-animation"></div>
                  <Image
                    src="/images/mascots/mascot_sleep.png"
                    alt="STATHIS Mascot"
                    width={280}
                    height={280}
                    className="relative z-10 float-animation"
                    priority
                  />
        </div>

                {/* Streak Badge */}
          <motion.div
                  initial={{ opacity: 0, scale: 0.8 }}
                  whileInView={{ opacity: 1, scale: 1 }}
                  transition={{ duration: 0.6, delay: 0.8 }}
            viewport={{ once: true }}
                  className="absolute -bottom-4 -left-8 bg-card rounded-2xl p-4 shadow-lg border border-border float-animation"
                  style={{animationDelay: '0.8s'}}
          >
                  <p className="text-sm text-muted-foreground mb-1">Streak</p>
                  <p className="text-2xl font-bold text-accent">12 days</p>
              </motion.div>
            </div>
          </motion.div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section id="features" className="py-20 bg-primary/5">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <div className="bg-secondary/10 text-secondary mb-6 inline-block rounded-full px-4 py-2 text-sm font-medium">
              Why Choose STATHIS?
            </div>
            <h2 className="text-section-title mb-6 text-foreground">Powerful Features for Effective Learning</h2>
            <p className="text-muted-foreground text-lg max-w-3xl mx-auto leading-relaxed">
              STATHIS combines innovative technology with proven educational methods to deliver 
              an unparalleled statistics learning experience.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            {/* Feature 1: Interactive Learning */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
              viewport={{ once: true }}
              className="bg-card rounded-xl p-8 card-hover-effect shadow-lg"
            >
              <div className="bg-primary/10 rounded-full p-4 w-16 h-16 flex items-center justify-center mb-6">
                <Play className="h-8 w-8 text-primary" />
              </div>
              <h3 className="text-xl font-bold mb-4">Interactive Learning</h3>
              <p className="text-muted-foreground leading-relaxed">
                Engage with statistics through hands-on simulations, interactive visualizations, 
                and real-time problem-solving exercises that make complex concepts easy to understand.
              </p>
            </motion.div>

            {/* Feature 2: Expert Instructors */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              viewport={{ once: true }}
              className="bg-card rounded-xl p-8 card-hover-effect shadow-lg"
            >
              <div className="bg-accent/10 rounded-full p-4 w-16 h-16 flex items-center justify-center mb-6">
                <CheckCircle className="h-8 w-8 text-accent" />
              </div>
              <h3 className="text-xl font-bold mb-4">Expert Instructors</h3>
              <p className="text-muted-foreground leading-relaxed">
                Learn from qualified statistics professionals and educators who bring years of 
                experience and proven teaching methodologies to every lesson.
            </p>
            </motion.div>

            {/* Feature 3: Flexible Schedule */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.3 }}
              viewport={{ once: true }}
              className="bg-card rounded-xl p-8 card-hover-effect shadow-lg"
            >
              <div className="bg-secondary/10 rounded-full p-4 w-16 h-16 flex items-center justify-center mb-6">
                <Clock className="h-8 w-8 text-secondary" />
              </div>
              <h3 className="text-xl font-bold mb-4">Flexible Schedule</h3>
              <p className="text-muted-foreground leading-relaxed">
                Study at your own pace with 24/7 access to course materials, allowing you to 
                balance learning with your personal and professional commitments.
            </p>
            </motion.div>

            {/* Feature 4: Real-world Applications */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.4 }}
              viewport={{ once: true }}
              className="bg-card rounded-xl p-8 card-hover-effect shadow-lg"
            >
              <div className="bg-primary/10 rounded-full p-4 w-16 h-16 flex items-center justify-center mb-6">
                <TrendingUp className="h-8 w-8 text-primary" />
                </div>
              <h3 className="text-xl font-bold mb-4">Real-world Applications</h3>
              <p className="text-muted-foreground leading-relaxed">
                Apply statistical concepts to practical scenarios from business, healthcare, 
                research, and technology to understand how statistics drive decision-making.
              </p>
            </motion.div>
          </div>
        </div>
      </section>
      
      {/* Location Section */}
      <section className="py-20 bg-background">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-4 text-foreground">
              Where Innovation Meets Education
            </h2>
            <p className="text-muted-foreground text-lg font-medium">
              STATHIS HQ
            </p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            viewport={{ once: true }}
            className="max-w-6xl mx-auto"
          >
            <div className="bg-card rounded-2xl shadow-lg border border-border overflow-hidden">
              <div className="grid grid-cols-1 lg:grid-cols-2">
                {/* Left Side - University Info */}
                <div className="p-8 lg:p-12">
                  <div className="flex items-start gap-4 mb-6">
                    <div className="bg-primary/10 rounded-full p-3">
                      <School className="h-6 w-6 text-primary" />
                    </div>
                    <div>
                      <h3 className="text-2xl font-bold text-foreground mb-2">CIT-University</h3>
                      <p className="text-muted-foreground font-medium">Cebu City, Philippines</p>
                    </div>
                  </div>
                  
                  <p className="text-muted-foreground leading-relaxed mb-8">
                    STATHIS is proudly based at CIT-University, Cebu City—an institution 
                    known for cultivating forward-thinking minds and technological excellence.
                  </p>
                  
                  <div className="space-y-4">
                    <div className="flex items-start gap-3">
                      <MapPin className="h-5 w-5 text-primary mt-1 flex-shrink-0" />
                      <div>
                        <p className="text-foreground font-medium">N. Bacalso Ave, Cebu City, 6000 Cebu</p>
                      </div>
                    </div>
                    
                    <div className="flex items-start gap-3">
                      <Phone className="h-5 w-5 text-primary mt-1 flex-shrink-0" />
                      <div>
                        <p className="text-foreground font-medium">+63 32 411 2000</p>
                      </div>
              </div>
              
                    <div className="flex items-start gap-3">
                      <Mail className="h-5 w-5 text-primary mt-1 flex-shrink-0" />
                      <div>
                        <p className="text-foreground font-medium">info@cit.edu</p>
                      </div>
                    </div>
                  </div>
              </div>
              
                {/* Right Side - Map */}
                <div className="bg-gradient-to-br from-primary/5 to-secondary/5 p-4 lg:p-6 flex items-center justify-center">
                  <div className="w-full h-full min-h-[300px] relative">
                    <GoogleMap />
                  </div>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* User Journey Section */}
      <section className="py-20 bg-secondary/5">
        <div className="container mx-auto px-4">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <div className="bg-primary/10 text-primary mb-6 inline-block rounded-full px-4 py-2 text-sm font-medium">
              Your Learning Journey
            </div>
            <h2 className="text-section-title mb-6 text-foreground">Simple Steps, Powerful Results</h2>
            <p className="text-muted-foreground text-lg max-w-3xl mx-auto leading-relaxed">
              Follow our proven 6-step learning path to master statistics and achieve your educational goals.
            </p>
          </motion.div>

          <div className="relative max-w-4xl mx-auto">
            {/* Progress Line */}
            <div className="absolute top-0 bottom-0 left-1/2 w-1 bg-primary/20 -translate-x-1/2 hidden md:block" />
            
            <div className="space-y-12">
              {/* Step 1: Discover */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-start md:items-center"
              >
                <div className="md:w-1/2 md:text-right order-2 md:order-1">
                  <h3 className="text-xl font-bold mb-3 text-foreground">1. Discover</h3>
                  <p className="text-muted-foreground">
                    Explore our comprehensive course catalog and find the perfect statistics program for your learning goals.
                  </p>
                </div>
                <div className="relative md:w-1/2 order-1 md:order-2 flex justify-center md:justify-start">
                  <div className="bg-primary/10 text-primary rounded-full p-4 w-16 h-16 flex items-center justify-center">
                    <Target className="h-8 w-8" />
                  </div>
                </div>
              </motion.div>
              
              {/* Step 2: Assess */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.1 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-start md:items-center"
              >
                <div className="relative md:w-1/2 order-1 flex justify-center md:justify-end">
                  <div className="bg-accent/10 text-accent rounded-full p-4 w-16 h-16 flex items-center justify-center">
                    <BarChart className="h-8 w-8" />
                  </div>
                </div>
                <div className="md:w-1/2 md:text-left order-2">
                  <h3 className="text-xl font-bold mb-3 text-foreground">2. Assess</h3>
                  <p className="text-muted-foreground">
                    Take our skill assessment to create a personalized learning path tailored to your current knowledge level.
                  </p>
                </div>
              </motion.div>
              
              {/* Step 3: Learn */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-start md:items-center"
              >
                <div className="md:w-1/2 md:text-right order-2 md:order-1">
                  <h3 className="text-xl font-bold mb-3 text-foreground">3. Learn</h3>
                  <p className="text-muted-foreground">
                    Engage with interactive lessons, multimedia content, and expert-led tutorials designed for optimal comprehension.
                  </p>
                </div>
                <div className="relative md:w-1/2 order-1 md:order-2 flex justify-center md:justify-start">
                  <div className="bg-secondary/10 text-secondary rounded-full p-4 w-16 h-16 flex items-center justify-center">
                    <BookOpen className="h-8 w-8" />
                  </div>
                </div>
              </motion.div>
              
              {/* Step 4: Practice */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-start md:items-center"
              >
                <div className="relative md:w-1/2 order-1 flex justify-center md:justify-end">
                  <div className="bg-primary/10 text-primary rounded-full p-4 w-16 h-16 flex items-center justify-center">
                    <Zap className="h-8 w-8" />
                  </div>
                </div>
                <div className="md:w-1/2 md:text-left order-2">
                  <h3 className="text-xl font-bold mb-3 text-foreground">4. Practice</h3>
                  <p className="text-muted-foreground">
                    Reinforce your knowledge with hands-on exercises, real-world problem-solving scenarios, and interactive simulations.
                  </p>
                </div>
              </motion.div>
              
              {/* Step 5: Apply */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.4 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-start md:items-center"
              >
                <div className="md:w-1/2 md:text-right order-2 md:order-1">
                  <h3 className="text-xl font-bold mb-3 text-foreground">5. Apply</h3>
                  <p className="text-muted-foreground">
                    Work on real-world projects and case studies to apply statistical concepts to practical business and research scenarios.
                  </p>
                </div>
                <div className="relative md:w-1/2 order-1 md:order-2 flex justify-center md:justify-start">
                  <div className="bg-accent/10 text-accent rounded-full p-4 w-16 h-16 flex items-center justify-center">
                    <TrendingUp className="h-8 w-8" />
                  </div>
                </div>
              </motion.div>
              
              {/* Step 6: Master */}
              <motion.div 
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.5 }}
                viewport={{ once: true }}
                className="flex flex-col md:flex-row gap-8 items-start md:items-center"
              >
                <div className="relative md:w-1/2 order-1 flex justify-center md:justify-end">
                  <div className="bg-secondary/10 text-secondary rounded-full p-4 w-16 h-16 flex items-center justify-center">
                    <Award className="h-8 w-8" />
                  </div>
                </div>
                <div className="md:w-1/2 md:text-left order-2">
                  <h3 className="text-xl font-bold mb-3 text-foreground">6. Master</h3>
                  <p className="text-muted-foreground">
                    Earn your certification and continue your learning journey with advanced topics and specialized statistical methods.
                  </p>
                </div>
              </motion.div>
            </div>
          </div>
        </div>
      </section>

      {/* Transform Section (Final CTA) */}
      <section className="py-20 bg-muted relative overflow-hidden">
        <div className="container mx-auto px-4 relative z-10">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
            viewport={{ once: true }}
            className="text-center mb-16"
          >
            <h2 className="text-4xl md:text-5xl font-bold mb-6 text-foreground">
              Ready to Transform <span className="text-primary">Statistics</span> <span className="text-accent">Education?</span>
            </h2>
            <p className="text-muted-foreground text-lg max-w-3xl mx-auto leading-relaxed">
              Join thousands of educators and students who are revolutionizing how statistics is 
              taught and learned. Experience the future of education with STATHIS.
            </p>
          </motion.div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-16 items-center mb-16">
            {/* Left Side - Feature Cards */}
            <div className="space-y-6">
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.1 }}
                viewport={{ once: true }}
                className="bg-card rounded-xl p-6 shadow-sm border border-border"
              >
                <div className="flex items-center gap-3 mb-3">
                  <div className="bg-primary/10 rounded-full p-2">
                    <Users className="h-5 w-5 text-primary" />
                  </div>
                  <h3 className="font-bold text-foreground">Engage Every Student</h3>
                </div>
                <p className="text-muted-foreground text-sm">
                  Interactive learning that adapts to different learning styles and paces
                </p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: -20 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                viewport={{ once: true }}
                className="bg-card rounded-xl p-6 shadow-sm border border-border"
              >
                <div className="flex items-center gap-3 mb-3">
                  <div className="bg-accent/10 rounded-full p-2">
                    <TrendingUp className="h-5 w-5 text-accent" />
                  </div>
                  <h3 className="font-bold text-foreground">Boost Performance</h3>
                </div>
                <p className="text-muted-foreground text-sm">
                  Data-driven insights help educators track and improve student outcomes
                </p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: -20 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                viewport={{ once: true }}
                className="bg-card rounded-xl p-6 shadow-sm border border-border"
              >
                <div className="flex items-center gap-3 mb-3">
                  <div className="bg-secondary/10 rounded-full p-2">
                    <Award className="h-5 w-5 text-secondary" />
                  </div>
                  <h3 className="font-bold text-foreground">Gamified Learning</h3>
                </div>
                <p className="text-muted-foreground text-sm">
                  Achievement systems and leaderboards that motivate continuous improvement
                </p>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, x: -20 }}
                whileInView={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, delay: 0.4 }}
                viewport={{ once: true }}
                className="bg-card rounded-xl p-6 shadow-sm border border-border"
              >
                <div className="flex items-center gap-3 mb-3">
                  <div className="bg-primary/10 rounded-full p-2">
                    <Smartphone className="h-5 w-5 text-primary" />
                  </div>
                  <h3 className="font-bold text-foreground">Accessible Anywhere</h3>
                </div>
                <p className="text-muted-foreground text-sm">
                  Cloud-based platform accessible on any device, anytime, anywhere
                </p>
              </motion.div>
            </div>

            {/* Right Side - Mascot and CTA */}
            <div className="text-center">
              <motion.div
                initial={{ opacity: 0, scale: 0.8 }}
                whileInView={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                viewport={{ once: true }}
                className="relative mb-8"
              >
                <div className="relative mx-auto w-96 h-96 bg-gradient-to-br from-primary/10 to-accent/10 rounded-[3rem] flex items-center justify-center border border-border/20">
                  <Image
                    src="/images/mascots/mascot_cheer.png"
                    alt="STATHIS Statistics Learning Mascot"
                    width={320}
                    height={320}
                    className="float-animation"
                    priority
                  />
                </div>
              </motion.div>

              <motion.div
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.5 }}
                viewport={{ once: true }}
                className="mb-8"
              >
                <h3 className="text-2xl font-bold text-foreground mb-4">Join the STATHIS Revolution</h3>
                <p className="text-muted-foreground mb-6 max-w-md mx-auto">
                  Be part of a community that's making statistics accessible, engaging, and fun for everyone.
                </p>
                
                <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Button 
                size="lg" 
                    className="cta-button font-medium text-base h-14 px-8 rounded-xl"
                asChild
              >
                <Link href="/sign-up">
                      Start Learning Today
                    </Link>
                  </Button>
                  <Button 
                    size="lg" 
                    variant="outline"
                    className="font-medium text-base h-14 px-8 rounded-xl border-2 border-primary text-primary hover:bg-primary hover:text-primary-foreground transition-all duration-300"
                    asChild
                  >
                    <Link href="#community">
                      Join Our Community
                </Link>
              </Button>
            </div>
              </motion.div>
            </div>
          </div>

          {/* Statistics Showcase */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.7 }}
            viewport={{ once: true }}
            className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center"
          >
            <div>
              <div className="text-3xl font-bold text-primary mb-2">10K+</div>
              <div className="text-muted-foreground text-sm">Active Students</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-accent mb-2">500+</div>
              <div className="text-muted-foreground text-sm">Educators</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-secondary mb-2">95%</div>
              <div className="text-muted-foreground text-sm">Success Rate</div>
            </div>
            <div>
              <div className="text-3xl font-bold text-primary mb-2">24/7</div>
              <div className="text-muted-foreground text-sm">Support</div>
            </div>
          </motion.div>
        </div>
      </section>

      <Footer />
    </div>
  );
}
